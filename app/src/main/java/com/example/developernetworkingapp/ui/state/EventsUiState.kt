package com.example.developernetworkingapp.ui.state

import com.example.developernetworkingapp.domain.model.EventContent

data class EventsUiState(
    val content: EventContent? = null,
    val registrationInFlight: String? = null,
    val actionError: String? = null,
)
