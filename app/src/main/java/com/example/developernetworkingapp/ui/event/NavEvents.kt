package com.example.developernetworkingapp.ui.event

sealed interface AuthNavEvent {
    data object NavigateToDashboard : AuthNavEvent
    data class NavigateToVerifyEmail(val email: String) : AuthNavEvent
    data object NavigateToLogin : AuthNavEvent
}

sealed interface AppNavEvent {
    data object NavigateToLogin : AppNavEvent
    data object NavigateToDashboard : AppNavEvent
}

sealed interface ProfileNavEvent {
    data object LoggedOut : ProfileNavEvent
}
