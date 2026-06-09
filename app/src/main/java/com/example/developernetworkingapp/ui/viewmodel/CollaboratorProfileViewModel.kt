package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.schema.UserProfileDoc
import com.example.developernetworkingapp.domain.model.CollaboratorProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CollaboratorProfileUiState(
    val isLoading: Boolean = true,
    val profile: CollaboratorProfile? = null,
    val errorMessage: String? = null,
)

class CollaboratorProfileViewModel(
    private val userId: String,
    private val matchScore: Int,
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollaboratorProfileUiState())
    val uiState: StateFlow<CollaboratorProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = CollaboratorProfileUiState(isLoading = true, errorMessage = null)
            val doc = withContext(Dispatchers.IO) {
                runCatching { userDataSource.fetchUserProfile(userId) }.getOrNull()
            }
            _uiState.value = if (doc == null) {
                CollaboratorProfileUiState(
                    isLoading = false,
                    profile = CollaboratorProfile(
                        id = userId,
                        name = "Developer",
                        stack = "Full Stack",
                        location = "Remote",
                        availability = "Profile not found in Firestore",
                        summary = "No user document at users/$userId",
                        matchScore = matchScore,
                    ),
                    errorMessage = "Could not load profile from Firestore.",
                )
            } else {
                CollaboratorProfileUiState(
                    isLoading = false,
                    profile = mapProfile(doc),
                )
            }
        }
    }

    private fun mapProfile(doc: UserProfileDoc): CollaboratorProfile {
        val stacks = doc.skillTags.takeIf { it.isNotEmpty() }?.joinToString(" · ") ?: "Full Stack"
        return CollaboratorProfile(
            id = doc.id,
            name = doc.displayName.ifBlank { doc.usernameLower },
            stack = stacks,
            location = doc.headline.ifBlank { "Remote" },
            availability = "Member on DevConnect",
            summary = doc.bio.ifBlank { "No bio yet." },
            email = doc.email,
            matchScore = matchScore,
        )
    }
}
