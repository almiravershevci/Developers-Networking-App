package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.example.developernetworkingapp.data.datasource.firebase.schema.MatchRequestDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.MatchWorkflow
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Match requests microservice: collaboration invites between developers.
 */
class FirestoreMatchRequestsDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun observeIncomingPending(userId: String): Flow<List<MatchRequestDoc>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = db.collection(FirestorePaths.MATCH_REQUESTS)
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("workflowStatus", MatchWorkflow.PENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(MatchRequestDoc::class.java)?.copy(
                            id = doc.id,
                            createdAt = doc.readTimestamp("createdAt"),
                            resolvedAt = doc.readTimestamp("resolvedAt"),
                        )
                    }
                    ?.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                    .orEmpty()
                trySend(requests)
            }
        awaitClose { registration?.remove() }
    }

    fun observeOutgoingPending(userId: String): Flow<List<MatchRequestDoc>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = db.collection(FirestorePaths.MATCH_REQUESTS)
            .whereEqualTo("fromUserId", userId)
            .whereEqualTo("workflowStatus", MatchWorkflow.PENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(MatchRequestDoc::class.java)?.copy(
                            id = doc.id,
                            createdAt = doc.readTimestamp("createdAt"),
                            resolvedAt = doc.readTimestamp("resolvedAt"),
                        )
                    }
                    ?.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                    .orEmpty()
                trySend(requests)
            }
        awaitClose { registration?.remove() }
    }

    suspend fun fetchMatchRequest(requestId: String): MatchRequestDoc? {
        val snap = db.collection(FirestorePaths.MATCH_REQUESTS).document(requestId).get().await()
        if (!snap.exists()) return null
        return snap.toObject(MatchRequestDoc::class.java)?.copy(
            id = snap.id,
            createdAt = snap.readTimestamp("createdAt"),
            resolvedAt = snap.readTimestamp("resolvedAt"),
        )
    }

    suspend fun createMatchRequest(
        fromUserId: String,
        toUserId: String,
        message: String?,
    ): String {
        require(fromUserId.isNotBlank() && toUserId.isNotBlank())
        require(fromUserId != toUserId)

        val ref = db.collection(FirestorePaths.MATCH_REQUESTS).document()
        val payload = mapOf(
            "schemaVersion" to 1,
            "fromUserId" to fromUserId,
            "toUserId" to toUserId,
            "workflowStatus" to MatchWorkflow.PENDING,
            "message" to message?.trim()?.takeIf { it.isNotEmpty() },
            "createdAt" to FieldValue.serverTimestamp(),
            "resolvedAt" to null,
        )
        ref.set(payload).await()
        return ref.id
    }

    suspend fun updateWorkflowStatus(requestId: String, workflowStatus: String) {
        db.collection(FirestorePaths.MATCH_REQUESTS).document(requestId)
            .update(
                mapOf(
                    "workflowStatus" to workflowStatus,
                    "resolvedAt" to FieldValue.serverTimestamp(),
                ),
            )
            .await()
    }
}
