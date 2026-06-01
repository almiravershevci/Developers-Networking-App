package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.repository.NotificationsRepository
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreNotificationsDataSource
import com.example.developernetworkingapp.data.datasource.firebase.formatRelativeTime
import com.example.developernetworkingapp.data.datasource.firebase.authStateChanges
import com.example.developernetworkingapp.data.datasource.firebase.schema.InboxNotificationDoc
import com.example.developernetworkingapp.domain.model.NotificationContent
import com.example.developernetworkingapp.domain.model.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Inbox notifications backed by Firestore (per-user [recipientUserId]).
 */
class NotificationsRepositoryFirestore(
    private val dataSource: FirestoreNotificationsDataSource = FirestoreNotificationsDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : NotificationsRepository {

    override fun observeNotifications(): Flow<NotificationContent> =
        firebaseAuth.authStateChanges().flatMapLatest { firebaseUser ->
            when {
                firebaseUser == null -> flow {
                    emit(
                        NotificationContent(
                            statusMessage = "Sign in to view your notification inbox.",
                        ),
                    )
                }
                !firebaseUser.isEmailVerified -> flow {
                    emit(
                        NotificationContent(
                            statusMessage = "Verify your email to load notifications.",
                        ),
                    )
                }
                else -> dataSource.observeInboxForUser(firebaseUser.uid)
                    .map { docs -> buildContent(docs, firebaseUser.uid) }
                    .catch { error ->
                        emit(inboxErrorContent(error))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun markAsRead(notificationId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val uid = firebaseAuth.currentUser?.uid
                ?: return@withContext Result.failure(IllegalStateException("Sign in required"))
            runCatching {
                dataSource.markNotificationRead(notificationId)
            }.fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { error ->
                    Result.failure(
                        error.takeIf { it.message?.contains("PERMISSION_DENIED") != true }
                            ?: IllegalStateException(
                                "Cannot mark read. Notification must belong to $uid.",
                                error,
                            ),
                    )
                },
            )
        }

    private fun buildContent(docs: List<InboxNotificationDoc>, currentUserId: String): NotificationContent {
        if (docs.isEmpty()) {
            return NotificationContent(
                statusMessage = "No notifications yet. Seed inbox docs with recipientUserId=$currentUserId " +
                    "or run firestore/add-inbox-for-user.mjs.",
            )
        }
        return NotificationContent(
            items = docs.map { it.toNotificationItem() },
            unreadCount = docs.count { !it.read },
        )
    }

    private fun inboxErrorContent(error: Throwable): NotificationContent {
        val detail = error.message.orEmpty()
        val isPermission = detail.contains("PERMISSION_DENIED", ignoreCase = true)
        val isIndex = detail.contains("FAILED_PRECONDITION", ignoreCase = true) ||
            detail.contains("index", ignoreCase = true)
        val message = when {
            isIndex -> "Firestore needs an inbox index. Deploy firestore.indexes.json, then retry."
            isPermission -> "Inbox blocked by rules. Publish firestore.rules and set recipientUserId to your Auth UID."
            else -> "Couldn't load notifications. ($detail)"
        }
        return NotificationContent(statusMessage = message)
    }

    private fun InboxNotificationDoc.toNotificationItem(): NotificationItem {
        val displayBody = when {
            title.isNotBlank() && body.isNotBlank() -> "$title — $body"
            body.isNotBlank() -> body
            else -> title.ifBlank { "Notification" }
        }
        return NotificationItem(
            id = id,
            body = displayBody,
            read = read,
            title = title,
            notificationKind = notificationKind,
            deepLink = deepLink,
            relativeTime = formatRelativeTime(createdAt),
        )
    }
}
