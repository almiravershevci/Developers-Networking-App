package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.ProjectsRepository
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreProjectsDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectMemberDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectTaskDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.TaskBoardColumn
import com.example.developernetworkingapp.data.datasource.firebase.schema.UserProfileDoc
import com.example.developernetworkingapp.domain.model.ProjectBoardContent
import com.example.developernetworkingapp.domain.model.ProjectMemberSummary
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

/**
 * Projects board (Kanban) backed by Firestore project + realtime tasks subcollection.
 */
class ProjectsRepositoryFirestore(
    private val authRepository: AuthRepository,
    private val projectsDataSource: FirestoreProjectsDataSource = FirestoreProjectsDataSource(),
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ProjectsRepository {

    private val refreshTick = MutableStateFlow(0)

    override fun invalidateProjects() {
        refreshTick.value += 1
    }

    override fun observeProjects(): Flow<ProjectBoardContent> =
        combine(authRepository.currentUser, refreshTick) { authUser, _ -> authUser }
            .flatMapLatest { authUser ->
                val firebaseUser = firebaseAuth.currentUser
                when {
                    authUser == null || firebaseUser == null -> flowOf(signedOutBoard())
                    !firebaseUser.isEmailVerified -> flowOf(signedOutBoard())
                    else -> observePrimaryProjectBoard(firebaseUser.uid)
                }
            }.flowOn(Dispatchers.IO)

    override suspend fun createProject(
        title: String,
        description: String,
        primaryStackLabel: String,
    ): Result<String> = runCatching {
        val user = firebaseAuth.currentUser
            ?: error("Sign in to create a project.")
        if (!user.isEmailVerified) {
            error("Verify your email before creating a project.")
        }
        projectsDataSource.createProject(
            ownerUserId = user.uid,
            title = title,
            description = description,
            primaryStackLabel = primaryStackLabel,
        )
    }.mapError { error ->
        Result.failure(IllegalStateException(mapCreateProjectError(error), error))
    }

    private fun mapCreateProjectError(error: Throwable): String {
        val detail = error.message.orEmpty()
        return when {
            detail.contains("PERMISSION_DENIED", ignoreCase = true) ->
                "Firestore blocked project creation. Publish the latest firestore.rules (see firestore/RULES_PASTE_IN_CONSOLE.rules)."
            detail.contains("Sign in", ignoreCase = true) -> detail
            else -> detail.ifBlank { "Could not create project." }
        }
    }

    private fun <T> Result<T>.mapError(transform: (Throwable) -> Result<T>): Result<T> =
        fold(onSuccess = { Result.success(it) }, onFailure = transform)

    private fun observePrimaryProjectBoard(uid: String): Flow<ProjectBoardContent> = flow {
        val ownedResult = runCatching { projectsDataSource.fetchOwnedProjects(uid) }
        ownedResult.exceptionOrNull()?.let { error ->
            emit(permissionOrLoadErrorBoard(error))
            return@flow
        }
        val owned = ownedResult.getOrDefault(emptyList())
        val memberProjectIds = runCatching { projectsDataSource.fetchMemberProjectIds(uid) }
            .getOrDefault(emptySet())
        val projectId = owned.firstOrNull()?.id
            ?: memberProjectIds.firstOrNull()
            ?: resolveShowcaseProjectId()
        if (projectId == null) {
            emit(emptyBoard())
            return@flow
        }
        val project = projectsDataSource.fetchProject(projectId)
        if (project == null) {
            emit(emptyBoard())
            return@flow
        }

        val members = runCatching { projectsDataSource.fetchProjectMembers(projectId) }
            .getOrDefault(emptyList())
        val memberProfiles = runCatching {
            userDataSource.fetchUserProfiles(members.map { it.memberUserId })
        }.getOrDefault(emptyMap())
        val isOwner = project.ownerUserId == uid

        projectsDataSource.observeProjectTasks(projectId).collect { tasks ->
            emit(buildBoardContent(project, uid, isOwner, members, memberProfiles, tasks))
        }
    }.catch { error ->
        emit(
            ProjectBoardContent(
                teamName = "Couldn't load project board",
                teamMeta = userFacingProjectError(error),
                todo = emptyList(),
                inProgress = emptyList(),
                done = emptyList(),
            ),
        )
    }

    private fun buildBoardContent(
        project: ProjectDoc,
        viewerUserId: String,
        isOwner: Boolean,
        members: List<ProjectMemberDoc>,
        memberProfiles: Map<String, UserProfileDoc>,
        tasks: List<ProjectTaskDoc>,
    ): ProjectBoardContent {
        val memberSummaries = members.map { member ->
            ProjectMemberSummary(
                userId = member.memberUserId,
                displayName = memberProfiles[member.memberUserId]?.displayName ?: member.memberUserId.take(8),
                role = member.memberRole,
            )
        }
        val memberNames = memberSummaries.map { it.displayName }
        val teamMeta = buildString {
            append("${project.memberCount} members")
            if (memberNames.isNotEmpty()) {
                append(" · ")
                append(memberNames.take(3).joinToString(", "))
            }
            append(" · ")
            append(project.primaryStackLabel.ifBlank { "Multi-stack" })
            if (!isOwner) append(" · You are a project member")
        }

        val visibleTasks = if (isOwner) {
            tasks
        } else {
            tasks.filter { task ->
                task.assigneeUserId == null || task.assigneeUserId == viewerUserId
            }
        }
        val todo = visibleTasks.filter { it.boardColumn == TaskBoardColumn.TODO }.map { it.title }
        val inProgress = visibleTasks.filter { it.boardColumn == TaskBoardColumn.IN_PROGRESS }.map { it.title }
        val done = visibleTasks.filter { it.boardColumn == TaskBoardColumn.DONE }.map { it.title }

        return ProjectBoardContent(
            projectId = project.id,
            teamName = project.title,
            teamMeta = teamMeta,
            isOwner = isOwner,
            members = memberSummaries,
            todo = todo,
            inProgress = inProgress,
            done = done,
        )
    }

    private suspend fun resolveShowcaseProjectId(): String? {
        val showcase = projectsDataSource.fetchProject(DEFAULT_SHOWCASE_PROJECT_ID)
        return if (showcase != null) DEFAULT_SHOWCASE_PROJECT_ID else null
    }

    private fun userFacingProjectError(error: Throwable): String {
        val detail = error.message.orEmpty()
        return when {
            detail.contains("PERMISSION_DENIED", ignoreCase = true) ->
                "Project access blocked by Firestore rules. Deploy rules and retry."
            detail.contains("index", ignoreCase = true) ||
                detail.contains("FAILED_PRECONDITION", ignoreCase = true) ->
                "Firestore index missing for project tasks. Deploy firestore.indexes.json."
            else -> "Couldn't load project board. $detail"
        }
    }

    private fun permissionOrLoadErrorBoard(error: Throwable): ProjectBoardContent =
        ProjectBoardContent(
            teamName = "Couldn't load project board",
            teamMeta = userFacingProjectError(error),
            todo = emptyList(),
            inProgress = emptyList(),
            done = emptyList(),
        )

    private fun emptyBoard(): ProjectBoardContent = ProjectBoardContent(
        teamName = "No projects yet",
        teamMeta = "Create your first project to open the kanban board and recruit collaborators.",
        todo = emptyList(),
        inProgress = emptyList(),
        done = emptyList(),
    )

    private fun signedOutBoard(): ProjectBoardContent = ProjectBoardContent(
        teamName = "Sign in required",
        teamMeta = "Log in to load your project board from Firestore",
        todo = emptyList(),
        inProgress = emptyList(),
        done = emptyList(),
    )

    private companion object {
        const val DEFAULT_SHOWCASE_PROJECT_ID = "proj_devconnect_mobile"
    }
}
