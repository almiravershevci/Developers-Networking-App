package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.NotificationContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface NotificationsRepository {
    fun observeNotifications(): Flow<NotificationContent>
}

class FakeNotificationsRepository : NotificationsRepository {
    override fun observeNotifications(): Flow<NotificationContent> = flowOf(
        NotificationContent(
            items = listOf(
                "Team Neon moved task 'Realtime chat UI' to In Progress",
                "New message from Aria in API review room",
                "You were invited to hackathon: DevSprint Global",
                "3 new collaborators match your Kotlin + Firebase stack",
                "Tech feed: Kotlin 2.x release notes are out"
            )
        )
    )
}
