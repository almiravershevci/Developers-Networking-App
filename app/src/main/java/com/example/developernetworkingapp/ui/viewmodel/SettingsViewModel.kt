package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.developernetworkingapp.data.local.SettingsStore
import com.example.developernetworkingapp.ui.state.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel(
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(settingsStore.load())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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

    private fun updateAndPersist(transform: (SettingsUiState) -> SettingsUiState) {
        _uiState.update { current ->
            val next = transform(current)
            settingsStore.save(next)
            next
        }
    }
}
