package com.example.developernetworkingapp.domain.model

data class ProjectMemberSummary(
    val userId: String,
    val displayName: String,
    val role: String,
)

data class ProjectBoardContent(
    val projectId: String = "",
    val teamName: String,
    val teamMeta: String,
    val isOwner: Boolean = false,
    val members: List<ProjectMemberSummary> = emptyList(),
    val todo: List<String>,
    val inProgress: List<String>,
    val done: List<String>,
)
