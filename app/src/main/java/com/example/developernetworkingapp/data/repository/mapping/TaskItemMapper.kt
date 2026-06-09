package com.example.developernetworkingapp.data.repository.mapping

import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectTaskDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.TaskBoardColumn
import com.example.developernetworkingapp.domain.model.TaskItem

object TaskItemMapper {
    fun fromDoc(
        task: ProjectTaskDoc,
        currentUserId: String,
        assigneeName: String?,
    ): TaskItem = TaskItem(
        id = task.id,
        title = task.title,
        statusLabel = columnLabel(task.boardColumn),
        boardColumn = task.boardColumn,
        priority = task.priority,
        assigneeLabel = assigneeLabel(task, currentUserId, assigneeName),
    )

    private fun assigneeLabel(
        task: ProjectTaskDoc,
        currentUserId: String,
        assigneeName: String?,
    ): String = when {
        task.assigneeUserId == null -> "Unassigned"
        task.assigneeUserId == currentUserId -> "You"
        !assigneeName.isNullOrBlank() -> assigneeName
        else -> "Teammate"
    }

    private fun columnLabel(boardColumn: String): String = when (boardColumn) {
        TaskBoardColumn.TODO -> "To Do"
        TaskBoardColumn.IN_PROGRESS -> "In Progress"
        TaskBoardColumn.DONE -> "Done"
        TaskBoardColumn.BLOCKED -> "Blocked"
        else -> boardColumn
    }
}
