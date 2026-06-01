package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.EventContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface EventsRepository {
    fun observeEvents(): Flow<EventContent>
}

class FakeEventsRepository : EventsRepository {
    override fun observeEvents(): Flow<EventContent> = flowOf(
        EventContent(
            items = listOf(
                "AI Builders Jam - Starts in 9h - 142 participants",
                "Open Source Weekend - Starts in 2 days - 89 participants",
                "Mobile Hack Night - Live now - 17 teams active",
            ),
            eventIds = listOf("event_ai_builders_jam", "event_open_source_weekend", "event_mobile_hack_night"),
        ),
    )
}
