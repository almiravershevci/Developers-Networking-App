package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.MatchRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface MatchRepository {
    fun observeIncomingRequests(): Flow<List<MatchRequest>>

    fun observeOutgoingRequests(): Flow<List<MatchRequest>>

    suspend fun sendMatchRequest(toUserId: String, message: String?): Result<Unit>

    suspend fun acceptRequest(requestId: String): Result<Unit>

    suspend fun declineRequest(requestId: String): Result<Unit>
}

class FakeMatchRepository : MatchRepository {
    private val demoIncoming = listOf(
        MatchRequest(
            id = "match_demo_incoming",
            fromUserId = "demo_mina_uid",
            toUserId = "current_user",
            fromDisplayName = "Mina K.",
            toDisplayName = "You",
            workflowStatus = "pending",
            statusLabel = "Pending",
            message = "Interested in collaborating on realtime features.",
            relativeTime = "2h ago",
            isIncoming = true,
        ),
    )

    override fun observeIncomingRequests(): Flow<List<MatchRequest>> = flowOf(demoIncoming)

    override fun observeOutgoingRequests(): Flow<List<MatchRequest>> = flowOf(emptyList())

    override suspend fun sendMatchRequest(toUserId: String, message: String?): Result<Unit> =
        Result.success(Unit)

    override suspend fun acceptRequest(requestId: String): Result<Unit> = Result.success(Unit)

    override suspend fun declineRequest(requestId: String): Result<Unit> = Result.success(Unit)
}
