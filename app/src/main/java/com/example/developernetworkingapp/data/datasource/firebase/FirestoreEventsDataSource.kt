package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.EventDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.EventRegistrationDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.EventRegistrationStatus
import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Events microservice: curated calendar reads and per-user RSVP writes.
 */
class FirestoreEventsDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun observeEvents(): Flow<List<EventDoc>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = db.collection(FirestorePaths.EVENTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(EventDoc::class.java)?.copy(
                            id = doc.id,
                            startsAt = doc.readTimestamp("startsAt"),
                        )
                    }.orEmpty()
                    .sortedBy { it.startsAt?.toDate()?.time ?: Long.MAX_VALUE }
                trySend(events)
            }
        awaitClose { registration?.remove() }
    }

    suspend fun fetchEventsOnce(): List<EventDoc> {
        val snap = db.collection(FirestorePaths.EVENTS).get().await()
        return snap.documents
            .mapNotNull { doc ->
                doc.toObject(EventDoc::class.java)?.copy(
                    id = doc.id,
                    startsAt = doc.readTimestamp("startsAt"),
                )
            }
            .sortedBy { it.startsAt?.toDate()?.time ?: Long.MAX_VALUE }
    }

    fun observeMyRegistrations(userId: String): Flow<List<String>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = db.collectionGroup(FirestorePaths.REGISTRATIONS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val eventIds = snapshot?.documents
                    ?.mapNotNull { doc -> doc.reference.parent?.parent?.id }
                    .orEmpty()
                trySend(eventIds)
            }
        awaitClose { registration?.remove() }
    }

    suspend fun registerForEvent(
        eventId: String,
        userId: String,
        status: String = EventRegistrationStatus.GOING,
    ) {
        require(eventId.isNotBlank() && userId.isNotBlank())
        db.collection(FirestorePaths.EVENTS)
            .document(eventId)
            .collection(FirestorePaths.REGISTRATIONS)
            .document(userId)
            .set(
                mapOf(
                    "schemaVersion" to 1,
                    "userId" to userId,
                    "status" to status,
                    "registeredAt" to FieldValue.serverTimestamp(),
                ),
            )
            .await()
    }

    suspend fun unregisterFromEvent(eventId: String, userId: String) {
        require(eventId.isNotBlank() && userId.isNotBlank())
        db.collection(FirestorePaths.EVENTS)
            .document(eventId)
            .collection(FirestorePaths.REGISTRATIONS)
            .document(userId)
            .delete()
            .await()
    }

    suspend fun fetchRegistration(eventId: String, userId: String): EventRegistrationDoc? {
        val snap = db.collection(FirestorePaths.EVENTS)
            .document(eventId)
            .collection(FirestorePaths.REGISTRATIONS)
            .document(userId)
            .get()
            .await()
        if (!snap.exists()) return null
        return snap.toObject(EventRegistrationDoc::class.java)?.copy(
            id = snap.id,
            registeredAt = snap.readTimestamp("registeredAt"),
        )
    }
}
