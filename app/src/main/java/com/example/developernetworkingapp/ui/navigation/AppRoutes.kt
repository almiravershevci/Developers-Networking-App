package com.example.developernetworkingapp.ui.navigation

import android.net.Uri

object AppRoutes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val VERIFY_EMAIL = "verify/{email}"
    const val DASHBOARD = "dashboard"
    const val PROJECTS = "projects"
    const val PROJECTS_WITH_PROJECT = "projects?project={project}"
    const val CHAT = "chat"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val TASKS = "tasks"
    const val EVENTS = "events"

    const val DETAIL = "detail/{title}/{subtitle}/{description}/{sourceRoute}"

    fun detailRoute(
        title: String,
        subtitle: String,
        description: String,
        sourceRoute: String
    ): String {
        return "detail/${Uri.encode(title)}/${Uri.encode(subtitle)}/${Uri.encode(description)}/${Uri.encode(sourceRoute)}"
    }

    fun projectsRoute(project: String? = null): String {
        return if (project.isNullOrBlank()) {
            PROJECTS
        } else {
            "projects?project=${Uri.encode(project)}"
        }
    }

    fun verifyEmailRoute(email: String): String {
        return "verify/${Uri.encode(email)}"
    }
}
