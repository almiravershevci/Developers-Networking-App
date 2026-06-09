package com.example.developernetworkingapp.domain.model

data class CollaboratorProfile(
    val id: String,
    val name: String,
    val stack: String,
    val location: String,
    val availability: String,
    val summary: String,
    val email: String = "",
    val matchScore: Int = 0,
    val projects: List<Pair<String, String>> = emptyList(),
    val collaborationHistory: List<String> = emptyList(),
)
