package com.example.developernetworkingapp.ui.components

import com.example.developernetworkingapp.domain.model.DashboardStat
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardAnalyticsChartTest {

    @Test
    fun analyticsBars_parseNumericValuesFromStats() {
        val stats = listOf(
            DashboardStat("Active Projects", "4", "trend"),
            DashboardStat("Open Tasks", "12", "trend"),
            DashboardStat("Unread Messages", "0", "trend"),
            DashboardStat("Match Requests", "2", "trend"),
        )

        val values = stats.map { stat ->
            stat.value.filter { it.isDigit() }.toIntOrNull() ?: 0
        }

        assertEquals(listOf(4, 12, 0, 2), values)
    }
}
