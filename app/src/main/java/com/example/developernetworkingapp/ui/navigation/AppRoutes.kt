package com.example.developernetworkingapp.ui.navigation

import android.net.Uri

object AppRoutes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val VERIFY_EMAIL = "verify/{email}"
    const val DASHBOARD = "dashboard"
    const val PROJECTS = "projects"
    const val CHAT = "chat"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val TASKS = "tasks"
    const val EVENTS = "events"

    const val DETAIL = "detail/{title}/{subtitle}/{description}/{sourceRoute}"
    const val COLLABORATOR_PROFILE = "collaborator/{name}/{stack}/{score}"

    fun detailRoute(
        title: String,
        subtitle: String,
        description: String,
        sourceRoute: String
    ): String {
        return "detail/${Uri.encode(title)}/${Uri.encode(subtitle)}/${Uri.encode(description)}/${Uri.encode(sourceRoute)}"
    }

    fun collaboratorProfileRoute(name: String, stack: String, score: Int): String {
        return "collaborator/${Uri.encode(name)}/${Uri.encode(stack)}/$score"
    }

    fun verifyEmailRoute(email: String): String {
        return "verify/${Uri.encode(email)}"
    }
}
