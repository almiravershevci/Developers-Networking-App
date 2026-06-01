package com.example.developernetworkingapp.domain.model

data class NotificationItem(
    val id: String,
    val body: String,
    val read: Boolean = false,
    val title: String = "",
    val notificationKind: String = "",
    val deepLink: String? = null,
    val relativeTime: String = "",
)

data class NotificationContent(
    val items: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
    val statusMessage: String? = null,
)
