package com.example.developernetworkingapp.domain.model

data class EventContent(
    /** Display lines for the events list (title + summary). */
    val items: List<String> = emptyList(),
    /** Parallel Firestore document ids (same order as [items]). */
    val eventIds: List<String> = emptyList(),
    val statusMessage: String? = null,
)
