package com.example.developernetworkingapp.domain.model

data class ProfileContent(
    val name: String,
    val role: String,
    val bio: String,
    val stacks: List<String>,
    val portfolio: String,
    val insights: String,
    val statsLine: String = "",
)
