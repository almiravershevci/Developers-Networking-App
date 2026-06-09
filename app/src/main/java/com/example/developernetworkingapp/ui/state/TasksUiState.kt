package com.example.developernetworkingapp.ui.state

import com.example.developernetworkingapp.domain.model.ProjectMemberSummary
import com.example.developernetworkingapp.domain.model.TaskContent

data class TasksUiState(
    val content: TaskContent? = null,
    val projectId: String = "",
    val isProjectOwner: Boolean = false,
    val assignableMembers: List<ProjectMemberSummary> = emptyList(),
    val updatingTaskId: String? = null,
    val actionError: String? = null,
    val isCreatingTask: Boolean = false,
    val createTaskError: String? = null,
)
