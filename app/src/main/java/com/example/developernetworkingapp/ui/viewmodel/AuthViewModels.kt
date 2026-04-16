package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.developernetworkingapp.ui.state.LoginUiState
import com.example.developernetworkingapp.ui.state.SignupUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) = _uiState.update { it.copy(form = it.form.copy(email = value)) }
    fun updatePassword(value: String) = _uiState.update { it.copy(form = it.form.copy(password = value)) }
}

class SignupViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun updateName(value: String) = _uiState.update { it.copy(form = it.form.copy(name = value)) }
    fun updateUsername(value: String) = _uiState.update { it.copy(form = it.form.copy(username = value)) }
    fun updateEmail(value: String) = _uiState.update { it.copy(form = it.form.copy(email = value)) }
    fun updatePassword(value: String) = _uiState.update { it.copy(form = it.form.copy(password = value)) }
}
