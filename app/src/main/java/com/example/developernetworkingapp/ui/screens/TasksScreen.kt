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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.TasksUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.TasksViewModel

@Composable
fun TaskManagementRoute(padding: PaddingValues, navController: NavController) {
    val viewModel: TasksViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TaskManagementScreen(padding, state, navController)
}

@Composable
fun TaskManagementScreen(
    padding: PaddingValues,
    state: TasksUiState,
    navController: NavController
) {
    var selectedTask by remember { mutableStateOf<String?>(null) }
    val taskStatusOverrides = remember { mutableStateMapOf<String, String>() }

    selectedTask?.let { task ->
        AlertDialog(
            onDismissRequest = { selectedTask = null },
            title = { Text("Task Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task, style = MaterialTheme.typography.bodyMedium)
                    Text("Includes assignee, due date, and linked project context.", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedTask = null
                    navController.navigate(AppRoutes.projectsRoute(taskLinkedProject(task)))
                }) { Text("Open Task") }
            },
            dismissButton = { TextButton(onClick = { selectedTask = null }) { Text("Close") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item { SectionTitle("Task Management") }
        val taskItems = state.content?.items.orEmpty()
        if (taskItems.isEmpty()) {
            item {
                Text(
                    "No tasks loaded. Stay logged in, rebuild the app, and confirm seed project " +
                        "proj_devconnect_mobile has a tasks subcollection.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(taskItems) { task ->
            val parsedTask = remember(task) { parseTask(task) }
            val activeStatus = taskStatusOverrides[task] ?: parsedTask.status
            val displayTaskText = "${parsedTask.content} - $activeStatus"
            ElevatedCard(
                onClick = { selectedTask = task },
                shape = AppDesignTokens.cardShape,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(AppDesignTokens.cardInnerPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(displayTaskText, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { taskStatusOverrides[task] = "In Progress" },
                            label = { Text("In Progress") }
                        )
                        AssistChip(
                            onClick = { taskStatusOverrides[task] = "To Do" },
                            label = { Text("To Do") }
                        )
                        AssistChip(
                            onClick = { taskStatusOverrides[task] = "Done" },
                            label = { Text("Done") }
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

private data class ParsedTask(
    val content: String,
    val status: String
)

private fun parseTask(rawTask: String): ParsedTask {
    val statusOptions = setOf("In Progress", "To Do", "Done", "In Review")
    val segments = rawTask.split(" - ")
    if (segments.isEmpty()) return ParsedTask(content = rawTask, status = "To Do")
    val lastSegment = segments.last()
    return if (lastSegment in statusOptions && segments.size > 1) {
        ParsedTask(
            content = segments.dropLast(1).joinToString(" - "),
            status = if (lastSegment == "In Review") "In Progress" else lastSegment
        )
    } else {
        ParsedTask(content = rawTask, status = "To Do")
    }
}

private fun taskLinkedProject(task: String): String {
    val normalized = task.lowercase()
    return when {
        "api" in normalized || "backend" in normalized -> "Talent Graph API"
        else -> "DevConnect Mobile"
    }
}
