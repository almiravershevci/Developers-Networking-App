package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.DashboardRepository
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreDashboardDataSource
import com.example.developernetworkingapp.data.datasource.firebase.formatRelativeTime
import com.example.developernetworkingapp.data.datasource.firebase.schema.UserStatsDoc
import com.example.developernetworkingapp.domain.model.ActivityItem
import com.example.developernetworkingapp.domain.model.CollaboratorMatch
import com.example.developernetworkingapp.domain.model.DashboardContent
import com.example.developernetworkingapp.domain.model.DashboardStat
import com.example.developernetworkingapp.domain.model.EventHighlight
import com.example.developernetworkingapp.domain.model.FeatureModule
import com.example.developernetworkingapp.domain.model.NewsHighlight
import com.example.developernetworkingapp.domain.model.ProjectHighlight
import com.example.developernetworkingapp.domain.model.ProjectPost
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Dashboard repository backed by Firestore (home feed microservice).
 * Reloads when [AuthRepository.currentUser] changes (e.g. after login).
 */
class DashboardRepositoryFirestore(
    private val authRepository: AuthRepository,
    private val dataSource: FirestoreDashboardDataSource = FirestoreDashboardDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : DashboardRepository {

    override fun observeDashboardContent(): Flow<DashboardContent> =
        authRepository.currentUser.flatMapLatest { authUser ->
            flow {
                val uid = firebaseAuth.currentUser?.uid
                val content = when {
                    authUser == null || !authUser.isVerified || uid == null ->
                        signedOutDashboardContent()
                    else -> loadDashboard(uid, authUser.name)
                }
                emit(content)
            }
        }.flowOn(Dispatchers.IO)

    private suspend fun loadDashboard(uid: String, fallbackName: String): DashboardContent {
        val profile = runCatching { dataSource.fetchUserProfiles(listOf(uid))[uid] }.getOrNull()
        val displayName = profile?.displayName?.takeIf { it.isNotBlank() }
            ?: fallbackName.takeIf { it.isNotBlank() }
            ?: "Developer"

        val statsDoc = runCatching { dataSource.fetchUserStats(uid) }.getOrNull()
        val suggestions = runCatching { dataSource.fetchCollaboratorSuggestions(uid) }.getOrDefault(emptyList())
        val profilesById = runCatching {
            dataSource.fetchUserProfiles(suggestions.map { it.suggestedUserId })
        }.getOrDefault(emptyMap())

        val projects = runCatching { dataSource.fetchRecruitingProjects() }.getOrDefault(emptyList())
        val ownersById = runCatching {
            dataSource.fetchUserProfiles(projects.map { it.ownerUserId })
        }.getOrDefault(emptyMap())

        val news = runCatching { dataSource.fetchNewsHighlights() }.getOrDefault(emptyList())
        val activity = runCatching { dataSource.fetchRecentActivity(uid) }.getOrDefault(emptyList())
        val events = runCatching { dataSource.fetchUpcomingEvents() }.getOrDefault(emptyList())

        return DashboardContent(
            greeting = greetingForHour(displayName),
            heroTitle = "Your network is active",
            heroSubtitle = heroSubtitle(statsDoc),
            stats = buildStats(statsDoc),
            modules = defaultModules(),
            matches = suggestions.map { suggestion ->
                CollaboratorMatch(
                    name = profilesById[suggestion.suggestedUserId]?.displayName ?: "Developer",
                    stack = suggestion.stackSummary,
                    matchScore = suggestion.matchScore,
                )
            },
            projects = projects.take(3).map { project ->
                ProjectHighlight(
                    title = project.title,
                    description = project.subtitle.ifBlank { project.description },
                    progress = project.progressPercent ?: 0,
                )
            },
            projectPosts = projects.map { project ->
                ProjectPost(
                    title = project.title,
                    stack = project.primaryStackLabel.ifBlank { project.stackTags.firstOrNull().orEmpty() },
                    description = project.description,
                    owner = ownersById[project.ownerUserId]?.displayName ?: "Project owner",
                    openRoles = project.openRoleLabels,
                    spotsLeft = project.spotsOpen.coerceAtLeast(0),
                )
            },
            events = events.map { event ->
                EventHighlight(
                    title = event.title,
                    meta = event.summaryLine.ifBlank {
                        "${event.participantCount} participants · ${event.formatKind}"
                    },
                )
            },
            news = news.map { item -> NewsHighlight(title = item.title, source = item.sourceName) },
            activity = activity.map { item ->
                ActivityItem(title = item.summary, time = formatRelativeTime(item.createdAt))
            },
        )
    }

    private fun buildStats(statsDoc: UserStatsDoc?): List<DashboardStat> {
        if (statsDoc == null) {
            return listOf(
                DashboardStat("Active Projects", "0", "Join a project to get started"),
                DashboardStat("Open Tasks", "0", "No tasks yet"),
                DashboardStat("Unread Messages", "0", "Open Chat"),
                DashboardStat("Match Requests", "0", "Explore Search"),
            )
        }
        return listOf(
            DashboardStat("Active Projects", statsDoc.activeProjectsCount.toString(), "From your workspace"),
            DashboardStat("Open Tasks", statsDoc.openTasksCount.toString(), "Across projects"),
            DashboardStat("Unread Messages", statsDoc.unreadMessagesCount.toString(), "Check Chat"),
            DashboardStat("Match Requests", statsDoc.pendingMatchRequestsCount.toString(), "Pending invites"),
        )
    }

    private fun heroSubtitle(statsDoc: UserStatsDoc?): String {
        if (statsDoc == null) {
            return "Explore projects, events, and collaborators in the DevConnect network."
        }
        return "${statsDoc.collaborationsCount} collaborations · ${statsDoc.unreadMessagesCount} unread " +
            "messages · ${statsDoc.pendingMatchRequestsCount} pending matches"
    }

    private fun greetingForHour(displayName: String): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val salutation = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Hello"
        }
        return "$salutation, $displayName"
    }

    private fun defaultModules(): List<FeatureModule> = listOf(
        FeatureModule("Team Matching", "Suggested collaborators from Firestore"),
        FeatureModule("Smart Tasks", "Project tasks synced to your stack"),
        FeatureModule("Live Events", "Hackathons and community sessions"),
    )

    private fun signedOutDashboardContent(): DashboardContent = DashboardContent(
        greeting = "Hello, Developer",
        heroTitle = "Welcome to DevConnect",
        heroSubtitle = "Sign in to load your live dashboard from Firestore.",
        stats = listOf(
            DashboardStat("Active Projects", "—", "Sign in to load"),
            DashboardStat("Open Tasks", "—", "Sign in to load"),
            DashboardStat("Unread Messages", "—", "Sign in to load"),
            DashboardStat("Match Requests", "—", "Sign in to load"),
        ),
        modules = defaultModules(),
        matches = emptyList(),
        projects = emptyList(),
        projectPosts = emptyList(),
        events = emptyList(),
        news = emptyList(),
        activity = emptyList(),
    )
}
