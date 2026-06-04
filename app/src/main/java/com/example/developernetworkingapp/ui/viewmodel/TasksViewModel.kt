package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.NotificationDispatcher
import com.example.developernetworkingapp.data.repository.TasksRepository
import com.example.developernetworkingapp.ui.state.TasksUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: TasksRepository,
    private val notificationDispatcher: NotificationDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeTasks().collect { _uiState.value = TasksUiState(it) }
        }
    }

    fun remindForTask(taskTitle: String) {
        notificationDispatcher.showLocalNotification(
            title = "Task reminder set",
            message = taskTitle.take(80)
        )
    }
}
