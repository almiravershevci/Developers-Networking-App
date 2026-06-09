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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

    override fun observeProjects(): Flow<ProjectBoardContent> =
        authRepository.currentUser.flatMapLatest {
            val firebaseUser = firebaseAuth.currentUser
            when {
                firebaseUser == null -> flowOf(signedOutBoard())
                !firebaseUser.isEmailVerified -> flowOf(signedOutBoard())
                else -> observePrimaryProjectBoard(firebaseUser.uid)
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun createProject(
        title: String,
        description: String,
        primaryStackLabel: String,
    ): Result<String> = runCatching {
        val uid = firebaseAuth.currentUser?.uid
            ?: error("Sign in to create a project.")
        projectsDataSource.createProject(
            ownerUserId = uid,
            title = title,
            description = description,
            primaryStackLabel = primaryStackLabel,
        )
    }

    private fun observePrimaryProjectBoard(uid: String): Flow<ProjectBoardContent> = flow {
        val owned = runCatching { projectsDataSource.fetchOwnedProjects(uid) }.getOrDefault(emptyList())
        val projectId = owned.firstOrNull()?.id ?: resolveShowcaseProjectId()
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

        projectsDataSource.observeProjectTasks(projectId).collect { tasks ->
            emit(buildBoardContent(project, members, memberProfiles, tasks))
        }
    }

    private fun buildBoardContent(
        project: ProjectDoc,
        members: List<ProjectMemberDoc>,
        memberProfiles: Map<String, UserProfileDoc>,
        tasks: List<ProjectTaskDoc>,
    ): ProjectBoardContent {
        val memberNames = members.mapNotNull { memberProfiles[it.memberUserId]?.displayName }
        val teamMeta = buildString {
            append("${project.memberCount} members")
            if (memberNames.isNotEmpty()) {
                append(" · ")
                append(memberNames.take(3).joinToString(", "))
            }
            append(" · ")
            append(project.primaryStackLabel.ifBlank { "Multi-stack" })
        }

        val todo = tasks.filter { it.boardColumn == TaskBoardColumn.TODO }.map { it.title }
        val inProgress = tasks.filter { it.boardColumn == TaskBoardColumn.IN_PROGRESS }.map { it.title }
        val done = tasks.filter { it.boardColumn == TaskBoardColumn.DONE }.map { it.title }

        return ProjectBoardContent(
            teamName = project.title,
            teamMeta = teamMeta,
            todo = todo,
            inProgress = inProgress,
            done = done,
        )
    }

    private suspend fun resolveShowcaseProjectId(): String? {
        val showcase = projectsDataSource.fetchProject(DEFAULT_SHOWCASE_PROJECT_ID)
        return if (showcase != null) DEFAULT_SHOWCASE_PROJECT_ID else null
    }

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
