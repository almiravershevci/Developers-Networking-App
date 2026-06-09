package com.example.developernetworkingapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.developernetworkingapp.domain.model.ProjectJoinRequest
import com.example.developernetworkingapp.ui.theme.AppDesignTokens

@Composable
fun ProjectJoinRequestCard(
    request: ProjectJoinRequest,
    isResolving: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppDesignTokens.cardShape,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${request.fromDisplayName} wants to join ${request.projectTitle}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Role: ${request.requestedRole}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            request.message?.takeIf { it.isNotBlank() }?.let { message ->
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = request.relativeTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAccept, enabled = !isResolving) {
                    Text(if (isResolving) "Working…" else "Accept")
                }
                TextButton(onClick = onDecline, enabled = !isResolving) {
                    Text("Decline")
                }
            }
        }
    }
}
