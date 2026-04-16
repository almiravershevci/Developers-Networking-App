package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.DashboardContent
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun observeDashboardContent(): Flow<DashboardContent>
}
