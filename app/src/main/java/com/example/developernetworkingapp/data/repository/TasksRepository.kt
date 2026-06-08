package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.data.datasource.firebase.schema.TaskBoardColumn
import com.example.developernetworkingapp.data.datasource.firebase.schema.TaskPriority
import com.example.developernetworkingapp.domain.model.TaskContent
import com.example.developernetworkingapp.domain.model.TaskItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface TasksRepository {
    fun observeTasks(): Flow<TaskContent>

    suspend fun createTask(
        title: String,
        priority: String = TaskPriority.MEDIUM,
        assigneeUserId: String? = null,
        boardColumn: String = TaskBoardColumn.TODO,
    ): Result<Unit>

    suspend fun moveTask(taskId: String, boardColumn: String): Result<Unit>

    suspend fun updateTask(
        taskId: String,
        title: String? = null,
        priority: String? = null,
        assigneeUserId: String? = null,
    ): Result<Unit>

    suspend fun deleteTask(taskId: String): Result<Unit>
}

class FakeTasksRepository : TasksRepository {
    override fun observeTasks(): Flow<TaskContent> = flowOf(
        TaskContent(
            items = listOf(
                TaskItem(
                    id = "fake_1",
                    title = "Design project details page",
                    statusLabel = "In Progress",
                    boardColumn = TaskBoardColumn.IN_PROGRESS,
                    priority = TaskPriority.HIGH,
                    assigneeLabel = "You",
                ),
                TaskItem(
                    id = "fake_2",
                    title = "Connect search filters to backend",
                    statusLabel = "To Do",
                    boardColumn = TaskBoardColumn.TODO,
                    priority = TaskPriority.MEDIUM,
                    assigneeLabel = "Omar",
                ),
                TaskItem(
                    id = "fake_3",
                    title = "Create notifications API contract",
                    statusLabel = "Done",
                    boardColumn = TaskBoardColumn.DONE,
                    priority = TaskPriority.LOW,
                    assigneeLabel = "Sara",
                ),
            ),
        ),
    )

    override suspend fun createTask(
        title: String,
        priority: String,
        assigneeUserId: String?,
        boardColumn: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun moveTask(taskId: String, boardColumn: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun updateTask(
        taskId: String,
        title: String?,
        priority: String?,
        assigneeUserId: String?,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun deleteTask(taskId: String): Result<Unit> = Result.success(Unit)
}
