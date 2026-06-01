package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.example.developernetworkingapp.data.datasource.firebase.schema.InboxNotificationDoc
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Notifications microservice: per-user inbox in [FirestorePaths.INBOX].
 * Clients may read and mark read; creates are server-only (rules).
 */
class FirestoreNotificationsDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun observeInboxForUser(recipientUserId: String): Flow<List<InboxNotificationDoc>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = db.collection(FirestorePaths.INBOX)
            .whereEqualTo("recipientUserId", recipientUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(InboxNotificationDoc::class.java)?.copy(
                            id = doc.id,
                            createdAt = doc.readTimestamp("createdAt"),
                        )
                    }.orEmpty()
                    .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                trySend(items)
            }
        awaitClose { registration?.remove() }
    }

    suspend fun markNotificationRead(notificationId: String) {
        db.collection(FirestorePaths.INBOX)
            .document(notificationId)
            .update("read", true)
            .await()
    }
}
