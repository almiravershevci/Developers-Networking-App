package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.DashboardRepository
import com.example.developernetworkingapp.domain.model.ProjectPost
import com.example.developernetworkingapp.ui.state.DashboardUiState
import com.example.developernetworkingapp.ui.state.FeedPostState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class DashboardViewModel(
    private val repository: DashboardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<DashboardUiEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<DashboardUiEvent> = _events.asSharedFlow()
    private var dynamicPostCounter: Int = 0

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.observeDashboardContent().collect { content ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        content = content,
                        feedPosts = content.projectPosts.mapIndexed { index, post ->
                            createFeedPostState(post = post, id = "${post.title.hashCode()}-$index")
                        },
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun refreshFeed() {
        loadDashboard()
        notify("Feed refreshed with latest activity.")
    }

    fun submitComposerPost() {
        val state = _uiState.value
        val trimmedText = state.composerText.trim()
        if (trimmedText.isBlank()) {
            notify("Add a short project update before posting.")
            return
        }
        val spots = state.composerSpotsInput.toIntOrNull()?.coerceIn(1, 20) ?: 3
        val resolvedBackendNeed = state.composerBackendNeed.takeIf { it.isNotBlank() } ?: "Backend (any stack)"
        val post = ProjectPost(
            title = trimmedText.take(48),
            stack = state.composerStack.ifBlank { "General Stack" },
            description = trimmedText,
            owner = "You",
            openRoles = listOf("Mobile", resolvedBackendNeed, "UI/UX"),
            spotsLeft = spots
        )
        dynamicPostCounter += 1
        _uiState.update { state ->
            state.copy(
                feedPosts = listOf(
                    createFeedPostState(post = post, id = "user-${dynamicPostCounter}-${post.title.hashCode().absoluteValue}")
                ) + state.feedPosts,
                composerText = "",
                composerStack = "",
                composerBackendNeed = "",
                composerSpotsInput = "3"
            )
        }
        notify("Project update posted to your feed.")
    }

    fun updateComposerText(value: String) {
        _uiState.update { it.copy(composerText = value) }
    }

    fun updateComposerStack(value: String) {
        _uiState.update { it.copy(composerStack = value) }
    }

    fun updateComposerBackendNeed(value: String) {
        _uiState.update { it.copy(composerBackendNeed = value) }
    }

    fun updateComposerSpotsInput(value: String) {
        _uiState.update { it.copy(composerSpotsInput = value.filter { ch -> ch.isDigit() }.take(2)) }
    }

    fun togglePostLike(postId: String) {
        _uiState.update { state ->
            state.copy(feedPosts = state.feedPosts.map { postState ->
                if (postState.id == postId) postState.copy(hasLiked = !postState.hasLiked) else postState
            })
        }
    }

    fun togglePostExpanded(postId: String) {
        _uiState.update { state ->
            state.copy(feedPosts = state.feedPosts.map { postState ->
                if (postState.id == postId) postState.copy(isExpanded = !postState.isExpanded) else postState
            })
        }
    }

    fun toggleCommentsVisibility(postId: String) {
        _uiState.update { state ->
            state.copy(feedPosts = state.feedPosts.map { postState ->
                if (postState.id == postId) postState.copy(isCommentsVisible = !postState.isCommentsVisible) else postState
            })
        }
    }

    fun updateCommentDraft(postId: String, value: String) {
        _uiState.update { state ->
            state.copy(feedPosts = state.feedPosts.map { postState ->
                if (postState.id == postId) postState.copy(commentDraft = value) else postState
            })
        }
    }

    fun submitComment(postId: String) {
        _uiState.update { state ->
            state.copy(feedPosts = state.feedPosts.map { postState ->
                if (postState.id != postId) {
                    postState
                } else {
                    val trimmed = postState.commentDraft.trim()
                    if (trimmed.isBlank()) postState else {
                        postState.copy(
                            comments = postState.comments + "You: $trimmed",
                            commentDraft = ""
                        )
                    }
                }
            })
        }
    }

    fun notifyProjectApplicationSubmitted() {
        notify("✓ Application submitted! We'll notify you when the project owner reviews your request.")
    }

    private fun createFeedPostState(post: ProjectPost, id: String): FeedPostState {
        return FeedPostState(
            id = id,
            post = post,
            comments = listOf(
                "${post.owner}: Looking for builders who can ship quickly this week.",
                "Lina: Love this idea. I can help with testing and release flow.",
                "Omar: I can support API integration if backend endpoints are ready."
            )
        )
    }

    private fun notify(message: String) {
        viewModelScope.launch {
            _events.emit(DashboardUiEvent.ShowNotification(message))
        }
    }
}

sealed interface DashboardUiEvent {
    data class ShowNotification(val message: String) : DashboardUiEvent
}
