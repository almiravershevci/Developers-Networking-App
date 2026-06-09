package com.example.developernetworkingapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.developernetworkingapp.domain.model.ProjectMemberSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    assigneeOptions: List<ProjectMemberSummary> = emptyList(),
    isOwner: Boolean = false,
    onCreate: (title: String, priority: String, boardColumn: String, assigneeUserId: String?) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf("medium") }
    var boardColumn by rememberSaveable { mutableStateOf("todo") }
    var priorityExpanded by rememberSaveable { mutableStateOf(false) }
    var columnExpanded by rememberSaveable { mutableStateOf(false) }
    var assigneeExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedAssigneeId by rememberSaveable { mutableStateOf<String?>(null) }

    val priorityOptions = listOf("low", "medium", "high")
    val columnOptions = listOf("todo" to "To Do", "in_progress" to "In Progress", "done" to "Done")
    val safePriority = priority.takeIf { it in priorityOptions } ?: "medium"
    val safeBoardColumn = boardColumn.takeIf { value -> columnOptions.any { it.first == value } } ?: "todo"
    val columnLabel = columnOptions.firstOrNull { it.first == safeBoardColumn }?.second ?: "To Do"
    val assigneeLabel = assigneeOptions.firstOrNull { it.userId == selectedAssigneeId }?.displayName ?: "Unassigned"

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Create task") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    when {
                        assigneeOptions.isNotEmpty() ->
                            "Assign a task to a project member. They will see it on their board."
                        isOwner ->
                            "No collaborators yet. When someone joins your project (Home → accept their request), they appear in Assign to."
                        else -> "Add a task to your active project board."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { if (!isSubmitting) priorityExpanded = !priorityExpanded },
                ) {
                    OutlinedTextField(
                        value = safePriority.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = !isSubmitting)
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                        enabled = !isSubmitting,
                    )
                    ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false },
                    ) {
                        priorityOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    priority = option
                                    priorityExpanded = false
                                },
                            )
                        }
                    }
                }
                if (isOwner) {
                    ExposedDropdownMenuBox(
                        expanded = assigneeExpanded,
                        onExpandedChange = {
                            if (!isSubmitting && assigneeOptions.isNotEmpty()) {
                                assigneeExpanded = !assigneeExpanded
                            }
                        },
                    ) {
                        OutlinedTextField(
                            value = assigneeLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assign to") },
                            modifier = Modifier
                                .menuAnchor(
                                    ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                    enabled = !isSubmitting && assigneeOptions.isNotEmpty(),
                                )
                                .fillMaxWidth(),
                            trailingIcon = {
                                if (assigneeOptions.isNotEmpty()) {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = assigneeExpanded)
                                }
                            },
                            enabled = !isSubmitting,
                            supportingText = {
                                if (assigneeOptions.isEmpty()) {
                                    Text("Accept a join request on Home to add members.")
                                }
                            },
                        )
                        if (assigneeOptions.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = assigneeExpanded,
                                onDismissRequest = { assigneeExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Unassigned") },
                                    onClick = {
                                        selectedAssigneeId = null
                                        assigneeExpanded = false
                                    },
                                )
                                assigneeOptions.forEach { member ->
                                    DropdownMenuItem(
                                        text = { Text("${member.displayName} (${member.role})") },
                                        onClick = {
                                            selectedAssigneeId = member.userId
                                            assigneeExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = columnExpanded,
                    onExpandedChange = { if (!isSubmitting) columnExpanded = !columnExpanded },
                ) {
                    OutlinedTextField(
                        value = columnLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Column") },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = !isSubmitting)
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = columnExpanded) },
                        enabled = !isSubmitting,
                    )
                    ExposedDropdownMenu(
                        expanded = columnExpanded,
                        onDismissRequest = { columnExpanded = false },
                    ) {
                        columnOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    boardColumn = value
                                    columnExpanded = false
                                },
                            )
                        }
                    }
                }
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, safePriority, safeBoardColumn, selectedAssigneeId) },
                enabled = !isSubmitting && title.isNotBlank(),
            ) {
                Text(if (isSubmitting) "Creating…" else "Create task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancel")
            }
        },
    )
}
