package com.example.developernetworkingapp.data.local

import android.content.Context
import androidx.core.content.edit
import com.example.developernetworkingapp.ui.state.SettingsUiState

class SettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): SettingsUiState = SettingsUiState(
        pushEnabled = prefs.getBoolean(KEY_PUSH, true),
        emailDigests = prefs.getBoolean(KEY_EMAIL, true),
        profilePublic = prefs.getBoolean(KEY_PROFILE_PUBLIC, true),
        analyticsOptIn = prefs.getBoolean(KEY_ANALYTICS, false),
        appVersion = prefs.getString(KEY_VERSION, "").orEmpty(),
    )

    fun save(state: SettingsUiState) {
        prefs.edit {
            putBoolean(KEY_PUSH, state.pushEnabled)
            putBoolean(KEY_EMAIL, state.emailDigests)
            putBoolean(KEY_PROFILE_PUBLIC, state.profilePublic)
            putBoolean(KEY_ANALYTICS, state.analyticsOptIn)
            putString(KEY_VERSION, state.appVersion)
        }
    }

    companion object {
        private const val PREFS_NAME = "developer_networking_settings"
        private const val KEY_PUSH = "push_enabled"
        private const val KEY_EMAIL = "email_digests"
        private const val KEY_PROFILE_PUBLIC = "profile_public"
        private const val KEY_ANALYTICS = "analytics_opt_in"
        private const val KEY_VERSION = "app_version"
    }
}
