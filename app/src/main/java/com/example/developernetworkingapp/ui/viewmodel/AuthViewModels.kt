package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.AuthResult
import com.example.developernetworkingapp.ui.state.LoginUiState
import com.example.developernetworkingapp.ui.state.SignupUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) = _uiState.update {
        it.copy(form = it.form.copy(email = value), errorMessage = null, infoMessage = null)
    }
    fun updatePassword(value: String) = _uiState.update {
        it.copy(form = it.form.copy(password = value), errorMessage = null, infoMessage = null)
    }
    fun updateRememberMe(value: Boolean) = _uiState.update { it.copy(rememberMe = value) }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        val emailOrUsername = state.form.email.trim()
        val password = state.form.password

        if (emailOrUsername.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email/username and password are required.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            delay(600)
            when (val result = AuthRepository.login(emailOrUsername, password, state.rememberMe)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null, infoMessage = null) }
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun requestPasswordReset() {
        val identifier = _uiState.value.form.email
        when (val result = AuthRepository.requestPasswordReset(identifier)) {
            is AuthResult.Success -> {
                _uiState.update {
                    it.copy(
                        errorMessage = null,
                        infoMessage = "Password reset link sent to ${result.user.email}."
                    )
                }
            }
            is AuthResult.Error -> {
                _uiState.update { it.copy(errorMessage = result.message, infoMessage = null) }
            }
        }
    }
}

class SignupViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun updateName(value: String) = _uiState.update {
        it.copy(form = it.form.copy(name = value), errorMessage = null, successMessage = null)
    }
    fun updateUsername(value: String) = _uiState.update {
        it.copy(form = it.form.copy(username = value), errorMessage = null, successMessage = null)
    }
    fun updateEmail(value: String) = _uiState.update {
        it.copy(form = it.form.copy(email = value), errorMessage = null, successMessage = null)
    }
    fun updatePassword(value: String) = _uiState.update {
        it.copy(form = it.form.copy(password = value), errorMessage = null, successMessage = null)
    }
    fun updateConfirmPassword(value: String) = _uiState.update {
        it.copy(form = it.form.copy(confirmPassword = value), errorMessage = null, successMessage = null)
    }
    fun updateRememberMe(value: Boolean) = _uiState.update {
        it.copy(rememberMe = value, errorMessage = null, successMessage = null)
    }

    fun signup(onSuccess: () -> Unit) {
        val state = _uiState.value
        val form = state.form

        when {
            form.name.trim().length < 2 -> {
                _uiState.update { it.copy(errorMessage = "Please enter your full name.") }
                return
            }
            form.username.trim().length < 3 -> {
                _uiState.update { it.copy(errorMessage = "Username must be at least 3 characters.") }
                return
            }
            !form.email.contains("@") || !form.email.contains(".") -> {
                _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
                return
            }
            !isPasswordStrong(form.password) -> {
                _uiState.update {
                    it.copy(errorMessage = "Password must be 8+ chars with upper, lower, number, and symbol.")
                }
                return
            }
            form.password != form.confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
                return
            }
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            delay(700)
            when (
                val result = AuthRepository.signup(
                    name = form.name,
                    username = form.username,
                    email = form.email,
                    password = form.password,
                    rememberMe = state.rememberMe
                )
            ) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            successMessage = "Account created successfully. Redirecting..."
                        )
                    }
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun isPasswordStrong(password: String): Boolean {
        if (password.length < 8) return false
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }
        return hasUpper && hasLower && hasDigit && hasSymbol
    }
}
