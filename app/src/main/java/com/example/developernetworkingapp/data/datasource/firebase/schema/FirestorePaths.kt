package com.example.developernetworkingapp.data.datasource.firebase.schema

/**
 * Top-level collection identifiers and path helpers.
 * Document IDs are plain strings; prefer Firebase Auth uid for [userId] paths.
 */
object FirestorePaths {
    const val USERS = "users"
    const val USERNAMES = "usernames"
    const val USER_STATS = "userStats"
    const val PROJECTS = "projects"
    const val MEMBERS = "members"
    const val TASKS = "tasks"
    const val CONVERSATIONS = "conversations"
    const val MESSAGES = "messages"
    const val MATCH_REQUESTS = "matchRequests"
    const val PROJECT_JOIN_REQUESTS = "projectJoinRequests"
    const val EVENTS = "events"
    const val REGISTRATIONS = "registrations"
    const val INBOX = "inbox"
    const val ACTIVITY = "activity"
    const val NEWS_HIGHLIGHTS = "newsHighlights"
    const val COLLABORATOR_SUGGESTIONS = "collaboratorSuggestions"
    const val SUPPORT_TICKETS = "supportTickets"
    const val CONTENT_REPORTS = "contentReports"
    const val PRODUCT_FEEDBACK = "productFeedback"
    const val PLATFORM_CONFIG = "platformConfig"

    fun user(userId: String) = "$USERS/$userId"

    fun usernameRegistryDoc(usernameLower: String) = "$USERNAMES/$usernameLower"

    fun userStats(userId: String) = "$USER_STATS/$userId"

    fun project(projectId: String) = "$PROJECTS/$projectId"

    fun projectMembers(projectId: String) = "${project(projectId)}/$MEMBERS"

    fun projectMember(projectId: String, memberUserId: String) =
        "${projectMembers(projectId)}/$memberUserId"

    fun projectTasks(projectId: String) = "${project(projectId)}/$TASKS"

    fun projectTask(projectId: String, taskId: String) =
        "${projectTasks(projectId)}/$taskId"

    fun conversation(conversationId: String) = "$CONVERSATIONS/$conversationId"

    fun conversationMessages(conversationId: String) =
        "${conversation(conversationId)}/$MESSAGES"

    fun conversationMessage(conversationId: String, messageId: String) =
        "${conversationMessages(conversationId)}/$messageId"

    fun matchRequest(requestId: String) = "$MATCH_REQUESTS/$requestId"

    fun event(eventId: String) = "$EVENTS/$eventId"

    fun eventRegistrations(eventId: String) = "${event(eventId)}/$REGISTRATIONS"

    fun eventRegistration(eventId: String, userId: String) =
        "${eventRegistrations(eventId)}/$userId"

    fun inboxNotification(notificationId: String) = "$INBOX/$notificationId"

    fun activityItem(activityId: String) = "$ACTIVITY/$activityId"

    fun newsHighlight(docId: String) = "$NEWS_HIGHLIGHTS/$docId"

    fun collaboratorSuggestion(docId: String) = "$COLLABORATOR_SUGGESTIONS/$docId"

    fun supportTicket(ticketId: String) = "$SUPPORT_TICKETS/$ticketId"

    fun contentReport(reportId: String) = "$CONTENT_REPORTS/$reportId"

    fun productFeedback(docId: String) = "$PRODUCT_FEEDBACK/$docId"

    fun platformConfig() = "$PLATFORM_CONFIG/default"
}
