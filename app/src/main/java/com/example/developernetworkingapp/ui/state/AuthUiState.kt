package com.example.developernetworkingapp.ui.state

import com.example.developernetworkingapp.domain.model.AuthForm

data class LoginUiState(
    val form: AuthForm = AuthForm(),
    val isLoading: Boolean = false
)

data class SignupUiState(
    val form: AuthForm = AuthForm(),
    val isLoading: Boolean = false
)
