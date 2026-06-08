package com.example.developernetworkingapp.domain.model

data class EventItem(
    val id: String,
    val displayLine: String,
    val isRegistered: Boolean = false,
)

data class EventContent(
    val items: List<EventItem> = emptyList(),
    val statusMessage: String? = null,
)
