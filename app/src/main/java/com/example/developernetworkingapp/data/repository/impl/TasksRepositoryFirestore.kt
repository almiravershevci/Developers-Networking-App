package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.repository.TasksRepository
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreInboxDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreProjectsDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.authStateChanges
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectTaskDoc
import com.example.developernetworkingapp.data.repository.mapping.TaskItemMapper
import com.example.developernetworkingapp.domain.model.TaskContent
import com.example.developernetworkingapp.domain.model.TaskItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Task list microservice — realtime reads and member-gated writes for project tasks.
 */
class TasksRepositoryFirestore(
    private val projectsDataSource: FirestoreProjectsDataSource = FirestoreProjectsDataSource(),
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource(),
    private val inboxDataSource: FirestoreInboxDataSource = FirestoreInboxDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : TasksRepository {

    override fun observeTasks(): Flow<TaskContent> =
        firebaseAuth.authStateChanges().flatMapLatest { firebaseUser ->
            when {
                firebaseUser == null -> flowOf(
                    TaskContent(statusMessage = "Sign in to view your project tasks."),
                )
                !firebaseUser.isEmailVerified -> flowOf(
                    TaskContent(statusMessage = "Verify your email to load tasks."),
                )
                else -> observeTasksForUser(firebaseUser.uid)
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun createTask(
        title: String,
        priority: String,
        assigneeUserId: String?,
        boardColumn: String,
        projectId: String?,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireSignedInUser() ?: return@withContext authRequiredFailure()
        runCatching {
            val resolvedProjectId = projectId?.takeIf { it.isNotBlank() } ?: resolveProjectId(uid)
            projectsDataSource.createProjectTask(
                projectId = resolvedProjectId,
                createdByUserId = uid,
                title = title,
                priority = priority,
                assigneeUserId = assigneeUserId,
                boardColumn = boardColumn,
            )
            assigneeUserId?.takeIf { it.isNotBlank() && it != uid }?.let { assigneeId ->
                runCatching {
                    inboxDataSource.createNotification(
                        recipientUserId = assigneeId,
                        title = "New project task",
                        body = "You were assigned \"$title\". Open Projects or Tasks to view it.",
                        deepLink = "/tasks",
                    )
                }
            }
            Unit
        }.toTaskResult("create task")
    }

    override suspend fun moveTask(taskId: String, boardColumn: String): Result<Unit> =
        mutateTask(taskId) { projectId ->
            projectsDataSource.updateTaskBoardColumn(projectId, taskId, boardColumn)
        }

    override suspend fun updateTask(
        taskId: String,
        title: String?,
        priority: String?,
        assigneeUserId: String?,
    ): Result<Unit> = mutateTask(taskId) { projectId ->
        projectsDataSource.updateProjectTask(
            projectId = projectId,
            taskId = taskId,
            title = title,
            priority = priority,
            assigneeUserId = assigneeUserId,
        )
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> =
        mutateTask(taskId) { projectId ->
            projectsDataSource.deleteProjectTask(projectId, taskId)
        }

    private suspend fun mutateTask(
        taskId: String,
        block: suspend (projectId: String) -> Unit,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireSignedInUser() ?: return@withContext authRequiredFailure()
        runCatching {
            val projectId = resolveProjectId(uid)
            block(projectId)
        }.toTaskResult("update task")
    }

    private fun observeTasksForUser(uid: String): Flow<TaskContent> = flow {
        val projectId = resolveProjectId(uid)
        val project = projectsDataSource.fetchProject(projectId)
        val isOwner = project?.ownerUserId == uid
        projectsDataSource.observeProjectTasks(projectId).collect { tasks ->
            val visibleTasks = if (isOwner) {
                tasks
            } else {
                tasks.filter { task -> task.assigneeUserId == uid }
            }
            val assigneeIds = visibleTasks.mapNotNull { it.assigneeUserId }.distinct()
            val profiles = runCatching {
                userDataSource.fetchUserProfiles(assigneeIds)
            }.getOrDefault(emptyMap())
            emit(
                TaskContent(
                    items = visibleTasks.map { task ->
                        mapToTaskItem(task, uid, profiles[task.assigneeUserId]?.displayName)
                    },
                    statusMessage = if (!isOwner && visibleTasks.isEmpty()) {
                        "No tasks assigned to you yet. Your project owner can assign work from the Projects tab."
                    } else {
                        null
                    },
                ),
            )
        }
    }.catch { error ->
        emit(taskErrorContent(error))
    }

    private suspend fun resolveProjectId(uid: String): String {
        val owned = runCatching { projectsDataSource.fetchOwnedProjects(uid) }
            .getOrDefault(emptyList())
        if (owned.isNotEmpty()) return owned.first().id
        val memberIds = runCatching { projectsDataSource.fetchMemberProjectIds(uid) }
            .getOrDefault(emptySet())
        if (memberIds.isNotEmpty()) return memberIds.first()
        return DEFAULT_PROJECT_ID
    }

    private fun requireSignedInUser(): String? {
        val user = firebaseAuth.currentUser ?: return null
        if (!user.isEmailVerified) return null
        return user.uid
    }

    private fun authRequiredFailure(): Result<Unit> =
        Result.failure(IllegalStateException("Sign in with a verified email to manage tasks."))

    private fun <T> Result<T>.toTaskResult(action: String): Result<Unit> = fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { error ->
            val detail = error.message.orEmpty()
            val message = when {
                detail.contains("PERMISSION_DENIED", ignoreCase = true) -> {
                    val uid = firebaseAuth.currentUser?.uid ?: "unknown"
                    "Cannot $action. Add projects/proj_devconnect_mobile/members/$uid " +
                        "in Firestore (run: npm run project:add-me in firestore/)."
                }
                else -> "Cannot $action. $detail"
            }
            Result.failure(IllegalStateException(message, error))
        },
    )

    private fun taskErrorContent(error: Throwable): TaskContent {
        val detail = error.message.orEmpty()
        val message = when {
            detail.contains("PERMISSION_DENIED", ignoreCase = true) ->
                "Tasks blocked by rules. Publish firestore.rules and join the project as a member."
            detail.contains("FAILED_PRECONDITION", ignoreCase = true) ||
                detail.contains("index", ignoreCase = true) ->
                "Firestore needs a tasks index. Deploy firestore.indexes.json, then retry."
            else -> "Couldn't load tasks. ($detail)"
        }
        return TaskContent(statusMessage = message)
    }

    private fun mapToTaskItem(
        task: ProjectTaskDoc,
        currentUserId: String,
        assigneeName: String?,
    ): TaskItem = TaskItemMapper.fromDoc(task, currentUserId, assigneeName)

    private companion object {
        const val DEFAULT_PROJECT_ID = "proj_devconnect_mobile"
    }
}
