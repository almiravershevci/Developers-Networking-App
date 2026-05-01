package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.ProfileRepository
import com.example.developernetworkingapp.di.AppContainer
import com.example.developernetworkingapp.ui.state.ProfileUiState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository = AppContainer.profileRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<ProfileUiEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<ProfileUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.observeProfile().collect { _uiState.value = ProfileUiState(it) }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun notifyProfileSaved() {
        emitEvent(ProfileUiEvent.ShowNotification("Profile updates saved locally."))
    }

    fun notifySyncStarted() {
        emitEvent(ProfileUiEvent.ShowNotification("GitHub sync started. Pulling latest activity."))
    }

    private fun emitEvent(event: ProfileUiEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}

sealed interface ProfileUiEvent {
    data class ShowNotification(val message: String) : ProfileUiEvent
}
