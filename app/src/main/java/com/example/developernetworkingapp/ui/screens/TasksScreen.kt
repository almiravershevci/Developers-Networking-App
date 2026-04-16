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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.state.TasksUiState
import com.example.developernetworkingapp.ui.viewmodel.TasksViewModel

@Composable
fun TaskManagementRoute(padding: PaddingValues) {
    val viewModel: TasksViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TaskManagementScreen(padding, state)
}

@Composable
fun TaskManagementScreen(padding: PaddingValues, state: TasksUiState) {
    var selectedTask by remember { mutableStateOf<String?>(null) }

    selectedTask?.let { task ->
        AlertDialog(
            onDismissRequest = { selectedTask = null },
            title = { Text("Task Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task, style = MaterialTheme.typography.bodyMedium)
                    Text("Includes assignee, due date, and linked project context.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { Button(onClick = { selectedTask = null }) { Text("Open Task") } },
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
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { SectionTitle("Task Management") }
        items(state.content?.items ?: emptyList()) { task ->
            ElevatedCard(
                onClick = { selectedTask = task },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text("In Progress") })
                        AssistChip(onClick = {}, label = { Text("Priority") })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { selectedTask = task }) { Text("View details") }
                        TextButton(onClick = {}) { Text("Update status") }
                    }
                }
            }
        }
    }
}
