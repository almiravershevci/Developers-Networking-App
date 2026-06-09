package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.datasource.firebase.FirestoreInboxDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreProjectJoinDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreProjectsDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.authStateChanges
import com.example.developernetworkingapp.data.datasource.firebase.formatRelativeTime
import com.example.developernetworkingapp.data.datasource.firebase.schema.MatchWorkflow
import com.example.developernetworkingapp.data.datasource.firebase.schema.MemberRole
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectJoinRequestDoc
import com.example.developernetworkingapp.data.repository.ProjectJoinRepository
import com.example.developernetworkingapp.domain.model.ProjectJoinRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProjectJoinRepositoryFirestore(
    private val joinDataSource: FirestoreProjectJoinDataSource = FirestoreProjectJoinDataSource(),
    private val projectsDataSource: FirestoreProjectsDataSource = FirestoreProjectsDataSource(),
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource(),
    private val inboxDataSource: FirestoreInboxDataSource = FirestoreInboxDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ProjectJoinRepository {

    override fun observeIncomingRequests(): Flow<List<ProjectJoinRequest>> =
        firebaseAuth.authStateChanges().flatMapLatest { firebaseUser ->
            when {
                firebaseUser == null -> flowOf(emptyList())
                else -> joinDataSource.observeIncomingPending(firebaseUser.uid)
                    .map { docs -> mapRequests(docs, currentUserId = firebaseUser.uid, incoming = true) }
            }
        }.flowOn(Dispatchers.IO)

    override fun observeOutgoingRequests(): Flow<List<ProjectJoinRequest>> =
        firebaseAuth.authStateChanges().flatMapLatest { firebaseUser ->
            when {
                firebaseUser == null -> flowOf(emptyList())
                else -> joinDataSource.observeOutgoingPending(firebaseUser.uid)
                    .map { docs -> mapRequests(docs, currentUserId = firebaseUser.uid, incoming = false) }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun sendJoinRequest(
        projectId: String,
        projectTitle: String,
        ownerUserId: String,
        requestedRole: String,
        message: String?,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireSignedInUser() ?: return@withContext authRequiredFailure()
        if (projectId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Project is required."))
        }
        runCatching {
            val project = projectsDataSource.fetchProject(projectId)
                ?: error("Project not found. It may have been removed from the feed.")
            val resolvedOwnerId = project.ownerUserId.ifBlank { ownerUserId }
            if (resolvedOwnerId.isBlank()) {
                error("This project has no owner set in Firestore. Ask the creator to recreate it.")
            }
            if (resolvedOwnerId == uid) {
                error("You already own this project.")
            }
            val existing = runCatching {
                joinDataSource.fetchPendingForProject(projectId, uid)
            }.getOrNull()
            if (existing != null) {
                error("You already have a pending request for this project.")
            }
            val memberProjectIds = runCatching {
                projectsDataSource.fetchMemberProjectIds(uid)
            }.getOrDefault(emptySet())
            if (memberProjectIds.contains(projectId) || project.ownerUserId == uid) {
                error("You are already a member of this project.")
            }
            joinDataSource.createJoinRequest(
                projectId = projectId,
                projectTitle = projectTitle.ifBlank { project.title },
                fromUserId = uid,
                toUserId = resolvedOwnerId,
                requestedRole = requestedRole,
                message = message,
            )
            runCatching {
                inboxDataSource.createNotification(
                    recipientUserId = resolvedOwnerId,
                    title = "New project join request",
                    body = "Someone wants to join \"${projectTitle.ifBlank { project.title }}\". Review it on Home or Projects.",
                    deepLink = "/dashboard",
                )
            }
            Unit
        }.toJoinResult("send join request")
    }

    override suspend fun acceptRequest(requestId: String): Result<Unit> =
        resolveRequest(requestId, MatchWorkflow.ACCEPTED)

    override suspend fun declineRequest(requestId: String): Result<Unit> =
        resolveRequest(requestId, MatchWorkflow.DECLINED)

    override suspend fun membershipProjectIds(userId: String): Set<String> =
        withContext(Dispatchers.IO) {
            runCatching { projectsDataSource.fetchMemberProjectIds(userId) }.getOrDefault(emptySet())
        }

    private suspend fun resolveRequest(requestId: String, workflowStatus: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val uid = requireSignedInUser() ?: return@withContext authRequiredFailure()
            runCatching {
                val request = joinDataSource.fetchJoinRequest(requestId)
                    ?: error("Join request not found.")
                if (request.workflowStatus != MatchWorkflow.PENDING) {
                    error("This request was already resolved.")
                }
                val project = projectsDataSource.fetchProject(request.projectId)
                    ?: error("Project no longer exists.")
                if (project.ownerUserId != uid && request.toUserId != uid) {
                    error("Only the project owner can accept or decline this request.")
                }
                if (workflowStatus == MatchWorkflow.ACCEPTED) {
                    projectsDataSource.addProjectMember(
                        projectId = request.projectId,
                        memberUserId = request.fromUserId,
                        memberRole = resolveMemberRole(request.requestedRole),
                    )
                    val applicantName = userDataSource.fetchUserProfile(request.fromUserId)
                        ?.displayName
                        ?.ifBlank { "A collaborator" }
                        ?: "A collaborator"
                    inboxDataSource.createNotification(
                        recipientUserId = request.fromUserId,
                        title = "Welcome to ${request.projectTitle}",
                        body = "Your join request was accepted. You are now a member of this project workspace.",
                        deepLink = "/projects",
                    )
                    inboxDataSource.createNotification(
                        recipientUserId = uid,
                        title = "${applicantName} joined ${request.projectTitle}",
                        body = "They can now see project tasks assigned to them.",
                        deepLink = "/projects",
                    )
                } else {
                    inboxDataSource.createNotification(
                        recipientUserId = request.fromUserId,
                        title = "Join request update",
                        body = "Your request to join \"${request.projectTitle}\" was declined.",
                        deepLink = "/dashboard",
                    )
                }
                joinDataSource.updateWorkflowStatus(requestId, workflowStatus)
                Unit
            }.toJoinResult(if (workflowStatus == MatchWorkflow.ACCEPTED) "accept join request" else "decline join request")
        }

    private suspend fun mapRequests(
        docs: List<ProjectJoinRequestDoc>,
        currentUserId: String,
        incoming: Boolean,
    ): List<ProjectJoinRequest> {
        if (docs.isEmpty()) return emptyList()
        val userIds = docs.flatMap { listOf(it.fromUserId, it.toUserId) }.distinct()
        val profiles = userDataSource.fetchUserProfiles(userIds)
        return docs.map { doc ->
            ProjectJoinRequest(
                id = doc.id,
                projectId = doc.projectId,
                projectTitle = doc.projectTitle,
                fromUserId = doc.fromUserId,
                toUserId = doc.toUserId,
                fromDisplayName = profiles[doc.fromUserId]?.displayName ?: "Developer",
                requestedRole = doc.requestedRole,
                workflowStatus = doc.workflowStatus,
                statusLabel = workflowStatusLabel(doc.workflowStatus),
                message = doc.message,
                relativeTime = formatRelativeTime(doc.createdAt),
                isIncoming = incoming,
            )
        }
    }

    private fun resolveMemberRole(requestedRole: String): String {
        val normalized = requestedRole.trim().lowercase()
        return when {
            normalized.contains("maintain") -> MemberRole.MAINTAINER
            normalized.contains("view") -> MemberRole.VIEWER
            else -> MemberRole.CONTRIBUTOR
        }
    }

    private fun workflowStatusLabel(status: String): String = when (status) {
        MatchWorkflow.PENDING -> "Pending"
        MatchWorkflow.ACCEPTED -> "Accepted"
        MatchWorkflow.DECLINED -> "Declined"
        else -> status.replaceFirstChar { it.uppercase() }
    }

    private suspend fun requireSignedInUser(): String? {
        val user = firebaseAuth.currentUser ?: return null
        if (user.isEmailVerified) return user.uid
        val profile = userDataSource.fetchUserProfile(user.uid)
        return if (profile?.emailVerified == true) user.uid else null
    }

    private fun authRequiredFailure(): Result<Unit> =
        Result.failure(
            IllegalStateException(
                "Verify your email before joining projects. Open the verify screen from login.",
            ),
        )

    private fun Result<Unit>.toJoinResult(action: String): Result<Unit> = fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { error ->
            val detail = error.message.orEmpty()
            val message = when {
                detail.contains("PERMISSION_DENIED", ignoreCase = true) ->
                    "Join blocked by Firestore rules. Deploy firestore.rules, or the project may be missing ownerUserId in Firestore."
                detail.contains("index", ignoreCase = true) ||
                    detail.contains("FAILED_PRECONDITION", ignoreCase = true) ->
                    "Firestore index missing for project join requests. Deploy firestore.indexes.json."
                detail.isNotBlank() -> detail
                else -> "Couldn't $action. Try again."
            }
            Result.failure(IllegalStateException(message, error))
        },
    )
}
