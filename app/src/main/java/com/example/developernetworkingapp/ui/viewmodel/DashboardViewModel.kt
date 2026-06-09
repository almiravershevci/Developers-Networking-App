package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.DashboardRepository
import com.example.developernetworkingapp.data.repository.MatchRepository
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
class DashboardViewModel(
    private val repository: DashboardRepository,
    private val matchRepository: MatchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<DashboardUiEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<DashboardUiEvent> = _events.asSharedFlow()
    init {
        loadDashboard()
        observeMatchRequests()
    }

    private fun observeMatchRequests() {
        viewModelScope.launch {
            matchRepository.observeIncomingRequests().collect { requests ->
                _uiState.update { it.copy(incomingMatchRequests = requests) }
            }
        }
        viewModelScope.launch {
            matchRepository.observeOutgoingRequests().collect { requests ->
                _uiState.update { it.copy(outgoingMatchRequests = requests) }
            }
        }
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
            comments = emptyList(),
        )
    }

    fun sendMatchInvite(toUserId: String, message: String?) {
        if (toUserId.isBlank()) {
            notify("Couldn't send invite — collaborator profile is missing.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(matchActionInFlight = "send") }
            val result = matchRepository.sendMatchRequest(toUserId, message)
            _uiState.update { it.copy(matchActionInFlight = null) }
            result.fold(
                onSuccess = { notify("✓ Match invite sent. They'll see it in pending requests.") },
                onFailure = { notify(it.message ?: "Couldn't send match invite.") },
            )
        }
    }

    fun acceptMatchRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(matchActionInFlight = requestId) }
            val result = matchRepository.acceptRequest(requestId)
            _uiState.update { it.copy(matchActionInFlight = null) }
            result.fold(
                onSuccess = { notify("✓ Match accepted — direct chat thread is ready.") },
                onFailure = { notify(it.message ?: "Couldn't accept match request.") },
            )
        }
    }

    fun declineMatchRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(matchActionInFlight = requestId) }
            val result = matchRepository.declineRequest(requestId)
            _uiState.update { it.copy(matchActionInFlight = null) }
            result.fold(
                onSuccess = { notify("Match request declined.") },
                onFailure = { notify(it.message ?: "Couldn't decline match request.") },
            )
        }
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
