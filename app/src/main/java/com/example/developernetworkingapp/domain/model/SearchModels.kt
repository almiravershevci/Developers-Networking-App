package com.example.developernetworkingapp.domain.model

data class SearchResult(
    val projectId: String = "",
    val title: String,
    val subtitle: String,
    val stack: String,
    val owner: String,
    val location: String,
    val rolesNeeded: List<String>,
    val membersCount: Int,
    val description: String
)

data class SearchContent(
    val filters: List<String> = emptyList(),
    val results: List<SearchResult> = emptyList(),
    val statusMessage: String? = null,
)
