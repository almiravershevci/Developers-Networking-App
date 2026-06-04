package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.ProfileRepository
import com.example.developernetworkingapp.ui.event.ProfileNavEvent
import com.example.developernetworkingapp.ui.state.ProfileUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val uiEmitter = eventEmitter<ProfileUiEvent>()
    val events: SharedFlow<ProfileUiEvent> = uiEmitter.events

    private val navEmitter = eventEmitter<ProfileNavEvent>()
    val navigationEvents: SharedFlow<ProfileNavEvent> = navEmitter.events

    init {
        viewModelScope.launch {
            repository.observeProfile().collect { _uiState.value = ProfileUiState(it) }
        }
    }

    fun logout() {
        authRepository.logout()
        navEmitter.emit(ProfileNavEvent.LoggedOut)
    }

    fun saveProfile(displayName: String, headline: String, bio: String) {
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                repository.updateProfile(displayName, headline, bio)
            }
            uiEmitter.emit(
                ProfileUiEvent.ShowNotification(
                    if (saved) "Profile saved to Firestore." else "Could not save profile. Try again.",
                ),
            )
        }
    }

    fun notifySyncStarted() {
        uiEmitter.emit(ProfileUiEvent.ShowNotification("GitHub sync started. Pulling latest activity."))
    }
}

sealed interface ProfileUiEvent {
    data class ShowNotification(val message: String) : ProfileUiEvent
}
