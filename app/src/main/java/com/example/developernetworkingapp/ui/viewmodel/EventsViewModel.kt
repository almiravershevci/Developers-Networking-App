package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.EventsRepository
import com.example.developernetworkingapp.di.AppContainer
import com.example.developernetworkingapp.ui.state.EventsUiState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class EventsViewModel(
    private val repository: EventsRepository = AppContainer.eventsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<EventsUiEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<EventsUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.observeEvents().collect { _uiState.value = EventsUiState(it) }
        }
    }

    fun notifyEventJoined(eventTitle: String) {
        emitEvent(EventsUiEvent.ShowNotification("Joined $eventTitle. Redirecting you to chat room."))
    }

    private fun emitEvent(event: EventsUiEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}

sealed interface EventsUiEvent {
    data class ShowNotification(val message: String) : EventsUiEvent
}
