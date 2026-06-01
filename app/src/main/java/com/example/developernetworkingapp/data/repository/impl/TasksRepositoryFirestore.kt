package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.repository.TasksRepository
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreProjectsDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.authStateChanges
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectTaskDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.TaskBoardColumn
import com.example.developernetworkingapp.domain.model.TaskContent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Task list screen — loads tasks from the same Firestore project as the Kanban board.
 */
class TasksRepositoryFirestore(
    private val projectsDataSource: FirestoreProjectsDataSource = FirestoreProjectsDataSource(),
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : TasksRepository {

    override fun observeTasks(): Flow<TaskContent> =
        firebaseAuth.authStateChanges().flatMapLatest { firebaseUser ->
            flow {
                val items = when {
                    firebaseUser == null -> emptyList()
                    !firebaseUser.isEmailVerified -> emptyList()
                    else -> runCatching { loadTaskLines(firebaseUser.uid) }.getOrDefault(emptyList())
                }
                emit(TaskContent(items = items))
            }
        }.flowOn(Dispatchers.IO)

    private suspend fun loadTaskLines(currentUserId: String): List<String> {
        val projectId = resolveProjectId(currentUserId)
        val tasks = projectsDataSource.fetchProjectTasks(projectId)
        val assigneeIds = tasks.mapNotNull { it.assigneeUserId }.distinct()
        val profiles = runCatching {
            userDataSource.fetchUserProfiles(assigneeIds)
        }.getOrDefault(emptyMap())

        return tasks.map { task ->
            formatTaskLine(task, currentUserId, profiles[task.assigneeUserId]?.displayName)
        }
    }

    private suspend fun resolveProjectId(uid: String): String {
        val owned = runCatching { projectsDataSource.fetchOwnedProjects(uid) }
            .getOrDefault(emptyList())
        if (owned.isNotEmpty()) return owned.first().id
        return DEFAULT_PROJECT_ID
    }

    private fun formatTaskLine(
        task: ProjectTaskDoc,
        currentUserId: String,
        assigneeName: String?,
    ): String {
        val assignee = when {
            task.assigneeUserId == null -> "Unassigned"
            task.assigneeUserId == currentUserId -> "You"
            !assigneeName.isNullOrBlank() -> assigneeName
            else -> "Teammate"
        }
        val column = when (task.boardColumn) {
            TaskBoardColumn.TODO -> "To Do"
            TaskBoardColumn.IN_PROGRESS -> "In Progress"
            TaskBoardColumn.DONE -> "Done"
            TaskBoardColumn.BLOCKED -> "Blocked"
            else -> task.boardColumn
        }
        val priority = task.priority.replaceFirstChar { it.uppercase() }
        return "${task.title} - $priority - Assignee: $assignee - $column"
    }

    private companion object {
        const val DEFAULT_PROJECT_ID = "proj_devconnect_mobile"
    }
}
