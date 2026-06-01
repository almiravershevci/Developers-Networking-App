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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.data.datasource.firebase.schema.MessageKind
import com.example.developernetworkingapp.data.repository.impl.ChatRepositoryFirestore
import com.example.developernetworkingapp.domain.model.ChatMessage
import com.example.developernetworkingapp.ui.state.ConversationUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.ConversationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    padding: PaddingValues,
    navController: NavController,
    conversationId: String,
) {
    val viewModel: ConversationViewModel = viewModel(
        key = conversationId,
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ConversationViewModel(conversationId) as T
            }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.sendError) {
        state.sendError?.let { snackbarHostState.showSnackbar(it) }
    }

    val thread = state.thread
    Scaffold(
        modifier = Modifier.padding(padding),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            thread?.title ?: "Conversation",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            thread?.subtitle ?: "Loading thread…",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        ConversationBody(
            innerPadding = innerPadding,
            state = state,
            onDraftChange = viewModel::onDraftChange,
            onSend = viewModel::sendMessage,
        )
    }
}

@Composable
private fun ConversationBody(
    innerPadding: PaddingValues,
    state: ConversationUiState,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val thread = state.thread
    val messages = thread?.messages.orEmpty()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            )
            .padding(innerPadding),
    ) {
        when {
            thread == null || thread.isLoading -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                }
            }
            thread.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(AppDesignTokens.screenHorizontalPadding),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(thread.errorMessage, style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(message = message)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDesignTokens.screenHorizontalPadding, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(thread?.composerHint ?: "Message…") },
                enabled = thread?.errorMessage == null && !state.isSending,
                minLines = 1,
                maxLines = 4,
            )
            IconButton(
                onClick = onSend,
                enabled = state.draft.isNotBlank() && !state.isSending && thread?.errorMessage == null,
                modifier = Modifier.padding(bottom = 4.dp),
            ) {
                if (state.isSending) {
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val mentionPrefix = ChatRepositoryFirestore.mentionPrefix(message.messageKind)
    val displayBody = when (message.messageKind) {
        MessageKind.MENTION -> message.body
        MessageKind.SYSTEM -> message.body
        else -> message.body
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromSelf) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (message.fromSelf) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (!message.fromSelf) {
                Text(
                    text = buildString {
                        if (mentionPrefix != null) append(mentionPrefix)
                        append(message.senderLabel)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Card(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (message.fromSelf) 18.dp else 4.dp,
                    bottomEnd = if (message.fromSelf) 4.dp else 18.dp,
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.fromSelf) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                ),
            ) {
                Text(
                    displayBody,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
