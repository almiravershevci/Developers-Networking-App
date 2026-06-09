package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.local.SettingsStore
import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.AuthResult
import com.example.developernetworkingapp.ui.event.SettingsNavEvent
import com.example.developernetworkingapp.ui.state.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsStore: SettingsStore,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(settingsStore.load())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val navEmitter = eventEmitter<SettingsNavEvent>()
    val navigationEvents: SharedFlow<SettingsNavEvent> = navEmitter.events

    fun setAppVersion(version: String) {
        updateAndPersist { it.copy(appVersion = version) }
    }

    fun setPushEnabled(enabled: Boolean) {
        updateAndPersist { it.copy(pushEnabled = enabled) }
    }

    fun setEmailDigests(enabled: Boolean) {
        updateAndPersist { it.copy(emailDigests = enabled) }
    }

    fun setProfilePublic(enabled: Boolean) {
        updateAndPersist { it.copy(profilePublic = enabled) }
    }

    fun setAnalyticsOptIn(enabled: Boolean) {
        updateAndPersist { it.copy(analyticsOptIn = enabled) }
    }

    fun clearDeleteAccountFeedback() {
        _uiState.update { it.copy(deleteAccountError = null, deleteAccountSuccess = false) }
    }

    fun deleteAccount(password: String) {
        _uiState.update {
            it.copy(
                isDeletingAccount = true,
                deleteAccountError = null,
                deleteAccountSuccess = false,
            )
        }
        viewModelScope.launch {
            when (val result = authRepository.deleteAccount(password = password.takeIf { it.isNotBlank() })) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(isDeletingAccount = false, deleteAccountSuccess = true)
                    }
                    navEmitter.emit(SettingsNavEvent.AccountDeleted)
                }
                is AuthResult.PendingEmailVerification -> Unit
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isDeletingAccount = false, deleteAccountError = result.message)
                    }
                }
            }
        }
    }

    private fun updateAndPersist(transform: (SettingsUiState) -> SettingsUiState) {
        _uiState.update { current ->
            val next = transform(current)
            settingsStore.save(next)
            next
        }
    }
}
