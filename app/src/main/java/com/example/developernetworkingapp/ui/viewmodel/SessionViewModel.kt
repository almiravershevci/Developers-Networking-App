package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.developernetworkingapp.data.repository.AuthRepository
class SessionViewModel(
    authRepository: AuthRepository,
) : ViewModel() {
    val currentUser = authRepository.currentUser
}
