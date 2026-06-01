package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.data.datasource.remote.TechTrendsApi

interface TechTrendsRepository {
    suspend fun loadTrendingTopics(): List<String>
}

class ApiTechTrendsRepository(
    private val api: TechTrendsApi
) : TechTrendsRepository {
    override suspend fun loadTrendingTopics(): List<String> {
        val response = api.searchTechStories()
        return response.hits
            .mapNotNull { it.title ?: it.story_title }
            .filter { it.isNotBlank() }
            .distinct()
            .take(8)
    }
}
