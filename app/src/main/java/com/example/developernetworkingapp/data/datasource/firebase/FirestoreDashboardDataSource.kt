package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.ActivityItemDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.CollaboratorSuggestionDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.EventDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.example.developernetworkingapp.data.datasource.firebase.schema.NewsHighlightDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectLifecycle
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectVisibility
import com.example.developernetworkingapp.data.datasource.firebase.schema.UserProfileDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.UserStatsDoc
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Dashboard microservice data access: curated Firestore collections for home feed.
 */
class FirestoreDashboardDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun fetchUserStats(userId: String): UserStatsDoc? {
        val snap = db.collection(FirestorePaths.USER_STATS).document(userId).get().await()
        return snap.toUserStatsDocSafe()
    }

    suspend fun fetchCollaboratorSuggestions(viewerUserId: String): List<CollaboratorSuggestionDoc> {
        val snap = db.collection(FirestorePaths.COLLABORATOR_SUGGESTIONS)
            .whereEqualTo("viewerUserId", viewerUserId)
            .orderBy("rank", Query.Direction.ASCENDING)
            .limit(6)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(CollaboratorSuggestionDoc::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun fetchNewsHighlights(): List<NewsHighlightDoc> {
        val snap = db.collection(FirestorePaths.NEWS_HIGHLIGHTS)
            .orderBy("sortOrder", Query.Direction.ASCENDING)
            .limit(6)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(NewsHighlightDoc::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun fetchRecentActivity(audienceUserId: String): List<ActivityItemDoc> {
        val snap = db.collection(FirestorePaths.ACTIVITY)
            .whereEqualTo("audienceUserId", audienceUserId)
            .limit(8)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(ActivityItemDoc::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun fetchUpcomingEvents(): List<EventDoc> {
        val snap = db.collection(FirestorePaths.EVENTS).get().await()
        return snap.documents
            .mapNotNull { doc ->
                doc.toObject(EventDoc::class.java)?.copy(
                    id = doc.id,
                    startsAt = doc.readTimestamp("startsAt"),
                )
            }
            .sortedBy { it.startsAt?.toDate()?.time ?: Long.MAX_VALUE }
            .take(4)
    }

    suspend fun fetchRecruitingProjects(): List<ProjectDoc> {
        val ordered = runCatching {
            db.collection(FirestorePaths.PROJECTS)
                .whereEqualTo("visibility", ProjectVisibility.PUBLIC)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(24)
                .get()
                .await()
        }.getOrNull()
        val snap = ordered ?: db.collection(FirestorePaths.PROJECTS)
            .whereEqualTo("visibility", ProjectVisibility.PUBLIC)
            .limit(24)
            .get()
            .await()
        return snap.documents.mapNotNull { doc -> doc.toProjectDocSafe() }
            .filter { project -> project.lifecycleStatus != ProjectLifecycle.ARCHIVED }
            .sortedByDescending { project ->
                project.updatedAt?.toDate()?.time ?: project.createdAt?.toDate()?.time ?: 0L
            }
    }

    suspend fun fetchOwnedProjects(ownerUserId: String, limit: Long = 6): List<ProjectDoc> {
        val ordered = runCatching {
            db.collection(FirestorePaths.PROJECTS)
                .whereEqualTo("ownerUserId", ownerUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
        }.getOrNull()
        val snap = ordered ?: db.collection(FirestorePaths.PROJECTS)
            .whereEqualTo("ownerUserId", ownerUserId)
            .limit(limit)
            .get()
            .await()
        return snap.documents.mapNotNull { doc -> doc.toProjectDocSafe() }
            .sortedByDescending { project ->
                project.createdAt?.toDate()?.time ?: 0L
            }
    }

    suspend fun fetchPublicUsersExcluding(viewerUserId: String, limit: Long = 6): List<UserProfileDoc> {
        val snap = db.collection(FirestorePaths.USERS)
            .whereEqualTo("profileVisibility", "public")
            .limit(limit + 4)
            .get()
            .await()
        return snap.documents
            .mapNotNull { doc -> doc.toUserProfileDocSafe() }
            .filter { it.id != viewerUserId }
            .take(limit.toInt())
    }

    suspend fun fetchInboxActivity(userId: String, limit: Long = 6): List<ActivityItemDoc> {
        val snap = db.collection(FirestorePaths.INBOX)
            .whereEqualTo("recipientUserId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            val title = doc.getString("title").orEmpty()
            val body = doc.getString("body").orEmpty()
            ActivityItemDoc(
                id = doc.id,
                audienceUserId = userId,
                summary = listOf(title, body).filter { it.isNotBlank() }.joinToString(" — "),
                createdAt = doc.readTimestamp("createdAt"),
            )
        }
    }

    suspend fun fetchUserProfiles(userIds: Collection<String>): Map<String, UserProfileDoc> {
        if (userIds.isEmpty()) return emptyMap()
        val unique = userIds.distinct()
        return coroutineScope {
            unique.map { uid ->
                async {
                    val snap = db.collection(FirestorePaths.USERS).document(uid).get().await()
                    if (!snap.exists()) {
                        null
                    } else {
                        snap.toObject(UserProfileDoc::class.java)?.copy(id = snap.id)
                    }
                }
            }.awaitAll().filterNotNull().associateBy { it.id }
        }
    }
}

fun formatRelativeTime(timestamp: Timestamp?): String {
    if (timestamp == null) return "Recently"
    val now = System.currentTimeMillis()
    val then = timestamp.toDate().time
    val diffMs = (now - then).coerceAtLeast(0)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 60 * 24 -> "${minutes / 60}h ago"
        else -> "${minutes / (60 * 24)}d ago"
    }
}
