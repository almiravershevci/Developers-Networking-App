package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.local.ChatMuteStore
import com.example.developernetworkingapp.data.repository.ChatRepository
import com.example.developernetworkingapp.ui.state.ChatUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val muteStore: ChatMuteStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.observeChat(), muteStore.mutedTitles) { content, muted ->
                ChatUiState(content = content, mutedConversations = muted)
            }.collect { _uiState.value = it }
        }
    }

    fun toggleMute(conversationTitle: String) {
        muteStore.toggleMute(conversationTitle)
    }
}
