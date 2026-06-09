package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.ProjectsRepository
import com.example.developernetworkingapp.data.repository.TasksRepository
import com.example.developernetworkingapp.ui.state.ProjectsUiState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectsViewModel(
    private val repository: ProjectsRepository,
    private val tasksRepository: TasksRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProjectsUiState())
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<ProjectsUiEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<ProjectsUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.observeProjects().collect { content ->
                _uiState.update { state ->
                    state.copy(
                        content = content,
                        isLoading = false,
                        displayContent = ProjectsBoardMapper.resolve(state.selectedProjectName, content),
                    )
                }
            }
        }
    }

    fun setSelectedProject(name: String) {
        _uiState.update { state ->
            state.copy(
                selectedProjectName = name,
                displayContent = ProjectsBoardMapper.resolve(name, state.content),
            )
        }
    }

    fun notifyInviteStarted() {
        emitEvent(ProjectsUiEvent.ShowNotification("Invitation flow opened. Select the best collaborator for your sprint."))
    }

    fun createProject(title: String, description: String, stackLabel: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingProject = true, createProjectError = null) }
            val result = repository.createProject(title, description, stackLabel)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isCreatingProject = false, createProjectError = null) }
                    emitEvent(ProjectsUiEvent.ProjectCreated)
                    emitEvent(ProjectsUiEvent.ShowNotification("Project \"$title\" created. Kanban board is ready."))
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isCreatingProject = false,
                            createProjectError = error.message ?: "Could not create project.",
                        )
                    }
                },
            )
        }
    }

    fun clearCreateProjectError() {
        _uiState.update { it.copy(createProjectError = null) }
    }

    fun createTask(title: String, priority: String, boardColumn: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingTask = true, createTaskError = null) }
            val result = tasksRepository.createTask(
                title = title,
                priority = priority,
                boardColumn = boardColumn,
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isCreatingTask = false, createTaskError = null) }
                    emitEvent(ProjectsUiEvent.TaskCreated)
                    emitEvent(ProjectsUiEvent.ShowNotification("Task \"$title\" added to your board."))
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isCreatingTask = false,
                            createTaskError = error.message ?: "Could not create task.",
                        )
                    }
                },
            )
        }
    }

    fun clearCreateTaskError() {
        _uiState.update { it.copy(createTaskError = null) }
    }

    private fun emitEvent(event: ProjectsUiEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}

sealed interface ProjectsUiEvent {
    data class ShowNotification(val message: String) : ProjectsUiEvent
    data object ProjectCreated : ProjectsUiEvent
    data object TaskCreated : ProjectsUiEvent
}
