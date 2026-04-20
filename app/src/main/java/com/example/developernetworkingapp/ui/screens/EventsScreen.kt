package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.EventsUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.EventsViewModel

@Composable
fun EventFeedRoute(padding: PaddingValues, navController: NavController) {
    val viewModel: EventsViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    EventFeedScreen(padding, state, navController)
}

@Composable
fun EventFeedScreen(padding: PaddingValues, state: EventsUiState, navController: NavController) {
    var selectedEvent by remember { mutableStateOf<String?>(null) }

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
        items(state.content?.items ?: emptyList()) { event ->
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
                                    description = "See timeline, challenges, mentors, participant feed, and leaderboard updates for this event.",
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
