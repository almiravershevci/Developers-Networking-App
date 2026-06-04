package com.example.developernetworkingapp.ui.viewmodel

import com.example.developernetworkingapp.domain.model.ProjectBoardContent

object ProjectsBoardMapper {
    fun resolve(selectedProjectName: String, fallback: ProjectBoardContent?): ProjectBoardContent? {
        val normalized = selectedProjectName.trim().lowercase()
        return when {
            normalized.isBlank() -> fallback
            "devconnect mobile" in normalized -> ProjectBoardContent(
                teamName = "DevConnect Mobile Workspace",
                teamMeta = "6 active members • Android + Backend Sync • Sprint 9",
                todo = listOf(
                    "Finalize chat thread unread indicators",
                    "Implement push permission onboarding copy",
                    "Design profile activity timeline card",
                ),
                inProgress = listOf(
                    "Realtime presence heartbeat reliability",
                    "In-app notification deep-link routing",
                    "Crash-free startup optimization pass",
                ),
                done = listOf(
                    "Authentication session persistence",
                    "Feed composer validation rules",
                    "Collaborator profile navigation flow",
                ),
            )
            "talent graph api" in normalized -> ProjectBoardContent(
                teamName = "Talent Graph API Workspace",
                teamMeta = "5 active members • Ranking Engine • Sprint 6",
                todo = listOf(
                    "Document scoring-weight tuning guidelines",
                    "Add contract tests for match explanation field",
                    "Create rate-limit policy for public endpoints",
                ),
                inProgress = listOf(
                    "Endpoint latency optimization under load",
                    "Redis cache invalidation strategy",
                    "Integration tests for ranking consistency",
                ),
                done = listOf(
                    "Initial match scoring pipeline",
                    "Skill normalization dictionary",
                    "Base endpoint authentication middleware",
                ),
            )
            else -> fallback?.copy(
                teamName = "${selectedProjectName.ifBlank { "Project" }} Workspace",
                teamMeta = "Project board • Planning + Delivery • Active sprint",
            )
        }
    }
}
