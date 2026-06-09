package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.ProjectJoinRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ProjectJoinRepository {
    fun observeIncomingRequests(): Flow<List<ProjectJoinRequest>>
    fun observeOutgoingRequests(): Flow<List<ProjectJoinRequest>>
    suspend fun sendJoinRequest(
        projectId: String,
        projectTitle: String,
        ownerUserId: String,
        requestedRole: String,
        message: String?,
    ): Result<Unit>
    suspend fun acceptRequest(requestId: String): Result<Unit>
    suspend fun declineRequest(requestId: String): Result<Unit>
    suspend fun membershipProjectIds(userId: String): Set<String>
}

class FakeProjectJoinRepository : ProjectJoinRepository {
    override fun observeIncomingRequests(): Flow<List<ProjectJoinRequest>> = flowOf(emptyList())
    override fun observeOutgoingRequests(): Flow<List<ProjectJoinRequest>> = flowOf(emptyList())
    override suspend fun sendJoinRequest(
        projectId: String,
        projectTitle: String,
        ownerUserId: String,
        requestedRole: String,
        message: String?,
    ): Result<Unit> = Result.success(Unit)
    override suspend fun acceptRequest(requestId: String): Result<Unit> = Result.success(Unit)
    override suspend fun declineRequest(requestId: String): Result<Unit> = Result.success(Unit)
    override suspend fun membershipProjectIds(userId: String): Set<String> = emptySet()
}
