package com.example.developernetworkingapp.ui.state

import com.example.developernetworkingapp.domain.model.ConversationThread

data class ConversationUiState(
    val thread: ConversationThread? = null,
    val draft: String = "",
    val isSending: Boolean = false,
    val sendError: String? = null,
)
