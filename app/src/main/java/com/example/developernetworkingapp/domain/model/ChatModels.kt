package com.example.developernetworkingapp.domain.model

data class ConversationSummary(
    val id: String,
    val title: String,
    val preview: String,
    val relativeTime: String,
    val conversationKind: String,
    val unreadCount: Int,
    val participantCount: Int,
    val projectId: String? = null,
)

data class ChatMessage(
    val id: String,
    val body: String,
    val fromSelf: Boolean,
    val senderLabel: String,
    val messageKind: String,
)

data class ConversationThread(
    val conversationId: String,
    val title: String,
    val subtitle: String,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val composerHint: String = "Message…",
)

data class ChatContent(
    /** Titles only — kept for mute keys and legacy callers. */
    val conversations: List<String> = emptyList(),
    val inbox: List<ConversationSummary> = emptyList(),
    val composerHint: String = "Type a message...",
    val statusMessage: String? = null,
    val isSignedIn: Boolean = false,
)

object ChatMessageKinds {
    const val TEXT = "text"
    const val SYSTEM = "system"
    const val MENTION = "mention"
}

fun chatMentionPrefix(messageKind: String): String? = when (messageKind) {
    ChatMessageKinds.MENTION -> "@"
    ChatMessageKinds.SYSTEM -> "•"
    else -> null
}
