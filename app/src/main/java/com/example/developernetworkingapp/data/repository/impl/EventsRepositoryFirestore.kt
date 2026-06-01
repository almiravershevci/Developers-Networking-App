package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.repository.EventsRepository
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreEventsDataSource
import com.example.developernetworkingapp.data.datasource.firebase.authStateChanges
import com.example.developernetworkingapp.data.datasource.firebase.schema.EventDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.EventStatus
import com.example.developernetworkingapp.domain.model.EventContent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Events feed backed by Firestore (read-only curated collection).
 */
class EventsRepositoryFirestore(
    private val eventsDataSource: FirestoreEventsDataSource = FirestoreEventsDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : EventsRepository {

    override fun observeEvents(): Flow<EventContent> =
        firebaseAuth.authStateChanges().flatMapLatest { firebaseUser ->
            when {
                firebaseUser == null -> flow {
                    emit(
                        EventContent(
                            statusMessage = "Sign in to browse hackathons and live events.",
                        ),
                    )
                }
                !firebaseUser.isEmailVerified -> flow {
                    emit(
                        EventContent(
                            statusMessage = "Verify your email to load the events calendar.",
                        ),
                    )
                }
                else -> eventsDataSource.observeEvents()
                    .map { docs -> buildEventContent(docs) }
                    .catch { error ->
                        emit(
                            EventContent(
                                statusMessage = "Couldn't load events. Publish firestore.rules and seed the events collection. " +
                                    "(${error.message})",
                            ),
                        )
                    }
            }
        }.flowOn(Dispatchers.IO)

    private fun buildEventContent(docs: List<EventDoc>): EventContent {
        if (docs.isEmpty()) {
            return EventContent(
                statusMessage = "No events in Firestore yet. Seed the events collection or add docs via Admin SDK.",
            )
        }
        return EventContent(
            items = docs.map { formatEventLine(it) },
            eventIds = docs.map { it.id },
        )
    }

    private fun formatEventLine(event: EventDoc): String {
        val summary = event.summaryLine.takeIf { it.isNotBlank() }
            ?: buildSummaryFallback(event)
        return "${event.title} - $summary"
    }

    private fun buildSummaryFallback(event: EventDoc): String {
        val status = when (event.eventStatus) {
            EventStatus.LIVE -> "Live now"
            else -> "Scheduled"
        }
        return "$status - ${event.participantCount} participants"
    }
}
