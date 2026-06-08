package com.example.developernetworkingapp.data.repository

import com.example.developernetworkingapp.domain.model.EventContent
import com.example.developernetworkingapp.domain.model.EventItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface EventsRepository {
    fun observeEvents(): Flow<EventContent>

    fun observeMyRegistrations(): Flow<List<String>>

    suspend fun registerForEvent(eventId: String): Result<Unit>

    suspend fun unregisterFromEvent(eventId: String): Result<Unit>
}

class FakeEventsRepository : EventsRepository {
    override fun observeEvents(): Flow<EventContent> = flowOf(
        EventContent(
            items = listOf(
                EventItem("event_ai_builders_jam", "AI Builders Jam - Starts in 9h - 142 participants"),
                EventItem("event_open_source_weekend", "Open Source Weekend - Starts in 2 days - 89 participants"),
                EventItem("event_mobile_hack_night", "Mobile Hack Night - Live now - 17 teams active", isRegistered = true),
            ),
        ),
    )

    override fun observeMyRegistrations(): Flow<List<String>> =
        flowOf(listOf("event_mobile_hack_night"))

    override suspend fun registerForEvent(eventId: String): Result<Unit> = Result.success(Unit)

    override suspend fun unregisterFromEvent(eventId: String): Result<Unit> = Result.success(Unit)
}
