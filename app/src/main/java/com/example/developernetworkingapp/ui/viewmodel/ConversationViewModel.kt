package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.ChatRepository
import com.example.developernetworkingapp.ui.state.ConversationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConversationViewModel(
    private val conversationId: String,
    private val repository: ChatRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeConversation(conversationId).collect { thread ->
                _uiState.update { current ->
                    current.copy(thread = thread, sendError = null)
                }
            }
        }
    }

    fun onDraftChange(value: String) {
        _uiState.update { it.copy(draft = value, sendError = null) }
    }

    fun sendMessage() {
        val body = _uiState.value.draft.trim()
        if (body.isEmpty() || _uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, sendError = null) }
            val result = repository.sendMessage(conversationId, body)
            _uiState.update { state ->
                if (result.isSuccess) {
                    state.copy(draft = "", isSending = false, sendError = null)
                } else {
                    state.copy(
                        isSending = false,
                        sendError = result.exceptionOrNull()?.message ?: "Couldn't send message",
                    )
                }
            }
        }
    }
}
