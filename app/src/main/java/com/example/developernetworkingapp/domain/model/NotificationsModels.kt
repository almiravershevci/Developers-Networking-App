package com.example.developernetworkingapp.domain.model

data class NotificationItem(
    val id: String,
    val body: String,
    val read: Boolean = false
)

data class NotificationContent(
    val items: List<NotificationItem>
)
