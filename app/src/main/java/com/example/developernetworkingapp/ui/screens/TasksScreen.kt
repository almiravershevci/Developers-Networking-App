package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.developernetworkingapp.di.appViewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.domain.model.TaskItem
import com.example.developernetworkingapp.ui.components.CreateTaskDialog
import com.example.developernetworkingapp.ui.components.EmptyStateCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.TasksUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.TasksViewModel

@Composable
fun TaskManagementRoute(padding: PaddingValues, navController: NavController) {
    val viewModel: TasksViewModel = appViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TaskManagementScreen(
        padding = padding,
        state = state,
        navController = navController,
        onMoveTask = viewModel::moveTaskToStatus,
        onCreateTask = { title, priority, boardColumn, assigneeUserId ->
            viewModel.createTask(title, priority, boardColumn, assigneeUserId)
        },
        onClearError = viewModel::clearActionError,
        onClearCreateTaskError = viewModel::clearCreateTaskError,
    )
}

@Composable
fun TaskManagementScreen(
    padding: PaddingValues,
    state: TasksUiState,
    navController: NavController,
    onMoveTask: (taskId: String, statusLabel: String) -> Unit = { _, _ -> },
    onCreateTask: (title: String, priority: String, boardColumn: String, assigneeUserId: String?) -> Unit = { _, _, _, _ -> },
    onClearError: () -> Unit = {},
    onClearCreateTaskError: () -> Unit = {},
) {
    var selectedTask by remember { mutableStateOf<TaskItem?>(null) }
    var showCreateTaskDialog by rememberSaveable { mutableStateOf(false) }
    var taskSubmitRequested by remember { mutableStateOf(false) }

    LaunchedEffect(state.isCreatingTask, state.createTaskError, taskSubmitRequested) {
        if (taskSubmitRequested && !state.isCreatingTask) {
            if (state.createTaskError == null) {
                showCreateTaskDialog = false
            }
            taskSubmitRequested = false
        }
    }

    if (showCreateTaskDialog) {
        CreateTaskDialog(
            isSubmitting = state.isCreatingTask,
            errorMessage = state.createTaskError,
            assigneeOptions = state.assignableMembers,
            isOwner = state.isProjectOwner,
            onDismiss = {
                showCreateTaskDialog = false
                onClearCreateTaskError()
            },
            onCreate = { title, priority, boardColumn, assigneeUserId ->
                taskSubmitRequested = true
                onCreateTask(title, priority, boardColumn, assigneeUserId)
            },
        )
    }

    state.actionError?.let { error ->
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text("Task update failed") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = onClearError) { Text("OK") }
            },
        )
    }

    selectedTask?.let { task ->
        AlertDialog(
            onDismissRequest = { selectedTask = null },
            title = { Text("Task Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task.displayLine, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Assignee: ${task.assigneeLabel} · Priority: ${task.priority.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedTask = null
                    navController.navigate(AppRoutes.projectsRoute(taskLinkedProject(task.title)))
                }) { Text("Open Project") }
            },
            dismissButton = { TextButton(onClick = { selectedTask = null }) { Text("Close") } },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            )
            .padding(padding)
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding,
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("Task Management")
                if (state.isProjectOwner) {
                    Button(onClick = { showCreateTaskDialog = true }) {
                        Text("Create task")
                    }
                }
            }
        }
        val statusMessage = state.content?.statusMessage
        if (!statusMessage.isNullOrBlank()) {
            item {
                Text(
                    statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val taskItems = state.content?.items.orEmpty()
        if (taskItems.isEmpty() && statusMessage.isNullOrBlank()) {
            item {
                EmptyStateCard(
                    title = "No tasks yet",
                    subtitle = "Tasks from your active project will sync here. Create one to get started.",
                    actionLabel = "Create task",
                    onAction = { showCreateTaskDialog = true },
                )
            }
        }
        items(taskItems, key = { it.id }) { task ->
            val isUpdating = state.updatingTaskId == task.id
            ElevatedCard(
                onClick = { selectedTask = task },
                shape = AppDesignTokens.cardShape,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(
                    modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(task.displayLine, style = MaterialTheme.typography.titleMedium)
                    if (isUpdating) {
                        Text(
                            "Saving…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { onMoveTask(task.id, "In Progress") },
                            label = { Text("In Progress") },
                            enabled = !isUpdating,
                        )
                        AssistChip(
                            onClick = { onMoveTask(task.id, "To Do") },
                            label = { Text("To Do") },
                            enabled = !isUpdating,
                        )
                        AssistChip(
                            onClick = { onMoveTask(task.id, "Done") },
                            label = { Text("Done") },
                            enabled = !isUpdating,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { selectedTask = task }) { Text("View details") }
                    }
                }
            }
        }
    }
}

private fun taskLinkedProject(taskTitle: String): String {
    val normalized = taskTitle.lowercase()
    return when {
        "api" in normalized || "backend" in normalized -> "Talent Graph API"
        else -> "DevConnect Mobile"
    }
}
