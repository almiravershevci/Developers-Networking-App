package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.AuthResult
import com.example.developernetworkingapp.ui.event.AuthNavEvent
import com.example.developernetworkingapp.ui.state.LoginUiState
import com.example.developernetworkingapp.ui.state.SignupUiState
import com.example.developernetworkingapp.ui.state.VerificationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val navEmitter = eventEmitter<AuthNavEvent>()
    val navigationEvents: SharedFlow<AuthNavEvent> = navEmitter.events

    fun updateEmail(value: String) = _uiState.update {
        it.copy(form = it.form.copy(email = value), errorMessage = null, infoMessage = null)
    }

    fun updatePassword(value: String) = _uiState.update {
        it.copy(form = it.form.copy(password = value), errorMessage = null, infoMessage = null)
    }

    fun updateRememberMe(value: Boolean) = _uiState.update { it.copy(rememberMe = value) }

    fun login() {
        val state = _uiState.value
        val emailOrUsername = state.form.email.trim()
        val password = state.form.password

        if (emailOrUsername.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email/username and password are required.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            try {
                when (val result = authRepository.login(emailOrUsername, password, state.rememberMe)) {
                    is AuthResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = null, infoMessage = null) }
                        navEmitter.emit(AuthNavEvent.NavigateToDashboard)
                    }
                    is AuthResult.PendingEmailVerification -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                infoMessage = "Verification email sent to ${result.email}. Opening verify screen…",
                            )
                        }
                        navEmitter.emit(AuthNavEvent.NavigateToVerifyEmail(result.email))
                    }
                    is AuthResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message, infoMessage = null) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Login failed. Check your connection and try again.",
                        infoMessage = null,
                    )
                }
            }
        }
    }

    fun requestPasswordReset() {
        val identifier = _uiState.value.form.email
        viewModelScope.launch {
            when (val result = authRepository.requestPasswordReset(identifier)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = null,
                            infoMessage = "Password reset link sent to ${result.user.email}.",
                        )
                    }
                }
                is AuthResult.PendingEmailVerification -> Unit
                is AuthResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, infoMessage = null) }
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        val rememberMe = _uiState.value.rememberMe
        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(idToken, rememberMe)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null, infoMessage = null) }
                    navEmitter.emit(AuthNavEvent.NavigateToDashboard)
                }
                is AuthResult.PendingEmailVerification -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null, infoMessage = null) }
                    navEmitter.emit(AuthNavEvent.NavigateToVerifyEmail(result.email))
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun reportGoogleSignInError(message: String) {
        _uiState.update { it.copy(errorMessage = message, infoMessage = null) }
    }
}

class SignupViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    private val navEmitter = eventEmitter<AuthNavEvent>()
    val navigationEvents: SharedFlow<AuthNavEvent> = navEmitter.events

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

    fun signup() {
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
            when (
                val result = authRepository.signup(
                    name = form.name,
                    username = form.username,
                    email = form.email,
                    password = form.password,
                    rememberMe = state.rememberMe,
                )
            ) {
                is AuthResult.Success -> {
                    val email = form.email.trim().lowercase()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            successMessage = "Account created. Verify your email to continue.",
                        )
                    }
                    navEmitter.emit(AuthNavEvent.NavigateToVerifyEmail(email))
                }
                is AuthResult.PendingEmailVerification -> Unit
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

class VerificationViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    private val navEmitter = eventEmitter<AuthNavEvent>()
    val navigationEvents: SharedFlow<AuthNavEvent> = navEmitter.events

    fun setEmail(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                errorMessage = null,
                infoMessage = "A DevConnect verification link was sent to $email.",
            )
        }
    }

    fun updateCode(value: String) {
        _uiState.update { it.copy(code = value.filter { ch -> ch.isDigit() }.take(6), errorMessage = null) }
    }

    fun resendCode() {
        val email = _uiState.value.email
        viewModelScope.launch {
            when (val result = authRepository.requestEmailVerification(email)) {
                is AuthResult.Success -> _uiState.update {
                    it.copy(
                        infoMessage = "Verification email sent to $email. Open the link on this device, then tap Verify email.",
                        errorMessage = null,
                    )
                }
                is AuthResult.PendingEmailVerification -> Unit
                is AuthResult.Error -> _uiState.update { it.copy(errorMessage = result.message, infoMessage = null) }
            }
        }
    }

    fun verify() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.verifyEmailCode(state.email, state.code)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null, infoMessage = "Email verified.") }
                    navEmitter.emit(AuthNavEvent.NavigateToDashboard)
                }
                is AuthResult.PendingEmailVerification -> Unit
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
