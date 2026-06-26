package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.developernetworkingapp.di.appViewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.data.datasource.firebase.schema.ConversationKind
import com.example.developernetworkingapp.domain.model.ChatQuickRooms
import com.example.developernetworkingapp.domain.model.ConversationSummary
import com.example.developernetworkingapp.ui.components.EmptyStateCard
import com.example.developernetworkingapp.ui.components.PremiumInfoCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.util.userFacingStatusMessage
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.ChatUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.ChatViewModel

@Composable
fun ChatRoute(padding: PaddingValues, navController: NavController) {
    val viewModel: ChatViewModel = appViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChatScreen(
        padding = padding,
        state = state,
        navController = navController,
        onToggleMute = { viewModel.toggleMute(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    padding: PaddingValues,
    state: ChatUiState,
    navController: NavController,
    onToggleMute: (String) -> Unit,
) {
    var selectedConversation by remember { mutableStateOf<ConversationSummary?>(null) }
    val inbox = state.content?.inbox.orEmpty()

    selectedConversation?.let { convo ->
        AlertDialog(
            onDismissRequest = { selectedConversation = null },
            title = { Text(convo.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(convo.preview, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${kindLabel(convo.conversationKind)} · ${convo.participantCount} members · ${convo.relativeTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    if (state.mutedConversations.contains(convo.title)) {
                        Text(
                            "Notifications muted for this chat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedConversation = null
                    navController.navigate(AppRoutes.conversationRoute(convo.id))
                }) { Text("Open chat") }
            },
            dismissButton = { TextButton(onClick = { selectedConversation = null }) { Text("Close") } },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            )
            .padding(padding)
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding,
    ) {
        item {
            ElevatedCard(
                shape = AppDesignTokens.cardLargeShape,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Realtime Collaboration Inbox", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Firestore-backed threads with live snapshots, read receipts, and secure participant rules.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ChatQuickRooms.rooms.forEach { room ->
                            AssistChip(
                                onClick = {
                                    navController.navigate(AppRoutes.conversationRoute(room.conversationId))
                                },
                                label = { Text(room.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (room.label == "Project Room") {
                                            Icons.Outlined.Groups
                                        } else {
                                            Icons.Outlined.ChatBubbleOutline
                                        },
                                        contentDescription = null,
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                ),
                            )
                        }
                    }
                }
            }
        }

        state.content?.let { content ->
            content.statusMessage?.let { message ->
                item {
                    PremiumInfoCard(
                        title = if (content.isSignedIn) "Inbox status" else "Sign in required",
                        subtitle = userFacingStatusMessage(message) ?: message,
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("Conversations")
                Text(
                    "${inbox.size} active",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (inbox.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No conversations yet",
                    subtitle = "Start from a quick room above or open a project thread to begin messaging.",
                )
            }
        }

        items(inbox, key = { it.id }) { summary ->
            ConversationInboxCard(
                summary = summary,
                isMuted = state.mutedConversations.contains(summary.title),
                onOpen = { selectedConversation = summary },
                onOpenDirect = {
                    navController.navigate(AppRoutes.conversationRoute(summary.id))
                },
                onToggleMute = { onToggleMute(summary.title) },
            )
        }

        item {
            PremiumInfoCard(
                title = "Start a conversation",
                subtitle = "Open any thread above to compose messages with read receipts and live updates.",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationInboxCard(
    summary: ConversationSummary,
    isMuted: Boolean,
    onOpen: () -> Unit,
    onOpenDirect: () -> Unit,
    onToggleMute: () -> Unit,
) {
    ElevatedCard(
        onClick = onOpen,
        shape = AppDesignTokens.cardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = if (summary.conversationKind == ConversationKind.GROUP) {
                                Icons.Outlined.Groups
                            } else {
                                Icons.Outlined.Person
                            },
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            summary.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            kindLabel(summary.conversationKind),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
                if (isMuted) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsOff,
                        contentDescription = "Notifications muted",
                        tint = MaterialTheme.colorScheme.outline,
                    )
                } else if (summary.unreadCount > 0) {
                    BadgedBox(badge = { Badge { Text("${summary.unreadCount}") } }) {
                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Unread")
                    }
                }
            }

            Text(
                summary.preview,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    summary.relativeTime.ifBlank { "Synced" },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
                Text(
                    "${summary.participantCount} members",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onOpenDirect) { Text("Open") }
                TextButton(onClick = onToggleMute) {
                    Text(if (isMuted) "Unmute" else "Mute")
                }
            }
        }
    }
}

private fun kindLabel(kind: String): String = when (kind) {
    ConversationKind.GROUP -> "Group"
    ConversationKind.PROJECT_THREAD -> "Project thread"
    ConversationKind.DIRECT -> "Direct"
    else -> "Chat"
}
