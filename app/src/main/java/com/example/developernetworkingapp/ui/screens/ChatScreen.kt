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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
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
import com.example.developernetworkingapp.ui.components.PremiumInfoCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.state.ChatUiState
import com.example.developernetworkingapp.ui.viewmodel.ChatViewModel

@Composable
fun ChatRoute(padding: PaddingValues) {
    val viewModel: ChatViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChatScreen(padding, state)
}

@Composable
fun ChatScreen(padding: PaddingValues, state: ChatUiState) {
    var selectedConversation by remember { mutableStateOf<String?>(null) }
    val quickRooms = listOf("Project Room", "Mentorship", "Hackathon Team", "General")

    selectedConversation?.let { convo ->
        AlertDialog(
            onDismissRequest = { selectedConversation = null },
            title = { Text(convo) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This conversation includes rich messages, file sharing, and task references.")
                    Text("Status: 2 members typing • 5 unread", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { Button(onClick = { selectedConversation = null }) { Text("Open chat") } },
            dismissButton = { TextButton(onClick = { selectedConversation = null }) { Text("Close") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        androidx.compose.material3.MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                        androidx.compose.material3.MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Realtime Collaboration Inbox", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        quickRooms.forEach { room ->
                            AssistChip(
                                onClick = {},
                                label = { Text(room) },
                                leadingIcon = {
                                    androidx.compose.material3.Icon(
                                        imageVector = if (room == "Project Room") Icons.Outlined.Groups else Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = null
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                            )
                        }
                    }
                }
            }
        }
        item { SectionTitle("Conversations") }
        items(state.content?.conversations ?: emptyList()) { item ->
            ElevatedCard(
                onClick = { selectedConversation = item },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Text("Typing indicators • read receipts • pinned messages", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { selectedConversation = item }) { Text("Open") }
                        TextButton(onClick = {}) { Text("Mute") }
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                readOnly = true,
                label = { Text(state.content?.composerHint ?: "Type a message...") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            PremiumInfoCard(
                title = "Message actions",
                subtitle = "Reply in thread, react with emoji, attach task card, or jump to linked project."
            )
        }
    }
}
