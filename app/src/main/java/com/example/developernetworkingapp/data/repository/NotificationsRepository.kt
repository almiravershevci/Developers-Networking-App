package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.NotificationContent
import com.example.developernetworkingapp.domain.model.NotificationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface NotificationsRepository {
    fun observeNotifications(): Flow<NotificationContent>

    suspend fun markAsRead(notificationId: String): Result<Unit>
}

class FakeNotificationsRepository : NotificationsRepository {
    private val itemsState = MutableStateFlow(seedItems())

    override fun observeNotifications(): Flow<NotificationContent> =
        itemsState.map { list ->
            NotificationContent(
                items = list,
                unreadCount = list.count { !it.read },
            )
        }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        itemsState.update { list ->
            list.map { item ->
                if (item.id == notificationId) item.copy(read = true) else item
            }
        }
        return Result.success(Unit)
    }

    private companion object {
        fun seedItems() = listOf(
            NotificationItem("n1", "Team Neon — Task moved to In Progress: Realtime chat UI", read = false),
            NotificationItem("n2", "New message — New message from Aria in API review room", read = false),
            NotificationItem("n3", "Hackathon — You were invited to hackathon: DevSprint Global", read = false),
            NotificationItem("n4", "New matches — 3 new collaborators match your Kotlin + Firebase stack", read = false),
            NotificationItem("n5", "Tech feed — Tech feed: Kotlin 2.x release notes are out", read = false),
        )
    }
}
