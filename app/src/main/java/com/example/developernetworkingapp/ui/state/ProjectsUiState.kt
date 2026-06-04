package com.example.developernetworkingapp.ui.state

import com.example.developernetworkingapp.domain.model.ProjectBoardContent

data class ProjectsUiState(
    val content: ProjectBoardContent? = null,
    val displayContent: ProjectBoardContent? = null,
    val selectedProjectName: String = "",
    val isLoading: Boolean = true,
)
