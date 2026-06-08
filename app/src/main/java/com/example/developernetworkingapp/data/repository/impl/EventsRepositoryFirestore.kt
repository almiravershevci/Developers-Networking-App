package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.datasource.firebase.FirestoreEventsDataSource
import com.example.developernetworkingapp.data.datasource.firebase.authStateChanges
import com.example.developernetworkingapp.data.datasource.firebase.schema.EventDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.EventStatus
import com.example.developernetworkingapp.data.repository.EventsRepository
import com.example.developernetworkingapp.domain.model.EventContent
import com.example.developernetworkingapp.domain.model.EventItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Events microservice — curated Firestore calendar with per-user RSVP subcollection.
 */
class EventsRepositoryFirestore(
    private val eventsDataSource: FirestoreEventsDataSource = FirestoreEventsDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : EventsRepository {

    override fun observeEvents(): Flow<EventContent> =
        firebaseAuth.authStateChanges().flatMapLatest { firebaseUser ->
            when {
                firebaseUser == null -> flowOf(signedOutContent())
                !firebaseUser.isEmailVerified -> flowOf(needsVerificationContent())
                else -> combine(
                    eventsDataSource.observeEvents(),
                    eventsDataSource.observeMyRegistrations(firebaseUser.uid),
                ) { events, registeredIds ->
                    buildEventContent(events, registeredIds.toSet())
                }.catch { error ->
                    emit(eventErrorContent(error))
                }
            }
        }.flowOn(Dispatchers.IO)

    override fun observeMyRegistrations(): Flow<List<String>> =
        firebaseAuth.authStateChanges().flatMapLatest { firebaseUser ->
            when {
                firebaseUser == null || !firebaseUser.isEmailVerified -> flowOf(emptyList())
                else -> eventsDataSource.observeMyRegistrations(firebaseUser.uid)
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun registerForEvent(eventId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val uid = requireSignedInUser() ?: return@withContext authRequiredFailure()
            if (eventId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Event id is required."))
            }
            runCatching {
                eventsDataSource.registerForEvent(eventId = eventId, userId = uid)
                Unit
            }.toEventResult("register for event")
        }

    override suspend fun unregisterFromEvent(eventId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val uid = requireSignedInUser() ?: return@withContext authRequiredFailure()
            if (eventId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Event id is required."))
            }
            runCatching {
                eventsDataSource.unregisterFromEvent(eventId = eventId, userId = uid)
                Unit
            }.toEventResult("unregister from event")
        }

    private fun buildEventContent(
        docs: List<EventDoc>,
        registeredIds: Set<String>,
    ): EventContent {
        if (docs.isEmpty()) {
            return EventContent(
                statusMessage = "No events in Firestore yet. Seed the events collection or add docs via Admin SDK.",
            )
        }
        return EventContent(
            items = docs.map { event ->
                EventItem(
                    id = event.id,
                    displayLine = formatEventLine(event),
                    isRegistered = event.id in registeredIds,
                )
            },
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

    private fun signedOutContent() = EventContent(
        statusMessage = "Sign in to browse hackathons and live events.",
    )

    private fun needsVerificationContent() = EventContent(
        statusMessage = "Verify your email to load the events calendar.",
    )

    private fun eventErrorContent(error: Throwable): EventContent {
        val detail = error.message.orEmpty()
        val message = when {
            detail.contains("PERMISSION_DENIED", ignoreCase = true) ->
                "Events blocked by Firestore rules. Publish firestore.rules and retry."
            detail.contains("FAILED_PRECONDITION", ignoreCase = true) ||
                detail.contains("index", ignoreCase = true) ->
                "Firestore needs a registrations index. Deploy firestore.indexes.json, then retry."
            else -> "Couldn't load events. ($detail)"
        }
        return EventContent(statusMessage = message)
    }

    private fun requireSignedInUser() = firebaseAuth.currentUser?.takeIf { it.isEmailVerified }?.uid

    private fun authRequiredFailure(): Result<Unit> =
        Result.failure(IllegalStateException("Sign in with a verified email to manage event registrations."))

    private fun Result<Unit>.toEventResult(action: String): Result<Unit> = fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { error ->
            val detail = error.message.orEmpty()
            val message = when {
                detail.contains("PERMISSION_DENIED", ignoreCase = true) ->
                    "Registration blocked by Firestore rules. Publish firestore.rules and retry."
                detail.isNotBlank() -> detail
                else -> "Couldn't $action. Try again."
            }
            Result.failure(IllegalStateException(message, error))
        },
    )
}
