package com.example.developernetworkingapp.ui.viewmodel

import com.example.developernetworkingapp.domain.model.ProjectBoardContent

/** Applies optional deep-link project label to Firestore board content (no mock task lists). */
object ProjectsBoardMapper {
    fun resolve(selectedProjectName: String, fallback: ProjectBoardContent?): ProjectBoardContent? {
        if (fallback == null) return null
        val label = selectedProjectName.trim()
        if (label.isBlank() || label.equals(fallback.teamName, ignoreCase = true)) {
            return fallback
        }
        return fallback.copy(
            teamName = label,
            teamMeta = fallback.teamMeta.ifBlank { "Loaded from Firestore" },
        )
    }
}
