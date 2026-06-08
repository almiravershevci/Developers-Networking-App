package com.example.developernetworkingapp.data.datasource.firebase.schema

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Firestore document shapes aligned with the Compose UI layer (profile, dashboard, projects,
 * search, chat, events, notifications). Use [Timestamp] for server-coordinated times.
 *
 * Field names match deployed indexes and [firestore.rules]. Prefer incrementing [schemaVersion]
 * when making additive migrations.
 */

// region users / identity

data class UserProfileDoc(
    @DocumentId val id: String = "",
    val schemaVersion: Int = 1,
    val displayName: String = "",
    val usernameLower: String = "",
    val email: String = "",
    val accountRole: String = "user",
    val emailVerified: Boolean = false,
    val headline: String = "",
    val bio: String = "",
    val photoUrl: String? = null,
    val skillTags: List<String> = emptyList(),
    val portfolioLinks: PortfolioLinksDoc? = null,
    val profileVisibility: String = ProfileVisibility.PUBLIC,
    val gitInsightsSummary: String? = null,
    /** Device tokens for FCM push — each teammate device appends its own token. */
    val fcmTokens: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    val lastActiveAt: Timestamp? = null,
)

data class PortfolioLinksDoc(
    val github: String? = null,
    val linkedin: String? = null,
    val portfolio: String? = null,
)

data class UsernameRegistryDoc(
    @DocumentId val usernameLower: String = "",
    val userId: String = "",
)

/** Denormalized counters and badges; written by privileged automation only (Cloud Functions / Admin). */
data class UserStatsDoc(
    @DocumentId val userId: String = "",
    val schemaVersion: Int = 1,
    val activeProjectsCount: Int = 0,
    val openTasksCount: Int = 0,
    val unreadMessagesCount: Int = 0,
    val pendingMatchRequestsCount: Int = 0,
    val collaborationsCount: Int = 0,
    val ratingAggregate: Double? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

// endregion

// region projects

data class ProjectDoc(
    @DocumentId val id: String = "",
    val schemaVersion: Int = 1,
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val primaryStackLabel: String = "",
    val stackTags: List<String> = emptyList(),
    val ownerUserId: String = "",
    val locationKind: String = LocationKind.REMOTE,
    val cityName: String? = null,
    val openRoleLabels: List<String> = emptyList(),
    val capacityTotal: Int = 0,
    val spotsOpen: Int = 0,
    val memberCount: Int = 0,
    val progressPercent: Int? = null,
    val lifecycleStatus: String = ProjectLifecycle.RECRUITING,
    val visibility: String = ProjectVisibility.PUBLIC,
    /** Optional: [ProjectIntent] strings — distinguishes shipped products vs recruitment listings. */
    val projectIntent: String? = null,
    val searchKeywords: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

data class ProjectMemberDoc(
    @DocumentId val memberUserId: String = "",
    val memberRole: String = MemberRole.CONTRIBUTOR,
    @ServerTimestamp val joinedAt: Timestamp? = null,
)

data class ProjectTaskDoc(
    @DocumentId val id: String = "",
    val schemaVersion: Int = 1,
    val title: String = "",
    val boardColumn: String = TaskBoardColumn.TODO,
    val priority: String = TaskPriority.MEDIUM,
    val assigneeUserId: String? = null,
    val createdByUserId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

// endregion

// region collaboration

data class MatchRequestDoc(
    @DocumentId val id: String = "",
    val schemaVersion: Int = 1,
    val fromUserId: String = "",
    val toUserId: String = "",
    val workflowStatus: String = MatchWorkflow.PENDING,
    val message: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    val resolvedAt: Timestamp? = null,
)

// endregion

// region messaging

data class ConversationDoc(
    @DocumentId val id: String = "",
    val schemaVersion: Int = 1,
    val conversationKind: String = ConversationKind.DIRECT,
    val title: String? = null,
    val projectId: String? = null,
    val participantIds: List<String> = emptyList(),
    val createdBy: String = "",
    val lastMessagePreview: String? = null,
    val lastMessageAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

data class MessageDoc(
    @DocumentId val id: String = "",
    val schemaVersion: Int = 1,
    val senderId: String = "",
    val body: String = "",
    val messageKind: String = MessageKind.TEXT,
    val readByUserIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
)

// endregion

// region events (typically curated / Admin SDK)

data class EventDoc(
    @DocumentId val id: String = "",
    val schemaVersion: Int = 1,
    val title: String = "",
    val summaryLine: String = "",
    val startsAt: Timestamp? = null,
    val timezone: String = "UTC",
    val participantCount: Int = 0,
    val formatKind: String = EventFormat.ONLINE,
    val eventStatus: String = EventStatus.SCHEDULED,
)

data class EventRegistrationDoc(
    @DocumentId val id: String = "",
    val schemaVersion: Int = 1,
    val userId: String = "",
    val status: String = EventRegistrationStatus.GOING,
    @ServerTimestamp val registeredAt: Timestamp? = null,
)

// endregion

// region inbox & activity feed

data class InboxNotificationDoc(
    @DocumentId val id: String = "",
    val schemaVersion: Int = 1,
    val recipientUserId: String = "",
    val notificationKind: String = NotificationKind.FEED,
    val title: String = "",
    val body: String = "",
    val deepLink: String? = null,
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

data class ActivityItemDoc(
    @DocumentId val id: String = "",
    val schemaVersion: Int = 1,
    val audienceUserId: String = "",
    val verb: String = ActivityVerb.STATUS_CHANGED,
    val summary: String = "",
    val relatedProjectId: String? = null,
    val relatedConversationId: String? = null,
    val relatedEventId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

data class NewsHighlightDoc(
    @DocumentId val id: String = "",
    val schemaVersion: Int = 1,
    val title: String = "",
    val sourceName: String = "",
    val sortOrder: Int = 0,
    val tagLine: String? = null,
    val externalUrl: String? = null,
    @ServerTimestamp val publishedAt: Timestamp? = null,
)

data class CollaboratorSuggestionDoc(
    @DocumentId val id: String = "",
    val schemaVersion: Int = 1,
    val viewerUserId: String = "",
    val suggestedUserId: String = "",
    val stackSummary: String = "",
    val matchScore: Int = 0,
    val rank: Int = 0,
    val availabilityNote: String? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

// endregion
