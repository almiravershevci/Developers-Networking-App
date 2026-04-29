package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.ProfileRepository
import com.example.developernetworkingapp.di.AppContainer
import com.example.developernetworkingapp.ui.state.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository = AppContainer.profileRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProfile().collect { _uiState.value = ProfileUiState(it) }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
