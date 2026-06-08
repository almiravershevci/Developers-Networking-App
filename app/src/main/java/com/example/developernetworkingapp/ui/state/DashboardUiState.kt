package com.example.developernetworkingapp.ui.state

import com.example.developernetworkingapp.domain.model.DashboardContent
import com.example.developernetworkingapp.domain.model.MatchRequest
import com.example.developernetworkingapp.domain.model.ProjectPost

data class FeedPostState(
    val id: String,
    val post: ProjectPost,
    val hasLiked: Boolean = false,
    val isExpanded: Boolean = false,
    val isCommentsVisible: Boolean = false,
    val commentDraft: String = "",
    val comments: List<String> = emptyList()
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val content: DashboardContent? = null,
    val feedPosts: List<FeedPostState> = emptyList(),
    val incomingMatchRequests: List<MatchRequest> = emptyList(),
    val outgoingMatchRequests: List<MatchRequest> = emptyList(),
    val matchActionInFlight: String? = null,
    val composerText: String = "",
    val composerStack: String = "",
    val composerBackendNeed: String = "",
    val composerSpotsInput: String = "3",
    val errorMessage: String? = null
)
