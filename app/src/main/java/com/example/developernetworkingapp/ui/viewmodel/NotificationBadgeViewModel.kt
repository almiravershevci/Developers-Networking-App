package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.NotificationsRepository
import com.example.developernetworkingapp.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Activity-scoped unread inbox count for bottom-nav and app-bar badges.
 * Starts listening as soon as the user enters the main shell (after login).
 */
class NotificationBadgeViewModel(
    private val repository: NotificationsRepository = AppContainer.notificationsRepository,
) : ViewModel() {
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeNotifications().collect { content ->
                _unreadCount.value = content.unreadCount
            }
        }
    }
}
