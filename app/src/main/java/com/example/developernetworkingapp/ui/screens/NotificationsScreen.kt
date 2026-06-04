package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.developernetworkingapp.di.appViewModel
import androidx.navigation.NavController
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.developernetworkingapp.domain.model.NotificationItem
import com.example.developernetworkingapp.ui.components.EmptyStateCard
import com.example.developernetworkingapp.ui.components.PremiumInfoCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.util.userFacingStatusMessage
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.NotificationsUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.NotificationsViewModel

@Composable
fun NotificationRoute(padding: PaddingValues, navController: NavController) {
    val viewModel: NotificationsViewModel = appViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    NotificationScreen(
        padding = padding,
        state = state,
        navController = navController,
        onMarkRead = viewModel::markAsRead
    )
}

@Composable
fun NotificationScreen(
    padding: PaddingValues,
    state: NotificationsUiState,
    navController: NavController,
    onMarkRead: (String) -> Unit
) {
    var selectedNotification by remember { mutableStateOf<NotificationItem?>(null) }

    selectedNotification?.let { alert ->
        AlertDialog(
            onDismissRequest = { selectedNotification = null },
            title = { Text("Notification") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(alert.body, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {
                            navController.navigate(AppRoutes.PROJECTS)
                        }, label = { Text("Project") })
                        AssistChip(
                            onClick = {
                                onMarkRead(alert.id)
                                selectedNotification = null
                            },
                            label = { Text(if (alert.read) "Already read" else "Mark read") },
                            enabled = !alert.read
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedNotification = null
                    navController.navigate(AppRoutes.PROJECTS)
                }) { Text("Open item") }
            },
            dismissButton = {
                TextButton(onClick = { selectedNotification = null }) { Text("Close") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("Recent Alerts")
                val unread = state.content?.unreadCount ?: 0
                if (unread > 0) {
                    Text(
                        "$unread unread",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        state.content?.statusMessage?.let { message ->
            item {
                PremiumInfoCard(
                    title = "Inbox",
                    subtitle = userFacingStatusMessage(message) ?: message,
                )
            }
        }
        val items = state.content?.items.orEmpty()
        if (items.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "You're all caught up",
                    subtitle = "New mentions, invites, and project updates will appear here.",
                )
            }
        }
        items(items, key = { it.id }) { item ->
            ElevatedCard(
                onClick = { selectedNotification = item },
                shape = AppDesignTokens.cardShape,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier
                        .alpha(if (item.read) 0.72f else 1f)
                        .padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(item.body, style = MaterialTheme.typography.titleMedium)
                    Text(
                        when {
                            item.read -> "Read"
                            item.relativeTime.isNotBlank() -> item.relativeTime
                            else -> "New"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.read) {
                            MaterialTheme.colorScheme.outline
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                navController.navigate(
                                    AppRoutes.detailRoute(
                                        title = "Notification",
                                        subtitle = "Recent update",
                                        description = item.body,
                                        sourceRoute = AppRoutes.NOTIFICATIONS
                                    )
                                )
                            },
                            label = { Text("View details") }
                        )
                        AssistChip(
                            onClick = { onMarkRead(item.id) },
                            label = { Text("Mark read") },
                            enabled = !item.read
                        )
                    }
                }
            }
        }
    }
}
