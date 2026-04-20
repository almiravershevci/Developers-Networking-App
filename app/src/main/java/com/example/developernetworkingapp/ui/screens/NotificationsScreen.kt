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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.NotificationsUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.NotificationsViewModel

@Composable
fun NotificationRoute(padding: PaddingValues, navController: NavController) {
    val viewModel: NotificationsViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    NotificationScreen(padding, state, navController)
}

@Composable
fun NotificationScreen(padding: PaddingValues, state: NotificationsUiState, navController: NavController) {
    var selectedAlert by remember { mutableStateOf<String?>(null) }

    selectedAlert?.let { alert ->
        AlertDialog(
            onDismissRequest = { selectedAlert = null },
            title = { Text("Notification Detail") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(alert, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {
                            navController.navigate(AppRoutes.PROJECTS)
                        }, label = { Text("Project") })
                        AssistChip(onClick = {
                            navController.navigate(
                                AppRoutes.detailRoute(
                                    title = "Unread alert",
                                    subtitle = "Notification state",
                                    description = "Unread alerts require your attention. Open linked thread or mark read from the alerts feed.",
                                    sourceRoute = AppRoutes.NOTIFICATIONS
                                )
                            )
                        }, label = { Text("Unread") })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedAlert = null
                    navController.navigate(AppRoutes.PROJECTS)
                }) { Text("Open item") }
            },
            dismissButton = { TextButton(onClick = { selectedAlert = null }) { Text("Dismiss") } }
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
        item { SectionTitle("Recent Alerts") }
        items(state.content?.items ?: emptyList()) { text ->
            ElevatedCard(
                onClick = { selectedAlert = text },
                shape = AppDesignTokens.cardShape,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(AppDesignTokens.cardInnerPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text, style = MaterialTheme.typography.titleMedium)
                    Text("Just now", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = {
                                navController.navigate(
                                    AppRoutes.detailRoute(
                                        title = "Notification",
                                        subtitle = "Recent update",
                                        description = text,
                                        sourceRoute = AppRoutes.NOTIFICATIONS
                                    )
                                )
                            },
                            label = { Text("View details") }
                        )
                        AssistChip(
                            onClick = {
                                navController.navigate(
                                    AppRoutes.detailRoute(
                                        title = "Marked as read",
                                        subtitle = "Notification",
                                        description = "This alert has been marked as read and moved to your notification history.",
                                        sourceRoute = AppRoutes.NOTIFICATIONS
                                    )
                                )
                            },
                            label = { Text("Mark read") }
                        )
                    }
                }
            }
        }
    }
}
