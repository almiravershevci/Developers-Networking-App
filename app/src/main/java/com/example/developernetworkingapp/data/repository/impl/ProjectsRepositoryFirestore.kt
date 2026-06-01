package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.ProjectsRepository
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreProjectsDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.schema.TaskBoardColumn
import com.example.developernetworkingapp.domain.model.ProjectBoardContent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Projects board (Kanban) backed by Firestore project + tasks subcollection.
 */
class ProjectsRepositoryFirestore(
    private val authRepository: AuthRepository,
    private val projectsDataSource: FirestoreProjectsDataSource = FirestoreProjectsDataSource(),
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ProjectsRepository {

    override fun observeProjects(): Flow<ProjectBoardContent> =
        authRepository.currentUser.flatMapLatest {
            flow {
                val firebaseUser = firebaseAuth.currentUser
                val content = when {
                    firebaseUser == null -> signedOutBoard()
                    !firebaseUser.isEmailVerified -> signedOutBoard()
                    else -> runCatching { loadPrimaryProjectBoard(firebaseUser.uid) }
                        .getOrElse { errorBoard(it) }
                }
                emit(content)
            }
        }.flowOn(Dispatchers.IO)

    private suspend fun loadPrimaryProjectBoard(uid: String): ProjectBoardContent {
        val projectId = resolvePrimaryProjectId(uid)
        val project = projectsDataSource.fetchProject(projectId)
            ?: throw IllegalStateException("Project $projectId not found. Run the Firestore seed.")

        val tasks = runCatching { projectsDataSource.fetchProjectTasks(projectId) }
            .getOrDefault(emptyList())
        val members = runCatching { projectsDataSource.fetchProjectMembers(projectId) }
            .getOrDefault(emptyList())
        val memberProfiles = runCatching {
            userDataSource.fetchUserProfiles(members.map { it.memberUserId })
        }.getOrDefault(emptyMap())

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

    private suspend fun resolvePrimaryProjectId(uid: String): String {
        val owned = runCatching { projectsDataSource.fetchOwnedProjects(uid) }
            .getOrDefault(emptyList())
        if (owned.isNotEmpty()) return owned.first().id
        return DEFAULT_SHOWCASE_PROJECT_ID
    }

    private fun signedOutBoard(): ProjectBoardContent = ProjectBoardContent(
        teamName = "Sign in required",
        teamMeta = "Log in to load your project board from Firestore",
        todo = emptyList(),
        inProgress = emptyList(),
        done = emptyList(),
    )

    private fun errorBoard(error: Throwable): ProjectBoardContent = ProjectBoardContent(
        teamName = "Could not load project",
        teamMeta = error.message ?: "Check Logcat for Firestore errors (rules, seed, or index).",
        todo = emptyList(),
        inProgress = emptyList(),
        done = emptyList(),
    )

    private companion object {
        const val DEFAULT_SHOWCASE_PROJECT_ID = "proj_devconnect_mobile"
    }
}
