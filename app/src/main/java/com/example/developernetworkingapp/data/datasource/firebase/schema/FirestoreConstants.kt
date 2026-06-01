package com.example.developernetworkingapp.data.datasource.firebase.schema

/**
 * Canonical string values stored in Firestore (no Kotlin enums).
 * Treat unknown future values as opaque strings on the client.
 */
object AccountRole {
    const val USER = "user"
    const val ADMIN = "admin"
    const val BANNED = "banned"
    const val DEACTIVATED = "deactivated"
}

object ProfileVisibility {
    const val PUBLIC = "public"
    const val NETWORK_ONLY = "network_only"
    const val PRIVATE = "private"
}

object ProjectLifecycle {
    const val DRAFT = "draft"
    const val RECRUITING = "recruiting"
    const val ACTIVE = "active"
    const val ARCHIVED = "archived"
}

object ProjectVisibility {
    const val PUBLIC = "public"
    const val UNLISTED = "unlisted"
    const val PRIVATE = "private"
}

object LocationKind {
    const val REMOTE = "remote"
    const val HYBRID = "hybrid"
    const val ONSITE = "onsite"
}

object MemberRole {
    const val OWNER = "owner"
    const val MAINTAINER = "maintainer"
    const val CONTRIBUTOR = "contributor"
    const val VIEWER = "viewer"
}

object TaskBoardColumn {
    const val TODO = "todo"
    const val IN_PROGRESS = "in_progress"
    const val DONE = "done"
    const val BLOCKED = "blocked"
}

object TaskPriority {
    const val LOW = "low"
    const val MEDIUM = "medium"
    const val HIGH = "high"
    const val URGENT = "urgent"
}

object MatchWorkflow {
    const val PENDING = "pending"
    const val ACCEPTED = "accepted"
    const val DECLINED = "declined"
    const val CANCELLED = "cancelled"
}

object ConversationKind {
    const val DIRECT = "direct"
    const val GROUP = "group"
    const val PROJECT_THREAD = "project_thread"
}

object MessageKind {
    const val TEXT = "text"
    const val SYSTEM = "system"
    const val MENTION = "mention"
}

object EventFormat {
    const val ONLINE = "online"
    const val IN_PERSON = "in_person"
    const val HYBRID = "hybrid"
}

object EventStatus {
    const val SCHEDULED = "scheduled"
    const val LIVE = "live"
    const val ENDED = "ended"
    const val CANCELLED = "cancelled"
}

object NotificationKind {
    const val TASK_UPDATE = "task_update"
    const val MESSAGE = "message"
    const val MATCH = "match"
    const val PROJECT_INVITE = "project_invite"
    const val EVENT = "event"
    const val FEED = "feed"
}

object ActivityVerb {
    const val COMMENTED = "commented"
    const val STATUS_CHANGED = "status_changed"
    const val INVITED = "invited"
    const val JOINED = "joined"
}

object ProjectIntent {
    const val PRODUCT = "product"
    const val RECRUITMENT = "recruitment"
}
