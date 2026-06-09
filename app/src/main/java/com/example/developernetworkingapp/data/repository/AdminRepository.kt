package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.AdminAccountRow
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
import com.example.developernetworkingapp.domain.model.SupportTicketRow
import com.example.developernetworkingapp.domain.model.TicketStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

interface AdminRepository {
    val snapshot: StateFlow<AdminDashboardSnapshot>

    fun deactivateUser(userId: String)
    fun activateUser(userId: String)
    fun banUser(userId: String)
    fun updateUserProfile(userId: String, techStack: String, bio: String)

    fun approveProject(projectId: String)
    fun rejectProject(projectId: String)
    fun archiveProject(projectId: String)
    fun removeProjectFromFeed(projectId: String, reason: String)
    fun updateProject(projectId: String, name: String, techSummary: String)

    fun approveAllQueuedContent()
    fun holdQueuedContentForReview()
    fun removeReportedContent(reportId: String)
    fun dismissReport(reportId: String)

    fun sendNotification(title: String, body: String, audience: String)
    fun scheduleNextPushSlot()

    fun exportAnalyticsCsv(): String

    fun setDefaultNotifications(enabled: Boolean)
    fun setStrictEncryption(enabled: Boolean)
    fun setAnalyticsSharing(enabled: Boolean)
    fun rotateIntegrationKeys()
    fun saveThemeDraft()

    fun assignTicket(ticketId: String)
    fun closeTicket(ticketId: String)
    fun addHelpArticleStub()

    fun updateAdminPreset(adminId: String, preset: AdminPermissionPreset)

    fun bulkExportUsersCsv(): String
}

/**
 * In-memory admin store with realistic transitions and audit logging (swap for API later).
 */
class InMemoryAdminRepository : AdminRepository {

    private val _snapshot = MutableStateFlow(seed())
    override val snapshot: StateFlow<AdminDashboardSnapshot> = _snapshot.asStateFlow()

    private fun nowLabel(): String {
        val t = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        return t
    }

    private fun audit(message: String) {
        val entry = AuditLogEntry(
            id = UUID.randomUUID().toString(),
            timeLabel = nowLabel(),
            message = message
        )
        _snapshot.update { s ->
            s.copy(auditLog = listOf(entry) + s.auditLog.take(49))
        }
    }

    override fun deactivateUser(userId: String) {
        _snapshot.update { s ->
            s.copy(users = s.users.map { u ->
                if (u.id == userId && u.status == AdminUserStatus.ACTIVE) u.copy(status = AdminUserStatus.DEACTIVATED) else u
            })
        }
        audit("Deactivated user $userId")
    }

    override fun activateUser(userId: String) {
        _snapshot.update { s ->
            s.copy(users = s.users.map { u ->
                if (u.id == userId && u.status != AdminUserStatus.ACTIVE) u.copy(status = AdminUserStatus.ACTIVE) else u
            })
        }
        audit("Activated user $userId")
    }

    override fun banUser(userId: String) {
        _snapshot.update { s ->
            s.copy(users = s.users.map { u ->
                if (u.id == userId) u.copy(status = AdminUserStatus.BANNED) else u
            })
        }
        audit("Banned user $userId")
    }

    override fun updateUserProfile(userId: String, techStack: String, bio: String) {
        _snapshot.update { s ->
            s.copy(users = s.users.map { u ->
                if (u.id == userId) u.copy(techStack = techStack.trim(), bio = bio.trim()) else u
            })
        }
        audit("Updated profile fields for $userId")
    }

    override fun approveProject(projectId: String) {
        _snapshot.update { s ->
            s.copy(projects = s.projects.map { p ->
                if (p.id == projectId && p.status == AdminProjectStatus.PENDING_APPROVAL) {
                    p.copy(status = AdminProjectStatus.ACTIVE)
                } else p
            })
        }
        audit("Approved project $projectId")
    }

    override fun rejectProject(projectId: String) {
        _snapshot.update { s ->
            s.copy(projects = s.projects.map { p ->
                if (p.id == projectId && p.status == AdminProjectStatus.PENDING_APPROVAL) {
                    p.copy(status = AdminProjectStatus.REJECTED)
                } else p
            })
        }
        audit("Rejected project $projectId")
    }

    override fun archiveProject(projectId: String) {
        _snapshot.update { s ->
            s.copy(projects = s.projects.map { p ->
                if (p.id == projectId && p.status != AdminProjectStatus.ARCHIVED) {
                    p.copy(status = AdminProjectStatus.ARCHIVED)
                } else p
            })
        }
        audit("Archived project $projectId")
    }

    override fun removeProjectFromFeed(projectId: String, reason: String) {
        _snapshot.update { s ->
            s.copy(projects = s.projects.filterNot { it.id == projectId })
        }
        audit("Removed project $projectId from feed: $reason")
    }

    override fun updateProject(projectId: String, name: String, techSummary: String) {
        _snapshot.update { s ->
            s.copy(projects = s.projects.map { p ->
                if (p.id == projectId) p.copy(name = name.trim(), techSummary = techSummary.trim()) else p
            })
        }
        audit("Edited project metadata $projectId")
    }

    override fun approveAllQueuedContent() {
        val n = _snapshot.value.contentQueue.size
        _snapshot.update { it.copy(contentQueue = emptyList()) }
        audit("Approved all visible queued posts ($n cleared)")
    }

    override fun holdQueuedContentForReview() {
        _snapshot.update { s ->
            s.copy(contentQueue = s.contentQueue.map { it.copy(held = true) })
        }
        audit("Held queued content for extended review")
    }

    override fun removeReportedContent(reportId: String) {
        _snapshot.update { s ->
            s.copy(reports = s.reports.filterNot { it.id == reportId })
        }
        audit("Removed content for report $reportId")
    }

    override fun dismissReport(reportId: String) {
        _snapshot.update { s ->
            s.copy(reports = s.reports.map { r -> if (r.id == reportId) r.copy(active = false) else r })
        }
        audit("Dismissed report $reportId")
    }

    override fun sendNotification(title: String, body: String, audience: String) {
        val row = OutboundNotificationRow(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            body = body.trim(),
            audience = audience.ifBlank { "All users" },
            sentAtLabel = "Just now"
        )
        _snapshot.update { s ->
            s.copy(outboundNotifications = listOf(row) + s.outboundNotifications.take(19))
        }
        audit("Sent notification: ${row.title}")
    }

    override fun scheduleNextPushSlot() {
        _snapshot.update { s ->
            s.copy(nextPushSlotLabel = "Tomorrow 09:00 (local)")
        }
        audit("Scheduled push slot on topic ${_snapshot.value.pushTopic}")
    }

    override fun exportAnalyticsCsv(): String {
        val s = _snapshot.value
        val csv = buildString {
            appendLine("metric,value")
            appendLine("total_users,${s.users.size}")
            appendLine("active_projects,${s.projects.count { it.status == AdminProjectStatus.ACTIVE }}")
            appendLine("pending_approvals,${s.projects.count { it.status == AdminProjectStatus.PENDING_APPROVAL }}")
            appendLine("exports_total,${s.analyticsExportsTotal + 1}")
        }
        _snapshot.update { it.copy(analyticsExportsTotal = it.analyticsExportsTotal + 1) }
        audit("Exported analytics CSV (batch ${csv.lines().size - 1} metrics)")
        return csv
    }

    override fun setDefaultNotifications(enabled: Boolean) {
        _snapshot.update { s ->
            s.copy(platformSettings = s.platformSettings.copy(defaultNotificationsEnabled = enabled))
        }
        audit("Platform default notifications → $enabled")
    }

    override fun setStrictEncryption(enabled: Boolean) {
        _snapshot.update { s ->
            s.copy(platformSettings = s.platformSettings.copy(strictTransportEncryption = enabled))
        }
        audit("Strict TLS enforcement → $enabled")
    }

    override fun setAnalyticsSharing(enabled: Boolean) {
        _snapshot.update { s ->
            s.copy(platformSettings = s.platformSettings.copy(analyticsSharingEnabled = enabled))
        }
        audit("Product analytics sharing → $enabled")
    }

    override fun rotateIntegrationKeys() {
        audit("Rotated integration API keys (new secrets issued server-side)")
    }

    override fun saveThemeDraft() {
        _snapshot.update { s ->
            s.copy(
                platformSettings = s.platformSettings.copy(
                    themeDraftLabel = "Draft saved ${nowLabel()}"
                )
            )
        }
        audit("Saved branding / theme draft locally")
    }

    override fun assignTicket(ticketId: String) {
        _snapshot.update { s ->
            s.copy(
                tickets = s.tickets.map { t ->
                    if (t.id == ticketId) t.copy(status = TicketStatus.ASSIGNED) else t
                }
            )
        }
        audit("Assigned ticket $ticketId")
    }

    override fun closeTicket(ticketId: String) {
        _snapshot.update { s ->
            s.copy(
                tickets = s.tickets.map { t ->
                    if (t.id == ticketId) t.copy(status = TicketStatus.CLOSED) else t
                }
            )
        }
        audit("Closed ticket $ticketId")
    }

    override fun addHelpArticleStub() {
        audit("Created help article draft #${UUID.randomUUID().toString().take(8)}")
    }

    override fun updateAdminPreset(adminId: String, preset: AdminPermissionPreset) {
        _snapshot.update { s ->
            s.copy(
                adminAccounts = s.adminAccounts.map { a ->
                    if (a.id == adminId) a.copy(
                        permissionPreset = preset,
                        roleLabel = when (preset) {
                            AdminPermissionPreset.SUPER_ADMIN -> "Super admin"
                            AdminPermissionPreset.MODERATOR -> "Moderator"
                            AdminPermissionPreset.ANALYST -> "Analyst"
                        }
                    ) else a
                }
            )
        }
        audit("Updated RBAC preset for admin $adminId → $preset")
    }

    override fun bulkExportUsersCsv(): String {
        val users = _snapshot.value.users
        val rows = users.joinToString("\n") { "${it.id},${it.username},${it.email},${it.status}" }
        val csv = "id,username,email,status\n$rows"
        audit("Bulk exported ${users.size} users to CSV")
        return csv
    }

    companion object {
        internal fun seed(): AdminDashboardSnapshot {
            val users = listOf(
                AdminUserRow(
                    id = "u1",
                    username = "alex.dev",
                    email = "alex@example.com",
                    techStack = "Kotlin • Android • Firebase",
                    bio = "Mobile engineer",
                    status = AdminUserStatus.ACTIVE,
                    lastLoginLabel = "2h ago"
                ),
                AdminUserRow(
                    id = "u2",
                    username = "sara.codes",
                    email = "sara@example.com",
                    techStack = "React • Node • Postgres",
                    bio = "Full-stack",
                    status = AdminUserStatus.ACTIVE,
                    lastLoginLabel = "30m ago"
                ),
                AdminUserRow(
                    id = "u3",
                    username = "inactive_one",
                    email = "gone@example.com",
                    techStack = "Python",
                    bio = "—",
                    status = AdminUserStatus.DEACTIVATED,
                    lastLoginLabel = "90d ago"
                )
            )
            val projects = listOf(
                AdminProjectRow(
                    id = "p1",
                    name = "Realtime Chat UI",
                    creatorSummary = "Aria Chen",
                    memberCount = 5,
                    techSummary = "Kotlin / Compose",
                    createdLabel = "Jan 12, 2026",
                    status = AdminProjectStatus.ACTIVE
                ),
                AdminProjectRow(
                    id = "p2",
                    name = "Talent Graph API",
                    creatorSummary = "Team Neon",
                    memberCount = 8,
                    techSummary = "Kotlin / Spring",
                    createdLabel = "Jan 10, 2026",
                    status = AdminProjectStatus.PENDING_APPROVAL
                ),
                AdminProjectRow(
                    id = "p3",
                    name = "Hackathon Portal",
                    creatorSummary = "Org",
                    memberCount = 12,
                    techSummary = "Next.js",
                    createdLabel = "Dec 01, 2025",
                    status = AdminProjectStatus.ARCHIVED
                )
            )
            val queue = listOf(
                ContentQueueItem("c1", "Portfolio showcase", "Maria", held = false),
                ContentQueueItem("c2", "Meetup recap", "Jon", held = false),
                ContentQueueItem("c3", "Job listing", "StartupXYZ", held = false)
            )
            val reports = listOf(
                ContentReportRow("r441", "Harassment — comment thread #882", active = true)
            )
            val notifications = listOf(
                OutboundNotificationRow(
                    id = "n1",
                    title = "Weekly digest",
                    body = "Your network highlights",
                    audience = "All active users",
                    sentAtLabel = "Yesterday"
                )
            )
            val tickets = listOf(
                SupportTicketRow("t901", "Login loop on Pixel 8", TicketStatus.OPEN),
                SupportTicketRow("t899", "Billing receipt", TicketStatus.ASSIGNED)
            )
            val admins = listOf(
                AdminAccountRow(
                    id = "a1",
                    displayName = "Primary administrator",
                    roleLabel = "Super admin",
                    lastSeenLabel = "2h ago",
                    permissionPreset = AdminPermissionPreset.SUPER_ADMIN
                ),
                AdminAccountRow(
                    id = "a2",
                    displayName = "moderator.bot",
                    roleLabel = "Moderator",
                    lastSeenLabel = "1d ago",
                    permissionPreset = AdminPermissionPreset.MODERATOR
                )
            )
            val audit = listOf(
                AuditLogEntry("e1", "09:42", "Previous session: exported user CSV"),
                AuditLogEntry("e2", "Yesterday", "moderator.bot banned user spam_bot_21")
            )
            return AdminDashboardSnapshot(
                catalogSourceLabel = "In-memory demo",
                users = users,
                projects = projects,
                contentQueue = queue,
                reports = reports,
                outboundNotifications = notifications,
                pushTopic = "product_updates",
                nextPushSlotLabel = "None scheduled",
                tickets = tickets,
                feedbackSuggestion = "Dark mode for admin dashboard",
                feedbackVotes = 42,
                adminAccounts = admins,
                auditLog = audit,
                platformSettings = PlatformSettingsSnapshot(
                    defaultNotificationsEnabled = true,
                    strictTransportEncryption = false,
                    analyticsSharingEnabled = false,
                    themeDraftLabel = "Default theme"
                ),
                analyticsExportsTotal = 0,
                quickStatNewUsers7d = 124,
                quickStatNewProjects7d = 38,
                activityTrendUp = true
            )
        }
    }
}
