package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.developernetworkingapp.ui.components.PremiumInfoCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.components.TaskColumn
import com.example.developernetworkingapp.ui.state.ProjectsUiState
import com.example.developernetworkingapp.ui.viewmodel.ProjectsViewModel

@Composable
fun ProjectBoardRoute(padding: PaddingValues) {
    val viewModel: ProjectsViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProjectBoardScreen(padding, state)
}

@Composable
fun ProjectBoardScreen(padding: PaddingValues, state: ProjectsUiState) {
    val content = state.content
    var selectedTask by remember { mutableStateOf<String?>(null) }

    selectedTask?.let { task ->
        AlertDialog(
            onDismissRequest = { selectedTask = null },
            title = { Text("Task Detail") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task, style = MaterialTheme.typography.titleMedium)
                    Text("Status, assignee, dependencies, and sprint estimate are tracked in this task.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text("In Sprint") })
                        AssistChip(onClick = {}, label = { Text("Priority High") })
                    }
                }
            },
            confirmButton = { Button(onClick = { selectedTask = null }) { Text("Open board") } },
            dismissButton = { TextButton(onClick = { selectedTask = null }) { Text("Close") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { SectionTitle("Project Workspace") }
        item {
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(content?.teamName ?: "Loading team...", style = MaterialTheme.typography.titleLarge)
                    Text(content?.teamMeta ?: "Preparing board data", style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text("Roadmap") }, colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)))
                        AssistChip(onClick = {}, label = { Text("Members") })
                        AssistChip(onClick = {}, label = { Text("Milestones") })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { }) { Text("Open Project") }
                        TextButton(onClick = { }) { Text("Invite dev") }
                    }
                }
            }
        }
        item { SectionTitle("Kanban Board") }
        item {
            ClickableTaskColumn("To Do", content?.todo ?: emptyList()) { selectedTask = it }
        }
        item {
            ClickableTaskColumn("In Progress", content?.inProgress ?: emptyList()) { selectedTask = it }
        }
        item {
            ClickableTaskColumn("Done", content?.done ?: emptyList()) { selectedTask = it }
        }
        item {
            PremiumInfoCard(
                "Project actions",
                "Create sprint, review merge requests, assign members, and publish updates to followers."
            )
        }
    }
}

@Composable
private fun ClickableTaskColumn(
    title: String,
    tasks: List<String>,
    onTaskClick: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            tasks.forEach { task ->
                ElevatedCard(
                    onClick = { onTaskClick(task) },
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(task, style = MaterialTheme.typography.bodyMedium)
                        Text("View", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
