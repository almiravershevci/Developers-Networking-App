package com.example.developernetworkingapp.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUser(
    val name: String,
    val username: String,
    val email: String,
    val password: String
)

sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

object AuthRepository {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_REMEMBER_ME = "remember_me"
    private const val KEY_SESSION_EMAIL = "session_email"

    private var isInitialized = false
    private var appContext: Context? = null

    private val users = mutableListOf(
        AuthUser(
            name = "Demo User",
            username = "demo",
            email = "demo@devconnect.app",
            password = "Demo@123"
        )
    )

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    fun initialize(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext
        isInitialized = true
        restoreSession()
    }

    fun login(identifier: String, password: String, rememberMe: Boolean): AuthResult {
        val normalized = identifier.trim().lowercase()
        val user = users.firstOrNull {
            it.email.lowercase() == normalized || it.username.lowercase() == normalized
        } ?: return AuthResult.Error("No account found for this email/username.")

        return if (user.password == password) {
            _currentUser.value = user
            persistSession(user.email, rememberMe)
            AuthResult.Success(user)
        } else {
            AuthResult.Error("Incorrect password. Please try again.")
        }
    }

    fun signup(
        name: String,
        username: String,
        email: String,
        password: String,
        rememberMe: Boolean
    ): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        val normalizedUsername = username.trim().lowercase()

        if (users.any { it.email.lowercase() == normalizedEmail }) {
            return AuthResult.Error("An account with this email already exists.")
        }
        if (users.any { it.username.lowercase() == normalizedUsername }) {
            return AuthResult.Error("This username is already taken.")
        }

        val user = AuthUser(
            name = name.trim(),
            username = username.trim(),
            email = normalizedEmail,
            password = password
        )
        users.add(user)
        _currentUser.value = user
        persistSession(user.email, rememberMe)
        return AuthResult.Success(user)
    }

    fun requestPasswordReset(identifier: String): AuthResult {
        val normalized = identifier.trim().lowercase()
        if (normalized.isBlank()) {
            return AuthResult.Error("Enter your email or username first.")
        }
        val user = users.firstOrNull {
            it.email.lowercase() == normalized || it.username.lowercase() == normalized
        } ?: return AuthResult.Error("No account found for that email/username.")
        return AuthResult.Success(user)
    }

    fun logout() {
        _currentUser.value = null
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()
            ?.putBoolean(KEY_REMEMBER_ME, false)
            ?.remove(KEY_SESSION_EMAIL)
            ?.apply()
    }

    private fun persistSession(email: String, rememberMe: Boolean) {
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()
            ?.putBoolean(KEY_REMEMBER_ME, rememberMe)
            ?.putString(KEY_SESSION_EMAIL, if (rememberMe) email else null)
            ?.apply()
    }

    private fun restoreSession() {
        val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false)
        val savedEmail = prefs.getString(KEY_SESSION_EMAIL, null)
        if (!rememberMe || savedEmail.isNullOrBlank()) return
        val user = users.firstOrNull { it.email.equals(savedEmail, ignoreCase = true) } ?: return
        _currentUser.value = user
    }
}
