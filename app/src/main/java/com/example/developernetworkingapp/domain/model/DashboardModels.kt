package com.example.developernetworkingapp.domain.model

data class DashboardStat(
    val label: String,
    val value: String,
    val trend: String
)

data class FeatureModule(
    val title: String,
    val subtitle: String
)

data class CollaboratorMatch(
    val name: String,
    val stack: String,
    val matchScore: Int
)

data class ProjectHighlight(
    val title: String,
    val description: String,
    val progress: Int
)

data class ProjectPost(
    val title: String,
    val stack: String,
    val description: String,
    val owner: String,
    val openRoles: List<String>,
    val spotsLeft: Int
)

data class EventHighlight(
    val title: String,
    val meta: String
)

data class NewsHighlight(
    val title: String,
    val source: String
)

data class ActivityItem(
    val title: String,
    val time: String
)

data class DashboardContent(
    val greeting: String,
    val heroTitle: String,
    val heroSubtitle: String,
    val stats: List<DashboardStat>,
    val modules: List<FeatureModule>,
    val matches: List<CollaboratorMatch>,
    val projects: List<ProjectHighlight>,
    val projectPosts: List<ProjectPost>,
    val events: List<EventHighlight>,
    val news: List<NewsHighlight>,
    val activity: List<ActivityItem>
)
