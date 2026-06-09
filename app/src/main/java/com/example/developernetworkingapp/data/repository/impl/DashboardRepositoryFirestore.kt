package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.DashboardRepository
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreDashboardDataSource
import com.example.developernetworkingapp.data.datasource.remote.DashboardRemoteDataSource
import com.example.developernetworkingapp.data.datasource.remote.DashboardStatsPayloadDto
import com.example.developernetworkingapp.data.datasource.remote.DevConnectApiConfig
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Dashboard repository backed by Firestore (home feed microservice).
 * Reloads when [AuthRepository.currentUser] changes (e.g. after login).
 */
class DashboardRepositoryFirestore(
    private val authRepository: AuthRepository,
    private val dataSource: FirestoreDashboardDataSource = FirestoreDashboardDataSource(),
    private val remoteDataSource: DashboardRemoteDataSource? = null,
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

    private suspend fun loadDashboard(uid: String, fallbackName: String): DashboardContent = coroutineScope {
        val profileDeferred = async {
            runCatching { dataSource.fetchUserProfiles(listOf(uid))[uid] }.getOrNull()
        }
        val statsDeferred = async {
            runCatching { dataSource.fetchUserStats(uid) }.getOrNull()
        }
        val suggestionsDeferred = async {
            runCatching { dataSource.fetchCollaboratorSuggestions(uid) }.getOrDefault(emptyList())
        }
        val recruitingProjectsDeferred = async {
            runCatching { dataSource.fetchRecruitingProjects() }.getOrDefault(emptyList())
        }
        val ownedProjectsDeferred = async {
            runCatching { dataSource.fetchOwnedProjects(uid) }.getOrDefault(emptyList())
        }
        val newsDeferred = async {
            runCatching { dataSource.fetchNewsHighlights() }.getOrDefault(emptyList())
        }
        val activityDeferred = async {
            runCatching { dataSource.fetchRecentActivity(uid) }.getOrDefault(emptyList())
        }
        val inboxActivityDeferred = async {
            runCatching { dataSource.fetchInboxActivity(uid) }.getOrDefault(emptyList())
        }
        val eventsDeferred = async {
            runCatching { dataSource.fetchUpcomingEvents() }.getOrDefault(emptyList())
        }

        val profile = profileDeferred.await()
        val displayName = profile?.displayName?.takeIf { it.isNotBlank() }
            ?: fallbackName.takeIf { it.isNotBlank() }
            ?: "Developer"

        val statsDoc = statsDeferred.await()
        val suggestions = suggestionsDeferred.await()
        val fallbackProfiles = if (suggestions.isEmpty()) {
            runCatching { dataSource.fetchPublicUsersExcluding(uid) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
        val profilesById = runCatching {
            val ids = suggestions.map { it.suggestedUserId } + fallbackProfiles.map { it.id }
            dataSource.fetchUserProfiles(ids)
        }.getOrDefault(emptyMap())

        var projects = recruitingProjectsDeferred.await()
        if (projects.isEmpty()) {
            projects = ownedProjectsDeferred.await()
        }
        val ownersById = runCatching {
            dataSource.fetchUserProfiles(projects.map { it.ownerUserId })
        }.getOrDefault(emptyMap())

        val news = newsDeferred.await()
        var activity = activityDeferred.await()
        if (activity.isEmpty()) {
            activity = inboxActivityDeferred.await()
        }
        val events = eventsDeferred.await()

        val firestoreContent = DashboardContent(
            greeting = greetingForHour(displayName),
            heroTitle = "Your network is active",
            heroSubtitle = heroSubtitle(statsDoc),
            stats = buildStats(statsDoc),
            modules = defaultModules(),
            matches = if (suggestions.isNotEmpty()) {
                suggestions.map { suggestion ->
                    CollaboratorMatch(
                        suggestedUserId = suggestion.suggestedUserId,
                        name = profilesById[suggestion.suggestedUserId]?.displayName ?: "Developer",
                        stack = suggestion.stackSummary,
                        matchScore = suggestion.matchScore,
                    )
                }
            } else {
                fallbackProfiles.mapIndexed { index, profile ->
                    CollaboratorMatch(
                        suggestedUserId = profile.id,
                        name = profile.displayName.ifBlank { "Developer" },
                        stack = profile.skillTags.take(3).joinToString(" · ").ifBlank { profile.headline },
                        matchScore = 88 - index * 3,
                    )
                }
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
        overlayRemoteAnalytics(firestoreContent)
    }

    private suspend fun overlayRemoteAnalytics(content: DashboardContent): DashboardContent {
        if (!DevConnectApiConfig.ENABLED) return content
        val remoteApi = remoteDataSource ?: return content
        val remote = withTimeoutOrNull(REMOTE_STATS_TIMEOUT_MS) {
            runCatching { remoteApi.fetchDashboardStats() }.getOrNull()
        } ?: return content
        val stats = remote.stats
        return content.copy(
            greeting = remote.welcomeMessage.takeIf { it.isNotBlank() } ?: content.greeting,
            heroSubtitle = "${stats.collaborationsCount} collaborations · ${stats.unreadMessagesCount} unread " +
                "messages · ${stats.pendingMatchRequestsCount} pending matches",
            stats = buildStatsFromRemote(stats),
            analyticsSourceLine = "Analytics microservice · Node REST (${remote.source})",
        )
    }

    private fun buildStatsFromRemote(stats: DashboardStatsPayloadDto): List<DashboardStat> = listOf(
        DashboardStat("Active Projects", stats.activeProjectsCount.toString(), "Node API aggregate"),
        DashboardStat("Open Tasks", stats.openTasksCount.toString(), "Across projects"),
        DashboardStat("Unread Messages", stats.unreadMessagesCount.toString(), "Check Chat"),
        DashboardStat("Match Requests", stats.pendingMatchRequestsCount.toString(), "Pending invites"),
    )

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

    private companion object {
        const val REMOTE_STATS_TIMEOUT_MS = 3_000L
    }

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
