package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.TaskContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface TasksRepository {
    fun observeTasks(): Flow<TaskContent>
}

class FakeTasksRepository : TasksRepository {
    override fun observeTasks(): Flow<TaskContent> = flowOf(
        TaskContent(
            items = listOf(
                "Design project details page - Due Today - Assignee: You - In Progress",
                "Connect search filters to backend - Due Tomorrow - Assignee: Omar - To Do",
                "Create notifications API contract - Due Fri - Assignee: Sara - In Review"
            )
        )
    )
}
