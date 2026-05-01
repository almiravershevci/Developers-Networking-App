package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.ProjectsRepository
import com.example.developernetworkingapp.di.AppContainer
import com.example.developernetworkingapp.ui.state.ProjectsUiState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ProjectsViewModel(
    private val repository: ProjectsRepository = AppContainer.projectsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProjectsUiState())
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<ProjectsUiEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<ProjectsUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.observeProjects().collect { _uiState.value = ProjectsUiState(it) }
        }
    }

    fun notifyInviteStarted() {
        emitEvent(ProjectsUiEvent.ShowNotification("Invitation flow opened. Select the best collaborator for your sprint."))
    }

    private fun emitEvent(event: ProjectsUiEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}

sealed interface ProjectsUiEvent {
    data class ShowNotification(val message: String) : ProjectsUiEvent
}
