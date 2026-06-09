package com.example.developernetworkingapp.ui.state

data class SettingsUiState(
    val pushEnabled: Boolean = true,
    val emailDigests: Boolean = true,
    val profilePublic: Boolean = true,
    val analyticsOptIn: Boolean = false,
    val appVersion: String = "",
    val isDeletingAccount: Boolean = false,
    val deleteAccountError: String? = null,
    val deleteAccountSuccess: Boolean = false,
)
