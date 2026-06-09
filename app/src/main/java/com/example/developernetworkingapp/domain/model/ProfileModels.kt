package com.example.developernetworkingapp.domain.model

data class ProfileContent(
    val name: String,
    val role: String,
    val bio: String,
    val stacks: List<String>,
    val portfolio: String,
    val insights: String,
    val statsLine: String = "",
    val activeProjectsCount: Int = 0,
    val collaborationsCount: Int = 0,
    val unreadMessagesCount: Int = 0,
    val openTasksCount: Int = 0,
    val activityItems: List<ActivityItem> = emptyList(),
)
