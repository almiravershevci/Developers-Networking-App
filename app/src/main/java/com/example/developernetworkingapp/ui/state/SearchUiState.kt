package com.example.developernetworkingapp.ui.state

import com.example.developernetworkingapp.domain.model.SearchContent

data class SearchUiState(
    val query: String = "",
    val content: SearchContent? = null,
    val trendingTopics: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
