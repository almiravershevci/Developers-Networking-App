package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.SearchContent
import com.example.developernetworkingapp.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface SearchRepository {
    fun observeSearch(): Flow<SearchContent>
}

class FakeSearchRepository : SearchRepository {
    override fun observeSearch(): Flow<SearchContent> = flowOf(
        SearchContent(
            filters = listOf("Kotlin", "Remote", "Backend", "Cloud", "Hackathon-ready", "Open Source"),
            results = listOf(
                SearchResult(
                    projectId = "proj_devconnect_mobile",
                    title = "DevConnect Mobile",
                    subtitle = "Realtime collaboration app for developers",
                    stack = "Kotlin + Firebase + Compose",
                    owner = "Alex Dev",
                    location = "Remote",
                    rolesNeeded = listOf("Backend APIs", "Mobile UI", "Notifications"),
                    membersCount = 4,
                    description = "A social collaboration platform where developers can discover each other, post active projects, join stacks, and coordinate work with messaging and task boards.",
                ),
                SearchResult(
                    projectId = "proj_cloudforge",
                    title = "CloudForge",
                    subtitle = "DevOps-focused startup toolkit",
                    stack = "Cloud + DevOps + Kotlin",
                    owner = "Ravi",
                    location = "Remote",
                    rolesNeeded = listOf("DevOps", "Platform Engineering"),
                    membersCount = 6,
                    description = "Building a platform for startup teams to manage deployments, infra templates, and release workflows with realtime operational alerts.",
                ),
                SearchResult(
                    projectId = "proj_pixelpair",
                    title = "PixelPair",
                    subtitle = "Design system and community showcase app",
                    stack = "Android + Design Systems",
                    owner = "Lina",
                    location = "Amman",
                    rolesNeeded = listOf("Compose UI", "Motion Design", "Brand Systems"),
                    membersCount = 3,
                    description = "A beautiful social showcase for app builders to publish interface work, design systems, and creative coding experiments.",
                ),
            ),
        ),
    )
}
