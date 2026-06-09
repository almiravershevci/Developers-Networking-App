package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.repository.ChatRepository
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreChatDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.formatRelativeTime
import com.example.developernetworkingapp.data.datasource.firebase.authStateChanges
import com.example.developernetworkingapp.data.datasource.firebase.schema.ConversationDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ConversationKind
import com.example.developernetworkingapp.data.datasource.firebase.schema.MessageDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.MessageKind
import com.example.developernetworkingapp.domain.model.ChatContent
import com.example.developernetworkingapp.domain.model.ChatMessage
import com.example.developernetworkingapp.domain.model.ConversationSummary
import com.example.developernetworkingapp.domain.model.ConversationThread
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
 * Chat repository: realtime inbox and thread messages backed by Firestore.
 */
class ChatRepositoryFirestore(
    private val chatDataSource: FirestoreChatDataSource = FirestoreChatDataSource(),
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ChatRepository {

    override fun observeChat(): Flow<ChatContent> =
        firebaseAuth.authStateChanges().flatMapLatest { firebaseUser ->
            when {
                firebaseUser == null -> flowOfSignedOut()
                !firebaseUser.isEmailVerified -> flowOfNeedsVerification()
                else -> chatDataSource.observeConversationsForUser(firebaseUser.uid)
                    .flatMapLatest { docs ->
                        flow {
                            val content = runCatching {
                                buildChatContent(uid = firebaseUser.uid, conversations = docs)
                            }.getOrElse { error ->
                                chatLoadErrorContent(error, partialCount = docs.size)
                            }
                            emit(content)
                        }
                    }
                    .catch { error ->
                        emit(chatLoadErrorContent(error, partialCount = 0))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun observeConversation(conversationId: String): Flow<ConversationThread> =
        firebaseAuth.authStateChanges().flatMapLatest { firebaseUser ->
            when {
                firebaseUser == null -> flow {
                    emit(
                        ConversationThread(
                            conversationId = conversationId,
                            title = "Conversation",
                            subtitle = "Sign in to view messages",
                            isLoading = false,
                            errorMessage = "Sign in required",
                        ),
                    )
                }
                !firebaseUser.isEmailVerified -> flow {
                    emit(
                        ConversationThread(
                            conversationId = conversationId,
                            title = "Conversation",
                            subtitle = "Verify your email to use chat",
                            isLoading = false,
                            errorMessage = "Email verification required",
                        ),
                    )
                }
                else -> chatDataSource.observeMessages(conversationId).flatMapLatest { messages ->
                    flow {
                        val conversation = runCatching {
                            chatDataSource.fetchConversation(conversationId)
                        }.getOrNull()
                        val senderIds = messages.map { it.senderId }.distinct()
                        val profiles = runCatching {
                            userDataSource.fetchUserProfiles(senderIds)
                        }.getOrDefault(emptyMap())

                        runCatching {
                            chatDataSource.markMessagesRead(conversationId, firebaseUser.uid, messages)
                        }

                        emit(
                            ConversationThread(
                                conversationId = conversationId,
                                title = conversation?.title?.takeIf { it.isNotBlank() } ?: "Conversation",
                                subtitle = threadSubtitle(conversation, messages.size),
                                messages = messages.map { message ->
                                    toChatMessage(
                                        message = message,
                                        currentUserId = firebaseUser.uid,
                                        senderLabel = profiles[message.senderId]?.displayName
                                            ?: message.senderId.take(8),
                                    )
                                },
                                isLoading = false,
                                errorMessage = null,
                            ),
                        )
                    }
                }.catch { error ->
                    emit(
                        ConversationThread(
                            conversationId = conversationId,
                            title = "Conversation",
                            subtitle = "Realtime thread",
                            isLoading = false,
                            errorMessage = error.message ?: "Unable to load messages",
                        ),
                    )
                }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun sendMessage(conversationId: String, body: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val uid = firebaseAuth.currentUser?.uid
                ?: return@withContext Result.failure(IllegalStateException("Sign in required"))
            if (!firebaseAuth.currentUser!!.isEmailVerified) {
                return@withContext Result.failure(IllegalStateException("Verify your email to send messages"))
            }
            if (body.trim().isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Message cannot be empty"))
            }
            runCatching {
                chatDataSource.sendTextMessage(conversationId, uid, body)
            }
        }

    private fun flowOfSignedOut(): Flow<ChatContent> = flow {
        emit(
            ChatContent(
                statusMessage = "Sign in to open your collaboration inbox",
                composerHint = "Sign in to message teammates",
                isSignedIn = false,
            ),
        )
    }

    private fun flowOfNeedsVerification(): Flow<ChatContent> = flow {
        emit(
            ChatContent(
                statusMessage = "Verify your email to access Firestore conversations",
                composerHint = "Complete verification to chat",
                isSignedIn = true,
            ),
        )
    }

    private suspend fun buildChatContent(
        uid: String,
        conversations: List<ConversationDoc>,
    ): ChatContent {
        if (conversations.isEmpty()) {
            return ChatContent(
                inbox = emptyList(),
                conversations = emptyList(),
                statusMessage = "No conversations yet. Add your user id to a thread's participantIds in Firestore, or start a new chat.",
                composerHint = "Type a message...",
                isSignedIn = true,
            )
        }

        val sorted = conversations.sortedByDescending { doc ->
            doc.lastMessageAt?.toDate()?.time ?: doc.createdAt?.toDate()?.time ?: 0L
        }
        val inbox = sorted.map { doc -> toSummary(doc, uid) }
        return ChatContent(
            inbox = inbox,
            conversations = inbox.map { it.title },
            composerHint = "Type a message...",
            isSignedIn = true,
        )
    }

    private suspend fun toSummary(doc: ConversationDoc, currentUserId: String): ConversationSummary {
        val unread = runCatching {
            chatDataSource.fetchMessagesOnce(doc.id)
                .count { currentUserId !in it.readByUserIds }
        }.getOrDefault(0)

        return ConversationSummary(
            id = doc.id,
            title = doc.title?.takeIf { it.isNotBlank() } ?: "Conversation",
            preview = doc.lastMessagePreview?.takeIf { it.isNotBlank() } ?: "No messages yet",
            relativeTime = formatRelativeTime(doc.lastMessageAt ?: doc.createdAt),
            conversationKind = doc.conversationKind,
            unreadCount = unread,
            participantCount = doc.participantIds.size,
            projectId = doc.projectId,
        )
    }

    private fun threadSubtitle(conversation: ConversationDoc?, messageCount: Int): String {
        val kind = when (conversation?.conversationKind) {
            ConversationKind.GROUP -> "Group thread"
            ConversationKind.PROJECT_THREAD -> "Project room"
            else -> "Direct message"
        }
        val participants = conversation?.participantIds?.size ?: 0
        return "$kind · $participants members · $messageCount messages · Live"
    }

    private fun toChatMessage(
        message: MessageDoc,
        currentUserId: String,
        senderLabel: String,
    ): ChatMessage = ChatMessage(
        id = message.id,
        body = message.body,
        fromSelf = message.senderId == currentUserId,
        senderLabel = if (message.senderId == currentUserId) "You" else senderLabel,
        messageKind = message.messageKind,
    )

    private fun chatLoadErrorContent(error: Throwable, partialCount: Int): ChatContent {
        val detail = error.message.orEmpty()
        val isPermission = detail.contains("PERMISSION_DENIED", ignoreCase = true)
        val message = when {
            isPermission -> "Firestore blocked the inbox query. Publish the latest firestore.rules " +
                "(isConversationMember fix), then add your Auth UID to participantIds on each conversation."
            partialCount > 0 -> "Loaded $partialCount conversations but failed to build the inbox. $detail"
            else -> "Couldn't load conversations. Publish firestore.rules, add your UID to participantIds, " +
                "then restart the app. ($detail)"
        }
        return ChatContent(
            statusMessage = message,
            composerHint = "Type a message...",
            isSignedIn = true,
        )
    }

    companion object {
        fun mentionPrefix(kind: String): String? = when (kind) {
            MessageKind.MENTION -> "@"
            MessageKind.SYSTEM -> "•"
            else -> null
        }
    }
}
