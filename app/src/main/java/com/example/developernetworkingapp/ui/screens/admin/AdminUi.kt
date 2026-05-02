package com.example.developernetworkingapp.ui.screens.admin

import com.example.developernetworkingapp.domain.model.AdminProjectStatus
import com.example.developernetworkingapp.domain.model.AdminUserStatus
import com.example.developernetworkingapp.domain.model.TicketStatus
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.developernetworkingapp.ui.theme.AppDesignTokens

@Composable
fun AdminMetricTile(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun AdminSectionIntro(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AdminPlaceholderCard(title: String, description: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminActivityRow(time: String, action: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(action, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun AdminStatusChip(label: String) {
    AssistChip(onClick = {}, label = { Text(label) })
}

fun AdminUserStatus.toDisplayString(): String = when (this) {
    AdminUserStatus.ACTIVE -> "Active"
    AdminUserStatus.DEACTIVATED -> "Deactivated"
    AdminUserStatus.BANNED -> "Banned"
}

fun AdminProjectStatus.toDisplayString(): String = when (this) {
    AdminProjectStatus.ACTIVE -> "Active"
    AdminProjectStatus.PENDING_APPROVAL -> "Pending approval"
    AdminProjectStatus.REJECTED -> "Rejected"
    AdminProjectStatus.ARCHIVED -> "Archived"
}

fun TicketStatus.toDisplayString(): String = when (this) {
    TicketStatus.OPEN -> "Open"
    TicketStatus.ASSIGNED -> "Assigned"
    TicketStatus.CLOSED -> "Closed"
}
