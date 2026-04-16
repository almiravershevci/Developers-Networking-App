package com.example.developernetworkingapp.ui.state

import com.example.developernetworkingapp.domain.model.SearchContent

data class SearchUiState(
    val query: String = "",
    val content: SearchContent? = null
)
