package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.AccountRole
import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.example.developernetworkingapp.data.datasource.firebase.schema.InboxNotificationDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.MatchRequestDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.MatchWorkflow
import com.example.developernetworkingapp.data.datasource.firebase.schema.NotificationKind
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectLifecycle
import com.example.developernetworkingapp.data.datasource.firebase.schema.UserProfileDoc
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Admin microservice: privileged reads/writes on curated and user-generated collections.
 */
class FirestoreAdminDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun fetchDirectoryUsers(limit: Long = 40): List<UserProfileDoc> {
        val snap = db.collection(FirestorePaths.USERS).limit(limit).get().await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(UserProfileDoc::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun fetchDirectoryProjects(limit: Long = 40): List<ProjectDoc> {
        val snap = db.collection(FirestorePaths.PROJECTS).limit(limit).get().await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(ProjectDoc::class.java)?.copy(
                id = doc.id,
                updatedAt = doc.readTimestamp("updatedAt"),
                createdAt = doc.readTimestamp("createdAt"),
            )
        }
    }

    suspend fun fetchPendingMatchRequests(limit: Long = 20): List<MatchRequestDoc> {
        val snap = db.collection(FirestorePaths.MATCH_REQUESTS)
            .whereEqualTo("workflowStatus", MatchWorkflow.PENDING)
            .limit(limit)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(MatchRequestDoc::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun fetchRecentInbox(limit: Long = 25): List<InboxNotificationDoc> {
        val snap = db.collection(FirestorePaths.INBOX).limit(limit).get().await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(InboxNotificationDoc::class.java)?.copy(
                id = doc.id,
                createdAt = doc.readTimestamp("createdAt"),
            )
        }.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
    }

    suspend fun setUserAccountRole(userId: String, accountRole: String) {
        db.collection(FirestorePaths.USERS).document(userId)
            .set(
                mapOf(
                    "accountRole" to accountRole,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
    }

    suspend fun updateUserModerationFields(
        userId: String,
        skillTags: List<String>,
        bio: String,
    ) {
        db.collection(FirestorePaths.USERS).document(userId)
            .set(
                mapOf(
                    "skillTags" to skillTags,
                    "bio" to bio.trim(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
    }

    suspend fun updateProjectFields(
        projectId: String,
        title: String,
        primaryStackLabel: String,
        lifecycleStatus: String? = null,
    ) {
        val payload = mutableMapOf<String, Any>(
            "title" to title.trim(),
            "primaryStackLabel" to primaryStackLabel.trim(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        if (lifecycleStatus != null) {
            payload["lifecycleStatus"] = lifecycleStatus
        }
        db.collection(FirestorePaths.PROJECTS).document(projectId)
            .set(payload, SetOptions.merge())
            .await()
    }

    suspend fun createInboxNotification(
        recipientUserId: String,
        title: String,
        body: String,
        deepLink: String? = null,
    ): String {
        val ref = db.collection(FirestorePaths.INBOX).document()
        val payload = mapOf(
            "schemaVersion" to 1,
            "recipientUserId" to recipientUserId,
            "notificationKind" to NotificationKind.FEED,
            "title" to title.trim(),
            "body" to body.trim(),
            "deepLink" to deepLink,
            "read" to false,
            "createdAt" to FieldValue.serverTimestamp(),
        )
        ref.set(payload).await()
        return ref.id
    }

    suspend fun broadcastInboxNotification(
        recipientUserIds: Collection<String>,
        title: String,
        body: String,
    ): Int {
        var sent = 0
        for (userId in recipientUserIds.distinct()) {
            if (userId.isBlank()) continue
            createInboxNotification(
                recipientUserId = userId,
                title = title,
                body = body,
                deepLink = "/notifications",
            )
            sent++
        }
        return sent
    }

    suspend fun resolveMatchRequest(requestId: String, accepted: Boolean) {
        val status = if (accepted) MatchWorkflow.ACCEPTED else MatchWorkflow.DECLINED
        db.collection(FirestorePaths.MATCH_REQUESTS).document(requestId)
            .update(
                mapOf(
                    "workflowStatus" to status,
                    "resolvedAt" to FieldValue.serverTimestamp(),
                ),
            )
            .await()
    }
}
