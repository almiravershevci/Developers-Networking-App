package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.datasource.firebase.FirestoreChatDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreMatchRequestsDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.authStateChanges
import com.example.developernetworkingapp.data.datasource.firebase.formatRelativeTime
import com.example.developernetworkingapp.data.datasource.firebase.schema.MatchRequestDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.MatchWorkflow
import com.example.developernetworkingapp.data.repository.MatchRepository
import com.example.developernetworkingapp.domain.model.MatchRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Match requests microservice — realtime pending invites and accept/decline writes.
 */
class MatchRepositoryFirestore(
    private val matchDataSource: FirestoreMatchRequestsDataSource = FirestoreMatchRequestsDataSource(),
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource(),
    private val chatDataSource: FirestoreChatDataSource = FirestoreChatDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : MatchRepository {

    override fun observeIncomingRequests(): Flow<List<MatchRequest>> =
        firebaseAuth.authStateChanges().flatMapLatest { firebaseUser ->
            when {
                firebaseUser == null || !firebaseUser.isEmailVerified -> flowOf(emptyList())
                else -> matchDataSource.observeIncomingPending(firebaseUser.uid)
                    .map { docs -> mapRequests(docs, currentUserId = firebaseUser.uid, incoming = true) }
            }
        }.flowOn(Dispatchers.IO)

    override fun observeOutgoingRequests(): Flow<List<MatchRequest>> =
        firebaseAuth.authStateChanges().flatMapLatest { firebaseUser ->
            when {
                firebaseUser == null || !firebaseUser.isEmailVerified -> flowOf(emptyList())
                else -> matchDataSource.observeOutgoingPending(firebaseUser.uid)
                    .map { docs -> mapRequests(docs, currentUserId = firebaseUser.uid, incoming = false) }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun sendMatchRequest(toUserId: String, message: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val uid = requireSignedInUser() ?: return@withContext authRequiredFailure()
            if (toUserId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Recipient is required."))
            }
            if (toUserId == uid) {
                return@withContext Result.failure(IllegalArgumentException("You cannot invite yourself."))
            }
            runCatching {
                matchDataSource.createMatchRequest(
                    fromUserId = uid,
                    toUserId = toUserId,
                    message = message,
                )
                Unit
            }.toMatchResult("send match request")
        }

    override suspend fun acceptRequest(requestId: String): Result<Unit> =
        resolveRequest(requestId, MatchWorkflow.ACCEPTED, createConversation = true)

    override suspend fun declineRequest(requestId: String): Result<Unit> =
        resolveRequest(requestId, MatchWorkflow.DECLINED, createConversation = false)

    private suspend fun resolveRequest(
        requestId: String,
        workflowStatus: String,
        createConversation: Boolean,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireSignedInUser() ?: return@withContext authRequiredFailure()
        runCatching {
            val request = matchDataSource.fetchMatchRequest(requestId)
                ?: error("Match request not found.")
            if (request.workflowStatus != MatchWorkflow.PENDING) {
                error("This request was already resolved.")
            }
            if (request.toUserId != uid) {
                error("Only the recipient can accept or decline this request.")
            }
            matchDataSource.updateWorkflowStatus(requestId, workflowStatus)
            if (createConversation && workflowStatus == MatchWorkflow.ACCEPTED) {
                chatDataSource.ensureDirectConversation(
                    currentUserId = request.toUserId,
                    peerUserId = request.fromUserId,
                )
            }
            Unit
        }.toMatchResult(if (workflowStatus == MatchWorkflow.ACCEPTED) "accept request" else "decline request")
    }

    private suspend fun mapRequests(
        docs: List<MatchRequestDoc>,
        currentUserId: String,
        incoming: Boolean,
    ): List<MatchRequest> {
        if (docs.isEmpty()) return emptyList()
        val userIds = docs.flatMap { listOf(it.fromUserId, it.toUserId) }.distinct()
        val profiles = userDataSource.fetchUserProfiles(userIds)
        return docs.map { doc ->
            doc.toMatchRequest(
                currentUserId = currentUserId,
                incoming = incoming,
                fromDisplayName = profiles[doc.fromUserId]?.displayName ?: "Developer",
                toDisplayName = profiles[doc.toUserId]?.displayName ?: "Developer",
            )
        }
    }

    private fun MatchRequestDoc.toMatchRequest(
        currentUserId: String,
        incoming: Boolean,
        fromDisplayName: String,
        toDisplayName: String,
    ): MatchRequest = MatchRequest(
        id = id,
        fromUserId = fromUserId,
        toUserId = toUserId,
        fromDisplayName = fromDisplayName,
        toDisplayName = toDisplayName,
        workflowStatus = workflowStatus,
        statusLabel = workflowStatusLabel(workflowStatus),
        message = message,
        relativeTime = formatRelativeTime(createdAt),
        isIncoming = incoming,
    )

    private fun workflowStatusLabel(status: String): String = when (status) {
        MatchWorkflow.PENDING -> "Pending"
        MatchWorkflow.ACCEPTED -> "Accepted"
        MatchWorkflow.DECLINED -> "Declined"
        MatchWorkflow.CANCELLED -> "Cancelled"
        else -> status.replaceFirstChar { it.uppercase() }
    }

    private fun requireSignedInUser() = firebaseAuth.currentUser?.takeIf { it.isEmailVerified }?.uid

    private fun authRequiredFailure(): Result<Unit> =
        Result.failure(IllegalStateException("Sign in with a verified email to manage match requests."))

    private fun Result<Unit>.toMatchResult(action: String): Result<Unit> = fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { error ->
            val detail = error.message.orEmpty()
            val message = when {
                detail.contains("PERMISSION_DENIED", ignoreCase = true) ->
                    "Match request blocked by Firestore rules. Publish firestore.rules and retry."
                detail.contains("FAILED_PRECONDITION", ignoreCase = true) ||
                    detail.contains("index", ignoreCase = true) ->
                    "Firestore needs a matchRequests index. Deploy firestore.indexes.json, then retry."
                detail.isNotBlank() -> detail
                else -> "Couldn't $action. Try again."
            }
            Result.failure(IllegalStateException(message, error))
        },
    )
}
