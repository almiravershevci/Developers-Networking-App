package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.repository.AdminRepository
import com.example.developernetworkingapp.data.repository.InMemoryAdminRepository
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreAdminDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.formatRelativeTime
import com.example.developernetworkingapp.data.datasource.firebase.schema.AccountRole
import com.example.developernetworkingapp.data.datasource.firebase.schema.MatchRequestDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectLifecycle
import com.example.developernetworkingapp.data.datasource.firebase.schema.UserProfileDoc
import com.example.developernetworkingapp.domain.model.AdminDashboardSnapshot
import com.example.developernetworkingapp.domain.model.AdminPermissionPreset
import com.example.developernetworkingapp.domain.model.AdminProjectRow
import com.example.developernetworkingapp.domain.model.AdminProjectStatus
import com.example.developernetworkingapp.domain.model.AdminUserRow
import com.example.developernetworkingapp.domain.model.AdminUserStatus
import com.example.developernetworkingapp.domain.model.AuditLogEntry
import com.example.developernetworkingapp.domain.model.ContentQueueItem
import com.example.developernetworkingapp.domain.model.ContentReportRow
import com.example.developernetworkingapp.domain.model.OutboundNotificationRow
import com.example.developernetworkingapp.domain.model.PlatformSettingsSnapshot
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Admin console backed by Firestore (users, projects, inbox broadcast, match queue).
 * Moderation actions require [AccountRole.ADMIN] on the signed-in user profile.
 */
class AdminRepositoryFirestore(
    private val adminDataSource: FirestoreAdminDataSource = FirestoreAdminDataSource(),
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AdminRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _snapshot = MutableStateFlow(AdminDashboardSnapshot.empty())
    override val snapshot: StateFlow<AdminDashboardSnapshot> = _snapshot.asStateFlow()

    private val localOutbound = mutableListOf<OutboundNotificationRow>()
    private var platformSettings = PlatformSettingsSnapshot(
        defaultNotificationsEnabled = true,
        strictTransportEncryption = true,
        analyticsSharingEnabled = false,
        themeDraftLabel = "Default theme",
    )
    private var analyticsExportsTotal = 0
    private var nextPushSlotLabel = "None scheduled"

    init {
        scope.launch { refreshCatalog() }
    }

    override fun deactivateUser(userId: String) {
        scope.launch {
            runAdminAction("Deactivated user $userId") {
                adminDataSource.setUserAccountRole(userId, AccountRole.DEACTIVATED)
            }
        }
    }

    override fun activateUser(userId: String) {
        scope.launch {
            runAdminAction("Activated user $userId") {
                adminDataSource.setUserAccountRole(userId, AccountRole.USER)
            }
        }
    }

    override fun banUser(userId: String) {
        scope.launch {
            runAdminAction("Banned user $userId") {
                adminDataSource.setUserAccountRole(userId, AccountRole.BANNED)
            }
        }
    }

    override fun updateUserProfile(userId: String, techStack: String, bio: String) {
        scope.launch {
            val tags = techStack.split(",", "•", "|")
                .map { it.trim() }
                .filter { it.isNotBlank() }
            runAdminAction("Updated profile fields for $userId") {
                adminDataSource.updateUserModerationFields(userId, tags, bio)
            }
        }
    }

    override fun approveProject(projectId: String) {
        scope.launch {
            runAdminAction("Approved project $projectId") {
                adminDataSource.updateProjectFields(
                    projectId = projectId,
                    title = projectTitleOrBlank(projectId),
                    primaryStackLabel = projectStackOrBlank(projectId),
                    lifecycleStatus = ProjectLifecycle.ACTIVE,
                )
            }
        }
    }

    override fun rejectProject(projectId: String) {
        scope.launch {
            runAdminAction("Rejected project $projectId") {
                adminDataSource.updateProjectFields(
                    projectId = projectId,
                    title = projectTitleOrBlank(projectId),
                    primaryStackLabel = projectStackOrBlank(projectId),
                    lifecycleStatus = ProjectLifecycle.ARCHIVED,
                )
            }
        }
    }

    override fun archiveProject(projectId: String) {
        scope.launch {
            runAdminAction("Archived project $projectId") {
                adminDataSource.updateProjectFields(
                    projectId = projectId,
                    title = projectTitleOrBlank(projectId),
                    primaryStackLabel = projectStackOrBlank(projectId),
                    lifecycleStatus = ProjectLifecycle.ARCHIVED,
                )
            }
        }
    }

    override fun updateProject(projectId: String, name: String, techSummary: String) {
        scope.launch {
            runAdminAction("Edited project metadata $projectId") {
                adminDataSource.updateProjectFields(
                    projectId = projectId,
                    title = name,
                    primaryStackLabel = techSummary,
                )
            }
        }
    }

    override fun approveAllQueuedContent() {
        scope.launch {
            val pending = _snapshot.value.contentQueue
            runAdminAction("Approved ${pending.size} pending match requests") {
                pending.forEach { item ->
                    runCatching { adminDataSource.resolveMatchRequest(item.id, accepted = true) }
                }
            }
        }
    }

    override fun holdQueuedContentForReview() {
        scope.launch { audit("Held queued match requests for extended review") }
    }

    override fun removeReportedContent(reportId: String) {
        scope.launch { audit("Removed content for report $reportId (local moderation log)") }
    }

    override fun dismissReport(reportId: String) {
        scope.launch { audit("Dismissed report $reportId (local moderation log)") }
    }

    override fun sendNotification(title: String, body: String, audience: String) {
        scope.launch {
            runAdminAction("Sent notification: ${title.trim()}") {
                val recipients = resolveAudienceUserIds(audience)
                val sent = adminDataSource.broadcastInboxNotification(recipients, title, body)
                val row = OutboundNotificationRow(
                    id = UUID.randomUUID().toString(),
                    title = title.trim(),
                    body = body.trim(),
                    audience = "$audience ($sent delivered)",
                    sentAtLabel = "Just now",
                )
                localOutbound.add(0, row)
            }
        }
    }

    override fun scheduleNextPushSlot() {
        scope.launch {
            nextPushSlotLabel = "Tomorrow 09:00 (local)"
            audit("Scheduled push slot on topic ${_snapshot.value.pushTopic}")
            refreshCatalog()
        }
    }

    override fun exportAnalyticsCsv(): String {
        val s = _snapshot.value
        val csv = buildString {
            appendLine("metric,value")
            appendLine("total_users,${s.users.size}")
            appendLine("active_projects,${s.projects.count { it.status == AdminProjectStatus.ACTIVE }}")
            appendLine(
                "pending_approvals,${s.projects.count { it.status == AdminProjectStatus.PENDING_APPROVAL }}",
            )
            appendLine("pending_matches,${s.contentQueue.size}")
        }
        analyticsExportsTotal++
        audit("Exported analytics CSV")
        scope.launch { refreshCatalog() }
        return csv
    }

    override fun setDefaultNotifications(enabled: Boolean) {
        platformSettings = platformSettings.copy(defaultNotificationsEnabled = enabled)
        audit("Platform default notifications → $enabled")
        scope.launch { refreshCatalog() }
    }

    override fun setStrictEncryption(enabled: Boolean) {
        platformSettings = platformSettings.copy(strictTransportEncryption = enabled)
        audit("Strict TLS enforcement → $enabled")
        scope.launch { refreshCatalog() }
    }

    override fun setAnalyticsSharing(enabled: Boolean) {
        platformSettings = platformSettings.copy(analyticsSharingEnabled = enabled)
        audit("Product analytics sharing → $enabled")
        scope.launch { refreshCatalog() }
    }

    override fun rotateIntegrationKeys() {
        scope.launch { audit("Rotated integration API keys (record in secret manager)") }
    }

    override fun saveThemeDraft() {
        scope.launch {
            platformSettings = platformSettings.copy(themeDraftLabel = "Draft saved ${nowLabel()}")
            audit("Saved branding / theme draft")
            refreshCatalog()
        }
    }

    override fun assignTicket(ticketId: String) {
        scope.launch { audit("Assigned ticket $ticketId (support desk local)") }
    }

    override fun closeTicket(ticketId: String) {
        scope.launch { audit("Closed ticket $ticketId (support desk local)") }
    }

    override fun addHelpArticleStub() {
        scope.launch { audit("Created help article draft #${UUID.randomUUID().toString().take(8)}") }
    }

    override fun updateAdminPreset(adminId: String, preset: AdminPermissionPreset) {
        scope.launch { audit("Updated RBAC preset for admin $adminId → $preset") }
    }

    override fun bulkExportUsersCsv(): String {
        val users = _snapshot.value.users
        val rows = users.joinToString("\n") { "${it.id},${it.username},${it.email},${it.status}" }
        audit("Bulk exported ${users.size} users to CSV")
        return "id,username,email,status\n$rows"
    }

    private suspend fun runAdminAction(auditMessage: String, block: suspend () -> Unit) {
        withContext(Dispatchers.IO) {
            runCatching {
                requireAdmin()
                block()
                audit(auditMessage)
                refreshCatalog()
            }.onFailure { error ->
                audit("Action failed: ${error.message}")
            }
        }
    }

    private suspend fun refreshCatalog() {
        if (!isCurrentUserAdmin()) {
            _snapshot.value = AdminDashboardSnapshot.empty(
                catalogSourceLabel = "Admin only — set accountRole=admin on your user doc",
            )
            return
        }

        val users = runCatching { adminDataSource.fetchDirectoryUsers() }.getOrDefault(emptyList())
        val projects = runCatching { adminDataSource.fetchDirectoryProjects() }.getOrDefault(emptyList())
        val pendingMatches = runCatching { adminDataSource.fetchPendingMatchRequests() }.getOrDefault(emptyList())
        val inbox = runCatching { adminDataSource.fetchRecentInbox() }.getOrDefault(emptyList())
        val ownerProfiles = runCatching {
            userDataSource.fetchUserProfiles(projects.map { it.ownerUserId }.distinct())
        }.getOrDefault(emptyMap())

        val adminUid = firebaseAuth.currentUser?.uid.orEmpty()
        val adminProfile = userDataSource.fetchUserProfile(adminUid)

        _snapshot.update { current ->
            current.copy(
                catalogSourceLabel = "Firestore · ${users.size} users · ${projects.size} projects",
                users = users.map { it.toAdminUserRow() },
                projects = projects.map { project ->
                    project.toAdminProjectRow(ownerProfiles[project.ownerUserId]?.displayName)
                },
                contentQueue = pendingMatches.map { it.toQueueItem() },
                reports = defaultReports(),
                outboundNotifications = (localOutbound + inbox.map { it.toOutboundRow() }).take(20),
                tickets = InMemoryAdminRepository.seed().tickets,
                feedbackSuggestion = InMemoryAdminRepository.seed().feedbackSuggestion,
                feedbackVotes = InMemoryAdminRepository.seed().feedbackVotes,
                adminAccounts = listOfNotNull(adminProfile?.toAdminAccountRow()),
                platformSettings = platformSettings,
                analyticsExportsTotal = analyticsExportsTotal,
                quickStatNewUsers7d = users.size,
                quickStatNewProjects7d = projects.count {
                    it.lifecycleStatus == ProjectLifecycle.RECRUITING
                },
                activityTrendUp = projects.isNotEmpty(),
                nextPushSlotLabel = nextPushSlotLabel,
            )
        }
    }

    private suspend fun requireAdmin() {
        if (!isCurrentUserAdmin()) {
            throw IllegalStateException("Admin role required (users/{uid}.accountRole = admin).")
        }
    }

    private suspend fun isCurrentUserAdmin(): Boolean {
        val uid = firebaseAuth.currentUser?.uid ?: return false
        val profile = userDataSource.fetchUserProfile(uid) ?: return false
        return profile.accountRole == AccountRole.ADMIN
    }

    private suspend fun resolveAudienceUserIds(audience: String): List<String> {
        val all = adminDataSource.fetchDirectoryUsers(limit = 50).map { it.id }
        val trimmed = audience.trim().lowercase()
        return when {
            trimmed.contains("all") -> all
            trimmed.contains("active") ->
                _snapshot.value.users
                    .filter { it.status == AdminUserStatus.ACTIVE }
                    .map { it.id }
            else -> all.take(10)
        }
    }

    private fun projectTitleOrBlank(projectId: String): String =
        _snapshot.value.projects.firstOrNull { it.id == projectId }?.name.orEmpty()

    private fun projectStackOrBlank(projectId: String): String =
        _snapshot.value.projects.firstOrNull { it.id == projectId }?.techSummary.orEmpty()

    private fun audit(message: String) {
        val entry = AuditLogEntry(
            id = UUID.randomUUID().toString(),
            timeLabel = nowLabel(),
            message = message,
        )
        _snapshot.update { state ->
            state.copy(auditLog = listOf(entry) + state.auditLog.take(49))
        }
    }

    private fun nowLabel(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    private fun UserProfileDoc.toAdminUserRow(): AdminUserRow = AdminUserRow(
        id = id,
        username = usernameLower.ifBlank { id.take(8) },
        email = email,
        techStack = skillTags.joinToString(" • "),
        bio = bio,
        status = when (accountRole) {
            AccountRole.BANNED -> AdminUserStatus.BANNED
            AccountRole.DEACTIVATED -> AdminUserStatus.DEACTIVATED
            AccountRole.ADMIN -> AdminUserStatus.ACTIVE
            else -> AdminUserStatus.ACTIVE
        },
        lastLoginLabel = formatRelativeTime(lastActiveAt ?: updatedAt ?: createdAt),
    )

    private fun ProjectDoc.toAdminProjectRow(ownerName: String?): AdminProjectRow = AdminProjectRow(
        id = id,
        name = title,
        creatorSummary = ownerName ?: ownerUserId.take(8),
        memberCount = memberCount.coerceAtLeast(1),
        techSummary = primaryStackLabel.ifBlank { stackTags.joinToString(" · ") },
        createdLabel = formatRelativeTime(createdAt),
        status = when (lifecycleStatus) {
            ProjectLifecycle.RECRUITING, ProjectLifecycle.DRAFT -> AdminProjectStatus.PENDING_APPROVAL
            ProjectLifecycle.ARCHIVED -> AdminProjectStatus.ARCHIVED
            else -> AdminProjectStatus.ACTIVE
        },
    )

    private fun MatchRequestDoc.toQueueItem(): ContentQueueItem = ContentQueueItem(
        id = id,
        title = message?.take(48)?.ifBlank { "Match request" } ?: "Collaboration request",
        author = fromUserId.take(8),
        held = false,
    )

    private fun com.example.developernetworkingapp.data.datasource.firebase.schema.InboxNotificationDoc.toOutboundRow() =
        OutboundNotificationRow(
            id = id,
            title = title,
            body = body,
            audience = recipientUserId.take(8),
            sentAtLabel = formatRelativeTime(createdAt),
        )

    private fun UserProfileDoc.toAdminAccountRow() = com.example.developernetworkingapp.domain.model.AdminAccountRow(
        id = id,
        displayName = displayName.ifBlank { "Administrator" },
        roleLabel = "Super admin",
        lastSeenLabel = formatRelativeTime(lastActiveAt),
        permissionPreset = AdminPermissionPreset.SUPER_ADMIN,
    )

    private fun defaultReports(): List<ContentReportRow> = listOf(
        ContentReportRow("r-local-1", "Flagged thread — awaiting moderator (demo)", active = true),
    )
}
