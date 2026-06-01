package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.EventDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Events microservice: curated hackathons / meetups in top-level [FirestorePaths.EVENTS].
 * Client read-only; writes via Admin SDK or Cloud Functions later.
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
}
