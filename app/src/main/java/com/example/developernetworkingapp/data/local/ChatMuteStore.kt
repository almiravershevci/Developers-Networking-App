package com.example.developernetworkingapp.data.local

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists which conversations have notifications muted (local prefs).
 */
class ChatMuteStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _mutedTitles = MutableStateFlow(readPersisted())

    val mutedTitles: StateFlow<Set<String>> = _mutedTitles.asStateFlow()

    fun toggleMute(conversationTitle: String) {
        val next = _mutedTitles.value.toMutableSet()
        if (!next.add(conversationTitle)) next.remove(conversationTitle)
        persist(next)
        _mutedTitles.value = next.toSet()
    }

    private fun readPersisted(): Set<String> =
        prefs.getStringSet(KEY_MUTED_TITLES, null)?.toSet() ?: emptySet()

    private fun persist(titles: Set<String>) {
        prefs.edit { putStringSet(KEY_MUTED_TITLES, HashSet(titles)) }
    }

    companion object {
        private const val PREFS_NAME = "chat_mute_prefs"
        private const val KEY_MUTED_TITLES = "muted_conversation_titles"
    }
}
