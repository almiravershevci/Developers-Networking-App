package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.NotificationsRepository
import com.example.developernetworkingapp.ui.data.AppShellData
import com.example.developernetworkingapp.ui.data.NavTab
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.MainShellUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainShellViewModel(
    notificationsRepository: NotificationsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainShellUiState())
    val uiState: StateFlow<MainShellUiState> = _uiState.asStateFlow()

    val bottomTabs: List<NavTab> = AppShellData.bottomTabs

    init {
        viewModelScope.launch {
            notificationsRepository.observeNotifications().collect { content ->
                _uiState.update { it.copy(unreadNotificationCount = content.unreadCount) }
            }
        }
    }

    fun onRouteChanged(route: String) {
        val normalized = normalizeRoute(route)
        _uiState.update {
            it.copy(
                currentRoute = normalized,
                screenTitle = screenTitle(normalized),
                showBottomBar = normalized != AppRoutes.ADMIN_DASHBOARD,
            )
        }
    }

    private fun normalizeRoute(route: String): String = when {
        route.startsWith(AppRoutes.PROJECTS) -> AppRoutes.PROJECTS
        route.startsWith(AppRoutes.DASHBOARD) -> AppRoutes.DASHBOARD
        route.startsWith(AppRoutes.CHAT) -> AppRoutes.CHAT
        route.startsWith(AppRoutes.SEARCH) -> AppRoutes.SEARCH
        route.startsWith(AppRoutes.NOTIFICATIONS) -> AppRoutes.NOTIFICATIONS
        route.startsWith(AppRoutes.PROFILE) -> AppRoutes.PROFILE
        route.startsWith(AppRoutes.SETTINGS) -> AppRoutes.SETTINGS
        route.startsWith(AppRoutes.ADMIN_DASHBOARD) -> AppRoutes.ADMIN_DASHBOARD
        route.startsWith(AppRoutes.TASKS) -> AppRoutes.TASKS
        route.startsWith(AppRoutes.EVENTS) -> AppRoutes.EVENTS
        else -> route
    }

    private fun screenTitle(route: String): String = when (route) {
        AppRoutes.DASHBOARD -> "Developer Command Center"
        AppRoutes.PROJECTS -> "Project Collaboration"
        AppRoutes.CHAT -> "Realtime Chat"
        AppRoutes.SEARCH -> "Talent Search"
        AppRoutes.NOTIFICATIONS -> "Notifications"
        AppRoutes.PROFILE -> "Profile"
        AppRoutes.SETTINGS -> "Settings"
        AppRoutes.ADMIN_DASHBOARD -> "Admin Dashboard"
        AppRoutes.TASKS -> "Task Manager"
        AppRoutes.EVENTS -> "Hackathons & Events"
        else -> "Developer App"
    }
}
