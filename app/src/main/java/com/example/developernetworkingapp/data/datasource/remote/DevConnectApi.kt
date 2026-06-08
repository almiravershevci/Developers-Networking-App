package com.example.developernetworkingapp.data.datasource.remote

import retrofit2.http.GET

data class DashboardStatsPayloadDto(
    val activeProjectsCount: Int = 0,
    val openTasksCount: Int = 0,
    val unreadMessagesCount: Int = 0,
    val pendingMatchRequestsCount: Int = 0,
    val collaborationsCount: Int = 0,
    val ratingAggregate: Double? = null,
)

data class DashboardStatsResponseDto(
    val welcomeMessage: String = "",
    val stats: DashboardStatsPayloadDto = DashboardStatsPayloadDto(),
    val source: String = "firestore",
    val projectId: String = "",
)

data class ProjectSummaryDto(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val primaryStackLabel: String = "",
    val ownerUserId: String = "",
    val spotsOpen: Int = 0,
    val memberCount: Int = 0,
)

data class ProjectsResponseDto(
    val projects: List<ProjectSummaryDto> = emptyList(),
    val source: String = "firestore",
)

interface DevConnectApi {
    @GET("api/dashboard/stats")
    suspend fun getDashboardStats(): DashboardStatsResponseDto

    @GET("api/projects")
    suspend fun getProjects(): ProjectsResponseDto
}
