package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.NotificationDispatcher
import com.example.developernetworkingapp.data.repository.TasksRepository
import com.example.developernetworkingapp.data.datasource.firebase.schema.TaskBoardColumn
import com.example.developernetworkingapp.ui.state.TasksUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: TasksRepository,
    private val notificationDispatcher: NotificationDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeTasks().collect { content ->
                _uiState.update { current ->
                    current.copy(content = content, updatingTaskId = null)
                }
            }
        }
    }

    fun moveTaskToStatus(taskId: String, statusLabel: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(updatingTaskId = taskId, actionError = null) }
            repository.moveTask(taskId, statusLabelToBoardColumn(statusLabel))
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            updatingTaskId = null,
                            actionError = error.message ?: "Could not update task status.",
                        )
                    }
                }
        }
    }

    fun createTask(
        title: String,
        priority: String = "medium",
        boardColumn: String = "todo",
        assigneeUserId: String? = null,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingTask = true, createTaskError = null, actionError = null) }
            repository.createTask(
                title = title,
                priority = priority,
                boardColumn = boardColumn,
                assigneeUserId = assigneeUserId,
            )
                .onSuccess {
                    _uiState.update { it.copy(isCreatingTask = false, createTaskError = null) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isCreatingTask = false,
                            createTaskError = error.message ?: "Could not create task.",
                        )
                    }
                }
        }
    }

    fun clearCreateTaskError() {
        _uiState.update { it.copy(createTaskError = null) }
    }

    fun clearActionError() {
        _uiState.update { it.copy(actionError = null) }
    }

    fun remindForTask(taskTitle: String) {
        notificationDispatcher.showLocalNotification(
            title = "Task reminder set",
            message = taskTitle.take(80),
        )
    }

    private fun statusLabelToBoardColumn(statusLabel: String): String = when (statusLabel) {
        "To Do" -> TaskBoardColumn.TODO
        "In Progress" -> TaskBoardColumn.IN_PROGRESS
        "Done" -> TaskBoardColumn.DONE
        "Blocked" -> TaskBoardColumn.BLOCKED
        else -> TaskBoardColumn.TODO
    }
}
