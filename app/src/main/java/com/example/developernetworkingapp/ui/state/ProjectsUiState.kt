package com.example.developernetworkingapp.ui.state

import com.example.developernetworkingapp.domain.model.ProjectBoardContent
import com.example.developernetworkingapp.domain.model.ProjectJoinRequest

data class ProjectsUiState(
    val content: ProjectBoardContent? = null,
    val displayContent: ProjectBoardContent? = null,
    val selectedProjectName: String = "",
    val incomingProjectJoinRequests: List<ProjectJoinRequest> = emptyList(),
    val joinRequestLoadError: String? = null,
    val projectJoinActionInFlight: String? = null,
    val isLoading: Boolean = true,
    val isCreatingProject: Boolean = false,
    val createProjectError: String? = null,
    val isCreatingTask: Boolean = false,
    val createTaskError: String? = null,
)
