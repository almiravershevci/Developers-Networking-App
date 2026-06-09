package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.ProjectsRepository
import com.example.developernetworkingapp.data.repository.DashboardRepository
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
    private val dashboardRepository: DashboardRepository,
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
            try {
                val result = repository.createProject(title, description, stackLabel)
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isCreatingProject = false, createProjectError = null) }
                        repository.invalidateProjects()
                        dashboardRepository.invalidateDashboard()
                        emitEvent(ProjectsUiEvent.ProjectCreated)
                        emitEvent(ProjectsUiEvent.ShowNotification("Project \"$title\" created. Kanban board is ready."))
                    },
                    onFailure = { error ->
                        val message = error.message ?: "Could not create project."
                        _uiState.update { it.copy(isCreatingProject = false, createProjectError = message) }
                        emitEvent(ProjectsUiEvent.ShowNotification(message))
                    },
                )
            } finally {
                _uiState.update { it.copy(isCreatingProject = false) }
            }
        }
    }

    fun clearCreateProjectError() {
        _uiState.update { it.copy(createProjectError = null) }
    }

    fun createTask(
        title: String,
        priority: String,
        boardColumn: String,
        assigneeUserId: String? = null,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingTask = true, createTaskError = null) }
            val projectId = _uiState.value.content?.projectId
            val result = tasksRepository.createTask(
                title = title,
                priority = priority,
                boardColumn = boardColumn,
                assigneeUserId = assigneeUserId,
                projectId = projectId,
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isCreatingTask = false, createTaskError = null) }
                    emitEvent(ProjectsUiEvent.TaskCreated)
                    val assigneeName = _uiState.value.content?.members
                        ?.firstOrNull { it.userId == assigneeUserId }
                        ?.displayName
                    val message = if (assigneeName != null) {
                        "Task \"$title\" assigned to $assigneeName."
                    } else {
                        "Task \"$title\" added to your board."
                    }
                    emitEvent(ProjectsUiEvent.ShowNotification(message))
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
