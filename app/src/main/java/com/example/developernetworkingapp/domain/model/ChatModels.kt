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

object ChatQuickRooms {
    data class Room(val label: String, val conversationId: String)

    val rooms: List<Room> = listOf(
        Room("Project Room", "conv_team_neon"),
        Room("Mentorship", "conv_aria_api"),
        Room("Hackathon Team", "conv_hackathon_squad"),
        Room("General", "conv_design_crew"),
    )

    fun conversationIdForLabel(label: String): String? =
        rooms.firstOrNull { it.label == label }?.conversationId
}
