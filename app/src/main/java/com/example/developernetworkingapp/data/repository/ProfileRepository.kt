package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.ProfileContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ProfileRepository {
    fun observeProfile(): Flow<ProfileContent>
}

class FakeProfileRepository : ProfileRepository {
    override fun observeProfile(): Flow<ProfileContent> = flowOf(
        ProfileContent(
            name = "Alex Dev",
            role = "Mobile + Cloud Engineer",
            bio = "Building scalable collaboration tools and real-time mobile experiences.",
            stacks = listOf("Android", "Kotlin", "Compose", "Firebase", "REST APIs", "CI/CD"),
            portfolio = "github.com/alex-dev - linkedin.com/in/alex-dev - alex.dev/portfolio",
            insights = "134 commits this month - 11 PRs merged - 7 repositories active"
        )
    )
}
