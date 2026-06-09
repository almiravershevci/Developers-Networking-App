package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.AuthUser
import com.example.developernetworkingapp.data.repository.ProfileRepository
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreDashboardDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.formatRelativeTime
import com.example.developernetworkingapp.data.datasource.firebase.schema.ActivityItemDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.UserProfileDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.UserStatsDoc
import com.example.developernetworkingapp.domain.model.ActivityItem
import com.example.developernetworkingapp.domain.model.ProfileContent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Profile microservice: loads the signed-in user's document from Firestore.
 */
class ProfileRepositoryFirestore(
    private val authRepository: AuthRepository,
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource(),
    private val dashboardDataSource: FirestoreDashboardDataSource = FirestoreDashboardDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ProfileRepository {

    override suspend fun updateProfile(displayName: String, headline: String, bio: String): Boolean {
        val uid = firebaseAuth.currentUser?.uid ?: return false
        return runCatching {
            userDataSource.updateUserProfile(uid, displayName, headline, bio)
            true
        }.getOrDefault(false)
    }

    override fun observeProfile(): Flow<ProfileContent> =
        authRepository.currentUser.flatMapLatest { authUser ->
            flow {
                val uid = firebaseAuth.currentUser?.uid
                val content = when {
                    authUser == null || !authUser.isVerified || uid == null -> signedOutProfile()
                    else -> loadProfile(uid, authUser)
                }
                emit(content)
            }
        }.flowOn(Dispatchers.IO)

    private suspend fun loadProfile(uid: String, authUser: AuthUser): ProfileContent {
        val profile = userDataSource.fetchUserProfile(uid)
        val stats = dashboardDataSource.fetchUserStats(uid)
        val activity = runCatching { dashboardDataSource.fetchRecentActivity(uid) }.getOrDefault(emptyList())
        return mapToProfileContent(profile, stats, authUser, activity)
    }

    private fun mapToProfileContent(
        profile: UserProfileDoc?,
        stats: UserStatsDoc?,
        authUser: AuthUser,
        activity: List<ActivityItemDoc>,
    ): ProfileContent {
        val name = profile?.displayName?.takeIf { it.isNotBlank() } ?: authUser.name
        val role = profile?.headline?.takeIf { it.isNotBlank() } ?: "Developer"
        val bio = profile?.bio?.takeIf { it.isNotBlank() }
            ?: "Add a bio in Edit Profile to tell collaborators about your work."
        val stacks = profile?.skillTags?.takeIf { it.isNotEmpty() }
            ?: listOf("Add skills in Edit Profile")
        val portfolio = formatPortfolio(profile)
        val insights = profile?.gitInsightsSummary?.takeIf { it.isNotBlank() }
            ?: "Connect GitHub in a future release to sync commits and PRs."
        return ProfileContent(
            name = name,
            role = role,
            bio = bio,
            stacks = stacks,
            portfolio = portfolio,
            insights = insights,
            statsLine = formatStatsLine(stats),
            activeProjectsCount = stats?.activeProjectsCount ?: 0,
            collaborationsCount = stats?.collaborationsCount ?: 0,
            unreadMessagesCount = stats?.unreadMessagesCount ?: 0,
            openTasksCount = stats?.openTasksCount ?: 0,
            activityItems = activity.map { item ->
                ActivityItem(
                    title = item.summary,
                    time = formatRelativeTime(item.createdAt),
                )
            },
        )
    }

    private fun formatPortfolio(profile: UserProfileDoc?): String {
        val links = profile?.portfolioLinks ?: return "No portfolio links yet."
        return listOfNotNull(
            links.github?.takeIf { it.isNotBlank() }?.let { "GitHub: $it" },
            links.linkedin?.takeIf { it.isNotBlank() }?.let { "LinkedIn: $it" },
            links.portfolio?.takeIf { it.isNotBlank() }?.let { "Site: $it" },
        ).joinToString("\n").ifBlank { "No portfolio links yet." }
    }

    private fun formatStatsLine(stats: UserStatsDoc?): String {
        if (stats == null) {
            return "New member · building your network"
        }
        val ratingPart = stats.ratingAggregate?.let { "⭐ ${"%.1f".format(it)} Rating" }
        val projectsPart = "${stats.activeProjectsCount} Projects"
        return listOfNotNull(ratingPart, projectsPart).joinToString(" • ")
    }

    private fun signedOutProfile(): ProfileContent = ProfileContent(
        name = "Guest",
        role = "Not signed in",
        bio = "Log in to view your Firestore profile.",
        stacks = emptyList(),
        portfolio = "",
        insights = "",
        statsLine = "Sign in to load profile",
    )
}
