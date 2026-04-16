package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.ChatContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ChatRepository {
    fun observeChat(): Flow<ChatContent>
}

class FakeChatRepository : ChatRepository {
    override fun observeChat(): Flow<ChatContent> = flowOf(
        ChatContent(
            conversations = listOf(
                "Aria - API contract review",
                "Team Neon - Sprint planning thread",
                "Hackathon Squad - Demo prep",
                "Design Crew - Portfolio showcase ideas"
            ),
            composerHint = "Type a message..."
        )
    )
}
