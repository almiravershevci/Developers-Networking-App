package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.ActivityItem
import com.example.developernetworkingapp.domain.model.CollaboratorMatch
import com.example.developernetworkingapp.domain.model.DashboardContent
import com.example.developernetworkingapp.domain.model.DashboardStat
import com.example.developernetworkingapp.domain.model.EventHighlight
import com.example.developernetworkingapp.domain.model.FeatureModule
import com.example.developernetworkingapp.domain.model.NewsHighlight
import com.example.developernetworkingapp.domain.model.ProjectHighlight
import com.example.developernetworkingapp.domain.model.ProjectPost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeDashboardRepository : DashboardRepository {
    override fun observeDashboardContent(): Flow<DashboardContent> {
        return flowOf(
            DashboardContent(
                greeting = "Good evening, Alex",
                heroTitle = "Your network is on fire this week",
                heroSubtitle = "12 new collaborators, 4 live events, and 3 project milestones due soon.",
                stats = listOf(
                    DashboardStat("Active Projects", "8", "+2 this week"),
                    DashboardStat("Open Tasks", "24", "6 high priority"),
                    DashboardStat("Unread Messages", "12", "3 mentions"),
                    DashboardStat("Match Requests", "17", "+5 today")
                ),
                modules = listOf(
                    FeatureModule("Team Matching", "AI-suggested devs by stack and availability"),
                    FeatureModule("Smart Tasks", "Auto-prioritized sprint board"),
                    FeatureModule("Live Events", "Hackathon feed and scoreboards")
                ),
                matches = listOf(
                    CollaboratorMatch("Mina", "Android + Firebase", 95),
                    CollaboratorMatch("Khaled", "Backend + AI APIs", 93),
                    CollaboratorMatch("Nora", "UI/UX + React Native", 91)
                ),
                projects = listOf(
                    ProjectHighlight("DevConnect Mobile", "Realtime collaboration app", 76),
                    ProjectHighlight("Talent Graph API", "Skill matching backend services", 58)
                ),
                projectPosts = listOf(
                    ProjectPost(
                        title = "Looking for Android + Firebase partner",
                        stack = "Kotlin, Compose, Firebase",
                        description = "I'm building a developer collaboration app and need one backend-minded mobile developer to help with realtime features and notifications.",
                        owner = "Alex Dev",
                        openRoles = listOf("Firebase", "Backend APIs", "Compose UI"),
                        spotsLeft = 2
                    ),
                    ProjectPost(
                        title = "Open source AI pair-programming assistant",
                        stack = "Android, Python, LLM APIs",
                        description = "Small product team building an AI coding companion with project rooms, live suggestions, and GitHub sync.",
                        owner = "Mina Rahman",
                        openRoles = listOf("Android", "Prompt Design", "Python"),
                        spotsLeft = 3
                    )
                ),
                events = listOf(
                    EventHighlight("DevSprint Global", "Starts in 8h - 332 participants"),
                    EventHighlight("Open Source Weekend", "Starts in 2 days - 89 participants")
                ),
                news = listOf(
                    NewsHighlight("Kotlin 2.x performance upgrades shipping now", "Kotlin Blog"),
                    NewsHighlight("Jetpack Compose receives new animation APIs", "Android Developers")
                ),
                activity = listOf(
                    ActivityItem("Aria commented on API schema PR", "5m ago"),
                    ActivityItem("Task moved to In Progress in Team Neon", "14m ago"),
                    ActivityItem("New invitation to AI Builders Jam", "27m ago")
                )
            )
        )
    }
}
