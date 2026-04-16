package com.example.developernetworkingapp.ui.state

import com.example.developernetworkingapp.domain.model.DashboardContent

data class DashboardUiState(
    val isLoading: Boolean = true,
    val content: DashboardContent? = null,
    val errorMessage: String? = null
)
