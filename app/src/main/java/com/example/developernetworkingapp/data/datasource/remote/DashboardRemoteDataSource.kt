package com.example.developernetworkingapp.data.datasource.remote

import android.util.Log

/**
 * Hybrid analytics layer — Node REST aggregates over the same Firestore the app uses.
 * Failures are silent; Firestore remains the primary dashboard source.
 */
class DashboardRemoteDataSource(
    private val api: DevConnectApi,
) {
    suspend fun fetchDashboardStats(): DashboardStatsResponseDto? =
        runCatching { api.getDashboardStats() }
            .onFailure { error -> Log.w(TAG, "Dashboard REST stats unavailable", error) }
            .getOrNull()

    suspend fun fetchProjects(): ProjectsResponseDto? =
        runCatching { api.getProjects() }
            .onFailure { error -> Log.w(TAG, "Projects REST feed unavailable", error) }
            .getOrNull()

    companion object {
        private const val TAG = "DashboardRemoteDataSource"
    }
}
