package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.developernetworkingapp.ui.theme.AppDesignTokens

private data class ChatLine(val text: String, val fromSelf: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    padding: PaddingValues,
    navController: NavController,
    conversationTitle: String
) {
    var draft by remember { mutableStateOf("") }
    val seed = conversationTitle.hashCode()
    val initialMessages = remember(conversationTitle) {
        listOf(
            ChatLine("Hey — quick sync on the latest API draft when you have a moment.", fromSelf = false),
            ChatLine("On it. I'll drop comments on the contract section and ping you before EOD.", fromSelf = true),
            ChatLine("Perfect. Also flagged two edge cases in error payloads — worth aligning with backend.", fromSelf = false),
            ChatLine("Saw those. I'll thread replies there so we keep one paper trail.", fromSelf = true),
            ChatLine(
                "Sounds good. Once we lock fields, I'll update the mobile client and cut a test build.",
                fromSelf = seed % 2 == 0
            )
        )
    }
    var messages by remember(conversationTitle) { mutableStateOf(initialMessages) }
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        modifier = Modifier.padding(padding),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(conversationTitle, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Text("Live • messages end-to-end for this room", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
            ) {
                items(
                    count = messages.size,
                    key = { index -> index }
                ) { index ->
                    val line = messages[index]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (line.fromSelf) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (line.fromSelf) 18.dp else 4.dp,
                                bottomEnd = if (line.fromSelf) 4.dp else 18.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (line.fromSelf) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                }
                            )
                        ) {
                            Text(
                                line.text,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDesignTokens.screenHorizontalPadding, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message…") },
                    minLines = 1,
                    maxLines = 4
                )
                IconButton(
                    onClick = {
                        val text = draft.trim()
                        if (text.isNotEmpty()) {
                            messages = messages + ChatLine(text, fromSelf = true)
                            draft = ""
                        }
                    },
                    enabled = draft.isNotBlank(),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}
