package com.example.developernetworkingapp.domain.model

data class MatchRequest(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val fromDisplayName: String,
    val toDisplayName: String,
    val workflowStatus: String,
    val statusLabel: String,
    val message: String?,
    val relativeTime: String,
    val isIncoming: Boolean,
)
