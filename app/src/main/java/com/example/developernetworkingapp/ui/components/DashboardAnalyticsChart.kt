package com.example.developernetworkingapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.developernetworkingapp.domain.model.DashboardStat
import com.example.developernetworkingapp.ui.theme.ElectricCyan
import com.example.developernetworkingapp.ui.theme.ElectricGreen
import com.example.developernetworkingapp.ui.theme.VibrantOrange

data class AnalyticsBar(
    val label: String,
    val value: Int,
    val color: Color,
)

object DashboardAnalyticsTestTags {
    const val CHART = "dashboard_analytics_chart"
}

@Composable
fun DashboardAnalyticsChart(
    stats: List<DashboardStat>,
    modifier: Modifier = Modifier,
) {
    val bars = stats.mapIndexed { index, stat ->
        AnalyticsBar(
            label = stat.label,
            value = stat.value.filter { it.isDigit() }.toIntOrNull() ?: 0,
            color = chartColors[index % chartColors.size],
        )
    }
    val maxValue = bars.maxOfOrNull { it.value }?.coerceAtLeast(1) ?: 1

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag(DashboardAnalyticsTestTags.CHART),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Workspace analytics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Live metrics from your projects, tasks, and collaboration activity.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(top = 16.dp),
            ) {
                val barWidth = size.width / (bars.size * 2f)
                val chartHeight = size.height * 0.72f
                val baseline = size.height * 0.9f

                bars.forEachIndexed { index, bar ->
                    val barHeight = (bar.value.toFloat() / maxValue) * chartHeight
                    val left = (index * 2 + 0.5f) * barWidth
                    drawRoundRect(
                        color = bar.color,
                        topLeft = Offset(left, baseline - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(12f, 12f),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                bars.forEach { bar ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            bar.value.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            bar.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private val chartColors = listOf(ElectricCyan, ElectricGreen, VibrantOrange, Color(0xFF8B5CF6))
