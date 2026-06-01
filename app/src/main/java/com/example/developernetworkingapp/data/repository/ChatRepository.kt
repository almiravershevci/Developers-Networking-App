package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.ChatContent
import com.example.developernetworkingapp.domain.model.ConversationSummary
import com.example.developernetworkingapp.domain.model.ConversationThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ChatRepository {
    fun observeChat(): Flow<ChatContent>

    fun observeConversation(conversationId: String): Flow<ConversationThread>

    suspend fun sendMessage(conversationId: String, body: String): Result<Unit>
}

class FakeChatRepository : ChatRepository {
    private val demoInbox = listOf(
        ConversationSummary(
            id = "conv_aria_api",
            title = "Aria - API contract review",
            preview = "Pagination cursor landed in staging — ship tomorrow?",
            relativeTime = "2h ago",
            conversationKind = "direct",
            unreadCount = 1,
            participantCount = 2,
        ),
        ConversationSummary(
            id = "conv_team_neon",
            title = "Team Neon - Sprint planning thread",
            preview = "Let's align on realtime chat scope for this sprint.",
            relativeTime = "1h ago",
            conversationKind = "group",
            unreadCount = 2,
            participantCount = 2,
            projectId = "proj_devconnect_mobile",
        ),
        ConversationSummary(
            id = "conv_hackathon_squad",
            title = "Hackathon Squad - Demo prep",
            preview = "Demo checklist: auth flow, realtime chat, tasks board — green?",
            relativeTime = "45m ago",
            conversationKind = "group",
            unreadCount = 0,
            participantCount = 3,
        ),
        ConversationSummary(
            id = "conv_design_crew",
            title = "Design Crew - Portfolio showcase ideas",
            preview = "Motion spec for the showcase carousel — Lina attached Figma link.",
            relativeTime = "3h ago",
            conversationKind = "group",
            unreadCount = 0,
            participantCount = 3,
        ),
    )

    override fun observeChat(): Flow<ChatContent> = flowOf(
        ChatContent(
            inbox = demoInbox,
            conversations = demoInbox.map { it.title },
            composerHint = "Type a message...",
            isSignedIn = true,
        ),
    )

    override fun observeConversation(conversationId: String): Flow<ConversationThread> = flowOf(
        ConversationThread(
            conversationId = conversationId,
            title = demoInbox.firstOrNull { it.id == conversationId }?.title ?: "Conversation",
            subtitle = "Offline demo thread",
            messages = listOf(
                com.example.developernetworkingapp.domain.model.ChatMessage(
                    id = "1",
                    body = "Hey — quick sync on the latest API draft when you have a moment.",
                    fromSelf = false,
                    senderLabel = "Aria",
                    messageKind = "text",
                ),
                com.example.developernetworkingapp.domain.model.ChatMessage(
                    id = "2",
                    body = "On it. I'll drop comments on the contract section and ping you before EOD.",
                    fromSelf = true,
                    senderLabel = "You",
                    messageKind = "text",
                ),
            ),
            isLoading = false,
        ),
    )

    override suspend fun sendMessage(conversationId: String, body: String): Result<Unit> =
        Result.success(Unit)
}
