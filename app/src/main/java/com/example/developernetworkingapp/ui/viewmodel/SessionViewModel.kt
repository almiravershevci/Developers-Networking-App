package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.di.AppContainer

class SessionViewModel(
    authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {
    val currentUser = authRepository.currentUser
}
