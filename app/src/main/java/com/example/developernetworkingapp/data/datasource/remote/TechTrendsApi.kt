package com.example.developernetworkingapp.data.datasource.remote

import retrofit2.http.GET
import retrofit2.http.Query

data class AlgoliaHitDto(
    val title: String?,
    val story_title: String?
)

data class AlgoliaSearchResponseDto(
    val hits: List<AlgoliaHitDto>
)

interface TechTrendsApi {
    @GET("search")
    suspend fun searchTechStories(
        @Query("query") query: String = "android kotlin",
        @Query("tags") tags: String = "story"
    ): AlgoliaSearchResponseDto
}
