package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.developernetworkingapp.di.appViewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.domain.model.TaskItem
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
        onClearError = viewModel::clearActionError,
    )
}

@Composable
fun TaskManagementScreen(
    padding: PaddingValues,
    state: TasksUiState,
    navController: NavController,
    onMoveTask: (taskId: String, statusLabel: String) -> Unit = { _, _ -> },
    onClearError: () -> Unit = {},
) {
    var selectedTask by remember { mutableStateOf<TaskItem?>(null) }

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
        item { SectionTitle("Task Management") }
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
                    subtitle = "Tasks from your active project will sync here. Open Projects to join a workspace.",
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
