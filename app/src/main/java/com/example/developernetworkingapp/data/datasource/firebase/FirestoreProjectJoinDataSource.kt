package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.example.developernetworkingapp.data.datasource.firebase.schema.MatchWorkflow
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectJoinRequestDoc
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreProjectJoinDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun observeIncomingPending(ownerUserId: String): Flow<List<ProjectJoinRequestDoc>> = callbackFlow {
        val registration = db.collection(FirestorePaths.PROJECT_JOIN_REQUESTS)
            .whereEqualTo("toUserId", ownerUserId)
            .whereEqualTo("workflowStatus", MatchWorkflow.PENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents
                    ?.mapNotNull { doc -> doc.toProjectJoinRequestDoc() }
                    .orEmpty()
                    .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                trySend(requests)
            }
        awaitClose { registration.remove() }
    }

    fun observeOutgoingPending(applicantUserId: String): Flow<List<ProjectJoinRequestDoc>> = callbackFlow {
        val registration = db.collection(FirestorePaths.PROJECT_JOIN_REQUESTS)
            .whereEqualTo("fromUserId", applicantUserId)
            .whereEqualTo("workflowStatus", MatchWorkflow.PENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents
                    ?.mapNotNull { doc -> doc.toProjectJoinRequestDoc() }
                    .orEmpty()
                    .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                trySend(requests)
            }
        awaitClose { registration.remove() }
    }

    suspend fun fetchJoinRequest(requestId: String): ProjectJoinRequestDoc? {
        val snap = db.collection(FirestorePaths.PROJECT_JOIN_REQUESTS).document(requestId).get().await()
        return snap.toProjectJoinRequestDoc()
    }

    suspend fun fetchAllPending(limit: Long = 40): List<ProjectJoinRequestDoc> {
        val snap = db.collection(FirestorePaths.PROJECT_JOIN_REQUESTS)
            .whereEqualTo("workflowStatus", MatchWorkflow.PENDING)
            .limit(limit)
            .get()
            .await()
        return snap.documents.mapNotNull { doc -> doc.toProjectJoinRequestDoc() }
            .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
    }

    suspend fun fetchPendingForProject(projectId: String, applicantUserId: String): ProjectJoinRequestDoc? {
        val snap = db.collection(FirestorePaths.PROJECT_JOIN_REQUESTS)
            .whereEqualTo("projectId", projectId)
            .whereEqualTo("fromUserId", applicantUserId)
            .whereEqualTo("workflowStatus", MatchWorkflow.PENDING)
            .limit(1)
            .get()
            .await()
        return snap.documents.firstOrNull()?.toProjectJoinRequestDoc()
    }

    suspend fun fetchPendingProjectIds(applicantUserId: String): Set<String> {
        val snap = db.collection(FirestorePaths.PROJECT_JOIN_REQUESTS)
            .whereEqualTo("fromUserId", applicantUserId)
            .whereEqualTo("workflowStatus", MatchWorkflow.PENDING)
            .limit(20)
            .get()
            .await()
        return snap.documents.mapNotNull { it.getString("projectId") }.toSet()
    }

    suspend fun fetchAcceptedProjectIds(applicantUserId: String): Set<String> {
        val snap = db.collection(FirestorePaths.PROJECT_JOIN_REQUESTS)
            .whereEqualTo("fromUserId", applicantUserId)
            .whereEqualTo("workflowStatus", MatchWorkflow.ACCEPTED)
            .limit(50)
            .get()
            .await()
        return snap.documents.mapNotNull { it.getString("projectId") }.toSet()
    }

    suspend fun createJoinRequest(
        projectId: String,
        projectTitle: String,
        fromUserId: String,
        toUserId: String,
        requestedRole: String,
        message: String?,
    ): String {
        val ref = db.collection(FirestorePaths.PROJECT_JOIN_REQUESTS).document()
        val payload = mapOf(
            "schemaVersion" to 1,
            "projectId" to projectId,
            "projectTitle" to projectTitle.trim(),
            "fromUserId" to fromUserId,
            "toUserId" to toUserId,
            "requestedRole" to requestedRole.trim().ifBlank { "Contributor" },
            "message" to message?.trim()?.takeIf { it.isNotBlank() },
            "workflowStatus" to MatchWorkflow.PENDING,
            "createdAt" to FieldValue.serverTimestamp(),
        )
        ref.set(payload).await()
        return ref.id
    }

    suspend fun updateWorkflowStatus(requestId: String, workflowStatus: String) {
        db.collection(FirestorePaths.PROJECT_JOIN_REQUESTS).document(requestId)
            .update(
                mapOf(
                    "workflowStatus" to workflowStatus,
                    "resolvedAt" to FieldValue.serverTimestamp(),
                ),
            )
            .await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toProjectJoinRequestDoc(): ProjectJoinRequestDoc? {
        if (!exists()) return null
        return toObject(ProjectJoinRequestDoc::class.java)?.copy(
            id = id,
            createdAt = readTimestamp("createdAt"),
            resolvedAt = readTimestamp("resolvedAt"),
        )
    }
}
