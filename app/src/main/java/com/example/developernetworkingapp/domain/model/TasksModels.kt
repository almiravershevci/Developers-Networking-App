package com.example.developernetworkingapp.domain.model

data class TaskItem(
    val id: String,
    val title: String,
    val statusLabel: String,
    val boardColumn: String,
    val priority: String,
    val assigneeLabel: String,
) {
    val displayLine: String
        get() = "$title - ${priority.replaceFirstChar { it.uppercase() }} - Assignee: $assigneeLabel - $statusLabel"
}

data class TaskContent(
    val items: List<TaskItem> = emptyList(),
    val statusMessage: String? = null,
)
