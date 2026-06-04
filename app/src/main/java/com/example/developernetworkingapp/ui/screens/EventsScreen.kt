package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.developernetworkingapp.di.appViewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.ui.components.EmptyStateCard
import com.example.developernetworkingapp.ui.components.NotificationBanner
import com.example.developernetworkingapp.ui.components.PremiumInfoCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.util.userFacingStatusMessage
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.EventsUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.EventsUiEvent
import com.example.developernetworkingapp.ui.viewmodel.EventsViewModel
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun EventFeedRoute(padding: PaddingValues, navController: NavController) {
    val viewModel: EventsViewModel = appViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    EventFeedScreen(
        padding = padding,
        state = state,
        navController = navController,
        events = viewModel.events,
        onJoinEvent = viewModel::notifyEventJoined
    )
}

@Composable
fun EventFeedScreen(
    padding: PaddingValues,
    state: EventsUiState,
    navController: NavController,
    events: SharedFlow<EventsUiEvent>,
    onJoinEvent: (String) -> Unit
) {
    var selectedEvent by remember { mutableStateOf<String?>(null) }
    var activeNotification by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is EventsUiEvent.ShowNotification -> activeNotification = event.message
            }
        }
    }

    activeNotification?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(AppDesignTokens.notificationAutoHideMs)
            activeNotification = null
        }
        NotificationBanner(message = message, onDismiss = { activeNotification = null })
    }

    selectedEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { selectedEvent = null },
            title = { Text("Event Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(event, style = MaterialTheme.typography.titleMedium)
                    Text("Team matching, challenges, leaderboard, and mentorship channels included.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {
                            navController.navigate(
                                AppRoutes.detailRoute(
                                    title = "$event Leaderboard",
                                    subtitle = "Rankings",
                                    description = "Live team rankings, score updates, and challenge completion metrics for this event.",
                                    sourceRoute = AppRoutes.EVENTS
                                )
                            )
                        }, label = { Text("Leaderboard") })
                        AssistChip(onClick = {
                            navController.navigate(
                                AppRoutes.detailRoute(
                                    title = "$event Challenges",
                                    subtitle = "Challenge tracks",
                                    description = "Challenge briefs, judging criteria, and submission windows for all active tracks.",
                                    sourceRoute = AppRoutes.EVENTS
                                )
                            )
                        }, label = { Text("Challenges") })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onJoinEvent(event)
                    selectedEvent = null
                    navController.navigate(AppRoutes.CHAT)
                }) { Text("Join event") }
            },
            dismissButton = { TextButton(onClick = { selectedEvent = null }) { Text("Close") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item { SectionTitle("Hackathons & Events") }
        state.content?.statusMessage?.let { message ->
            item {
                PremiumInfoCard(
                    title = "Events feed",
                    subtitle = userFacingStatusMessage(message) ?: message,
                )
            }
        }
        val events = state.content?.items.orEmpty()
        if (events.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No upcoming events",
                    subtitle = "Hackathons and community events will be listed here as they are published.",
                )
            }
        }
        items(events) { event ->
            ElevatedCard(
                onClick = { selectedEvent = event },
                shape = AppDesignTokens.cardShape,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(AppDesignTokens.cardInnerPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(event, style = MaterialTheme.typography.titleMedium)
                    Text("Team matching by skills, location, and availability is active.", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            navController.navigate(
                                AppRoutes.detailRoute(
                                    title = event,
                                    subtitle = "Event Overview",
                                    description = "Complete event brief:\n• Full timeline and submission windows\n• Challenge tracks with judging criteria\n• Mentor office hours and team matching notes\n• Participant activity and leaderboard trends\n• Recommended preparation checklist before joining\n\nUse this page as the single source of truth for planning and execution during the event.",
                                    sourceRoute = AppRoutes.EVENTS
                                )
                            )
                        }) { Text("View details") }
                        TextButton(onClick = { selectedEvent = event }) { Text("Join now") }
                    }
                }
            }
        }
    }
}
