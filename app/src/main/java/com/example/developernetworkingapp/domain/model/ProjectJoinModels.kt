package com.example.developernetworkingapp.domain.model

enum class ProjectJoinStatus {
    AVAILABLE,
    PENDING,
    MEMBER,
    OWNER,
}

data class ProjectJoinRequest(
    val id: String,
    val projectId: String,
    val projectTitle: String,
    val fromUserId: String,
    val toUserId: String,
    val fromDisplayName: String,
    val requestedRole: String,
    val workflowStatus: String,
    val statusLabel: String,
    val message: String?,
    val relativeTime: String,
    val isIncoming: Boolean,
)
