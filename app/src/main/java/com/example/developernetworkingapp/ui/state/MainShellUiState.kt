package com.example.developernetworkingapp.ui.state

import com.example.developernetworkingapp.ui.navigation.AppRoutes

data class MainShellUiState(
    val currentRoute: String = AppRoutes.DASHBOARD,
    val screenTitle: String = "Developer Command Center",
    val unreadNotificationCount: Int = 0,
    val showBottomBar: Boolean = true,
)
