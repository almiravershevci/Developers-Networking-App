package com.example.developernetworkingapp.ui.state

import com.example.developernetworkingapp.domain.model.AuthForm

data class LoginUiState(
    val form: AuthForm = AuthForm(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val rememberMe: Boolean = true
)

data class SignupUiState(
    val form: AuthForm = AuthForm(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val rememberMe: Boolean = true
)
