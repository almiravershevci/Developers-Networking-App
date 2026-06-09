package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.DashboardRepository
import com.example.developernetworkingapp.data.repository.MatchRepository
import com.example.developernetworkingapp.data.repository.ProjectJoinRepository
import com.example.developernetworkingapp.data.repository.ProjectsRepository
import com.example.developernetworkingapp.domain.model.ProjectJoinStatus
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DashboardRepository,
    private val matchRepository: MatchRepository,
    private val projectJoinRepository: ProjectJoinRepository,
    private val projectsRepository: ProjectsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<DashboardUiEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<DashboardUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.observeDashboardContent().collect { content ->
                _uiState.update { state ->
                    val pendingIds = state.pendingJoinProjectIds
                    state.copy(
                        isLoading = false,
                        content = content,
                        feedPosts = content.projectPosts.mapIndexed { index, post ->
                            val resolvedPost = applyPendingJoinStatus(post, pendingIds)
                            mergeFeedPostState(
                                existing = state.feedPosts.find { feed ->
                                    feed.post.projectId == resolvedPost.projectId && resolvedPost.projectId.isNotBlank()
                                },
                                post = resolvedPost,
                                id = resolvedPost.projectId.ifBlank { "${resolvedPost.title.hashCode()}-$index" },
                            )
                        },
                        errorMessage = null,
                    )
                }
            }
        }
        observeMatchRequests()
        observeProjectJoinRequests()
        observeOutgoingJoinRequests()
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

    private fun observeOutgoingJoinRequests() {
        viewModelScope.launch {
            projectJoinRepository.observeOutgoingRequests()
                .catch { /* Outgoing listener optional; optimistic UI covers pending state. */ }
                .collect { outgoing ->
                val pendingIds = outgoing.map { it.projectId }.toSet()
                _uiState.update { state ->
                    state.copy(
                        pendingJoinProjectIds = pendingIds,
                        feedPosts = state.feedPosts.map { postState ->
                            postState.copy(
                                post = applyPendingJoinStatus(postState.post, pendingIds),
                            )
                        },
                    )
                }
            }
        }
    }

    private fun observeProjectJoinRequests() {
        viewModelScope.launch {
            projectJoinRepository.observeIncomingRequests()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            incomingProjectJoinRequests = emptyList(),
                            joinRequestLoadError = mapJoinRequestLoadError(error),
                        )
                    }
                }
                .collect { requests ->
                    _uiState.update {
                        it.copy(
                            incomingProjectJoinRequests = requests,
                            joinRequestLoadError = null,
                        )
                    }
                }
        }
    }

    private fun mapJoinRequestLoadError(error: Throwable): String {
        val detail = error.message.orEmpty()
        return when {
            detail.contains("PERMISSION_DENIED", ignoreCase = true) ->
                "Join requests blocked by Firestore rules. Deploy the latest firestore.rules."
            detail.contains("index", ignoreCase = true) ||
                detail.contains("FAILED_PRECONDITION", ignoreCase = true) ->
                "Firestore index missing for join requests. Run: firebase deploy --only firestore"
            else -> "Couldn't load join requests. $detail"
        }
    }

    fun loadDashboard() {
        repository.invalidateDashboard()
    }

    fun refreshFeed() {
        repository.invalidateDashboard()
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
                            commentDraft = "",
                        )
                    }
                }
            })
        }
    }

    fun sendProjectJoinRequest(
        projectId: String,
        projectTitle: String,
        ownerUserId: String,
        requestedRole: String,
        message: String?,
        feedPostKey: String,
    ) {
        if (projectId.isBlank()) {
            val error = "Cannot join: this feed post is missing a project id."
            _uiState.update { it.copy(joinActionError = error) }
            notify(error)
            return
        }
        markProjectJoinPending(projectId = projectId, feedPostKey = feedPostKey)
        viewModelScope.launch {
            _uiState.update { it.copy(projectJoinActionInFlight = projectId, joinActionError = null) }
            val result = projectJoinRepository.sendJoinRequest(
                projectId = projectId,
                projectTitle = projectTitle,
                ownerUserId = ownerUserId,
                requestedRole = requestedRole,
                message = message,
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(projectJoinActionInFlight = null, joinActionError = null) }
                    notify("✓ Join request sent to the project owner.")
                },
                onFailure = { error ->
                    clearProjectJoinPending(projectId = projectId, feedPostKey = feedPostKey)
                    val failureMessage = error.message ?: "Couldn't send join request."
                    _uiState.update {
                        it.copy(
                            projectJoinActionInFlight = null,
                            joinActionError = failureMessage,
                        )
                    }
                    notify(failureMessage)
                },
            )
        }
    }

    private fun markProjectJoinPending(projectId: String, feedPostKey: String) {
        _uiState.update { state ->
            val pendingIds = state.pendingJoinProjectIds + projectId
            state.copy(
                pendingJoinProjectIds = pendingIds,
                feedPosts = state.feedPosts.map { postState ->
                    if (matchesJoinTarget(postState, projectId, feedPostKey)) {
                        postState.copy(
                            post = postState.post.copy(joinStatus = ProjectJoinStatus.PENDING),
                        )
                    } else {
                        postState
                    }
                },
            )
        }
    }

    private fun clearProjectJoinPending(projectId: String, feedPostKey: String) {
        _uiState.update { state ->
            val pendingIds = state.pendingJoinProjectIds - projectId
            state.copy(
                pendingJoinProjectIds = pendingIds,
                feedPosts = state.feedPosts.map { postState ->
                    if (matchesJoinTarget(postState, projectId, feedPostKey)) {
                        postState.copy(
                            post = postState.post.copy(joinStatus = ProjectJoinStatus.AVAILABLE),
                        )
                    } else {
                        postState
                    }
                },
            )
        }
    }

    private fun matchesJoinTarget(
        postState: FeedPostState,
        projectId: String,
        feedPostKey: String,
    ): Boolean = postState.id == feedPostKey ||
        (projectId.isNotBlank() && postState.post.projectId == projectId)

    private fun applyPendingJoinStatus(post: ProjectPost, pendingIds: Set<String>): ProjectPost {
        if (post.projectId.isBlank() || pendingIds.isEmpty()) return post
        if (post.joinStatus == ProjectJoinStatus.OWNER || post.joinStatus == ProjectJoinStatus.MEMBER) {
            return post
        }
        return if (pendingIds.contains(post.projectId)) {
            post.copy(joinStatus = ProjectJoinStatus.PENDING)
        } else {
            post
        }
    }

    private fun mergeFeedPostState(
        existing: FeedPostState?,
        post: ProjectPost,
        id: String,
    ): FeedPostState {
        if (existing == null) return createFeedPostState(post = post, id = id)
        val mergedPost = if (
            existing.post.joinStatus == ProjectJoinStatus.PENDING &&
            post.joinStatus == ProjectJoinStatus.AVAILABLE
        ) {
            post.copy(joinStatus = ProjectJoinStatus.PENDING)
        } else {
            post
        }
        return existing.copy(
            id = id,
            post = mergedPost,
        )
    }

    fun acceptProjectJoinRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(projectJoinActionInFlight = requestId) }
            val result = projectJoinRepository.acceptRequest(requestId)
            _uiState.update { it.copy(projectJoinActionInFlight = null) }
            result.fold(
                onSuccess = {
                    repository.invalidateDashboard()
                    projectsRepository.invalidateProjects()
                    notify("✓ Collaborator added to the project. They can now see assigned tasks.")
                },
                onFailure = { notify(it.message ?: "Couldn't accept join request.") },
            )
        }
    }

    fun declineProjectJoinRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(projectJoinActionInFlight = requestId) }
            val result = projectJoinRepository.declineRequest(requestId)
            _uiState.update { it.copy(projectJoinActionInFlight = null) }
            result.fold(
                onSuccess = { notify("Join request declined.") },
                onFailure = { notify(it.message ?: "Couldn't decline join request.") },
            )
        }
    }

    fun createProject(title: String, description: String, stackLabel: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingProject = true, createProjectError = null) }
            try {
                val result = projectsRepository.createProject(title, description, stackLabel)
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(isCreatingProject = false, showCreateProjectDialog = false, createProjectError = null)
                        }
                        repository.invalidateDashboard()
                        projectsRepository.invalidateProjects()
                        notify("✓ Project \"$title\" created and published to everyone's feed.")
                    },
                    onFailure = { error ->
                        val message = error.message ?: "Could not create project."
                        _uiState.update {
                            it.copy(isCreatingProject = false, createProjectError = message)
                        }
                        notify(message)
                    },
                )
            } finally {
                _uiState.update { it.copy(isCreatingProject = false) }
            }
        }
    }

    fun setShowCreateProjectDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateProjectDialog = show, createProjectError = null) }
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

