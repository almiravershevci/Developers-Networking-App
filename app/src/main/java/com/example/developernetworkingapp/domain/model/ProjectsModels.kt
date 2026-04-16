package com.example.developernetworkingapp.domain.model

data class ProjectBoardContent(
    val teamName: String,
    val teamMeta: String,
    val todo: List<String>,
    val inProgress: List<String>,
    val done: List<String>
)
