package com.example.developernetworkingapp.domain.model

enum class AdminUserStatus {
    ACTIVE,
    DEACTIVATED,
    BANNED
}

data class AdminUserRow(
    val id: String,
    val username: String,
    val email: String,
    val techStack: String,
    val bio: String,
    val status: AdminUserStatus,
    val lastLoginLabel: String
)

enum class AdminProjectStatus {
    ACTIVE,
    PENDING_APPROVAL,
    REJECTED,
    ARCHIVED
}

data class AdminProjectRow(
    val id: String,
    val name: String,
    val creatorSummary: String,
    val memberCount: Int,
    val techSummary: String,
    val createdLabel: String,
    val status: AdminProjectStatus
)

data class ContentQueueItem(
    val id: String,
    val title: String,
    val author: String,
    val held: Boolean
)

data class ContentReportRow(
    val id: String,
    val summary: String,
    val active: Boolean
)

data class OutboundNotificationRow(
    val id: String,
    val title: String,
    val body: String,
    val audience: String,
    val sentAtLabel: String
)

enum class TicketStatus {
    OPEN,
    ASSIGNED,
    CLOSED
}

data class SupportTicketRow(
    val id: String,
    val title: String,
    val status: TicketStatus
)

data class AdminAccountRow(
    val id: String,
    val displayName: String,
    val roleLabel: String,
    val lastSeenLabel: String,
    val permissionPreset: AdminPermissionPreset
)

enum class AdminPermissionPreset {
    SUPER_ADMIN,
    MODERATOR,
    ANALYST
}

data class AuditLogEntry(
    val id: String,
    val timeLabel: String,
    val message: String,
    val timestampMs: Long = System.currentTimeMillis()
)

data class PlatformSettingsSnapshot(
    val defaultNotificationsEnabled: Boolean,
    val strictTransportEncryption: Boolean,
    val analyticsSharingEnabled: Boolean,
    val themeDraftLabel: String
)

data class AdminDashboardSnapshot(
    val catalogSourceLabel: String = "Firestore",
    val users: List<AdminUserRow>,
    val projects: List<AdminProjectRow>,
    val contentQueue: List<ContentQueueItem>,
    val reports: List<ContentReportRow>,
    val outboundNotifications: List<OutboundNotificationRow>,
    val pushTopic: String,
    val nextPushSlotLabel: String,
    val tickets: List<SupportTicketRow>,
    val feedbackSuggestion: String,
    val feedbackVotes: Int,
    val adminAccounts: List<AdminAccountRow>,
    val auditLog: List<AuditLogEntry>,
    val platformSettings: PlatformSettingsSnapshot,
    val analyticsExportsTotal: Int,
    val quickStatNewUsers7d: Int,
    val quickStatNewProjects7d: Int,
    val activityTrendUp: Boolean,
) {
    companion object {
        fun empty(catalogSourceLabel: String = "Loading…") = AdminDashboardSnapshot(
            catalogSourceLabel = catalogSourceLabel,
            users = emptyList(),
            projects = emptyList(),
            contentQueue = emptyList(),
            reports = emptyList(),
            outboundNotifications = emptyList(),
            pushTopic = "product_updates",
            nextPushSlotLabel = "None scheduled",
            tickets = emptyList(),
            feedbackSuggestion = "—",
            feedbackVotes = 0,
            adminAccounts = emptyList(),
            auditLog = emptyList(),
            platformSettings = PlatformSettingsSnapshot(
                defaultNotificationsEnabled = true,
                strictTransportEncryption = true,
                analyticsSharingEnabled = false,
                themeDraftLabel = "Default theme",
            ),
            analyticsExportsTotal = 0,
            quickStatNewUsers7d = 0,
            quickStatNewProjects7d = 0,
            activityTrendUp = true,
        )
    }
}
