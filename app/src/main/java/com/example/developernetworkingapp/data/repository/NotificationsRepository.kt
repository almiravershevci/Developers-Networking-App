package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.NotificationContent
import com.example.developernetworkingapp.domain.model.NotificationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface NotificationsRepository {
    fun observeNotifications(): Flow<NotificationContent>
    fun markAsRead(notificationId: String)
}

class FakeNotificationsRepository : NotificationsRepository {
    private val itemsState = MutableStateFlow(seedItems())

    override fun observeNotifications(): Flow<NotificationContent> =
        itemsState.map { NotificationContent(items = it) }

    override fun markAsRead(notificationId: String) {
        itemsState.update { list ->
            list.map { item ->
                if (item.id == notificationId) item.copy(read = true) else item
            }
        }
    }

    private companion object {
        fun seedItems() = listOf(
            NotificationItem("n1", "Team Neon moved task 'Realtime chat UI' to In Progress", read = false),
            NotificationItem("n2", "New message from Aria in API review room", read = false),
            NotificationItem("n3", "You were invited to hackathon: DevSprint Global", read = false),
            NotificationItem("n4", "3 new collaborators match your Kotlin + Firebase stack", read = false),
            NotificationItem("n5", "Tech feed: Kotlin 2.x release notes are out", read = false)
        )
    }
}
