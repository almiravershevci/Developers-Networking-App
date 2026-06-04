package com.example.developernetworkingapp.ui.navigation

import androidx.lifecycle.ViewModel
import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.AuthUser
import com.example.developernetworkingapp.ui.event.AppNavEvent
import com.example.developernetworkingapp.ui.viewmodel.eventEmitter
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class AppNavigationViewModel(
    authRepository: AuthRepository,
) : ViewModel() {

    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser

    private val navEmitter = eventEmitter<AppNavEvent>()
    val navEvents: SharedFlow<AppNavEvent> = navEmitter.events

    private val unauthenticatedRoutes = setOf(AppRoutes.LOGIN, AppRoutes.SIGNUP)

    fun onRouteChanged(route: String?, isAuthenticated: Boolean) {
        when {
            !isAuthenticated && route != null && route !in unauthenticatedRoutes && !isVerificationRoute(route) ->
                navEmitter.emit(AppNavEvent.NavigateToLogin)
            isAuthenticated && route != null && route in unauthenticatedRoutes ->
                navEmitter.emit(AppNavEvent.NavigateToDashboard)
        }
    }

    private fun isVerificationRoute(route: String): Boolean =
        route.startsWith("verify/") || route == AppRoutes.VERIFY_EMAIL
}
