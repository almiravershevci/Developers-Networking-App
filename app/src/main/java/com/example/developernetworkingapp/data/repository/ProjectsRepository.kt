package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.ProjectBoardContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ProjectsRepository {
    fun observeProjects(): Flow<ProjectBoardContent>
}

class FakeProjectsRepository : ProjectsRepository {
    override fun observeProjects(): Flow<ProjectBoardContent> = flowOf(
        ProjectBoardContent(
            teamName = "Team Neon",
            teamMeta = "7 members - Kotlin, Firebase, Compose",
            todo = listOf("Onboarding flow", "Integrate search filters", "Team invite modal"),
            inProgress = listOf("Realtime chat room", "Task assignment module", "Home analytics cards"),
            done = listOf("Auth draft screens", "Profile summary card")
        )
    )
}
