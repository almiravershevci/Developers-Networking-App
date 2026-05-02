package com.example.developernetworkingapp.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class UserRole {
    USER,
    ADMIN
}

data class AuthUser(
    val name: String,
    val username: String,
    val email: String,
    val password: String,
    val isVerified: Boolean = false,
    val role: UserRole = UserRole.USER
)

sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>
    fun login(identifier: String, password: String, rememberMe: Boolean): AuthResult
    fun signup(name: String, username: String, email: String, password: String, rememberMe: Boolean): AuthResult
    fun requestPasswordReset(identifier: String): AuthResult
    fun requestEmailVerification(email: String): AuthResult
    fun verifyEmailCode(email: String, code: String): AuthResult
    fun logout()
}

class AuthRepositoryImpl(
    context: Context
) : AuthRepository {
    private val prefsName = "auth_prefs"
    private val keyRememberMe = "remember_me"
    private val keySessionEmail = "session_email"

    private val appContext: Context = context.applicationContext

    private val users = mutableListOf(
        AuthUser(
            name = "Demo User",
            username = "demo",
            email = "demo@devconnect.app",
            password = "Demo@123",
            isVerified = true,
            role = UserRole.USER
        ),
        AuthUser(
            name = "Admin",
            username = "admin",
            email = "admin@devconnect.app",
            password = "Admin@123",
            isVerified = true,
            role = UserRole.ADMIN
        )
    )

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        restoreSession()
    }

    override fun login(identifier: String, password: String, rememberMe: Boolean): AuthResult {
        val normalized = identifier.trim().lowercase()
        val user = users.firstOrNull {
            it.email.lowercase() == normalized || it.username.lowercase() == normalized
        } ?: return AuthResult.Error("No account found for this email/username.")

        return if (user.password == password) {
            if (!user.isVerified) {
                return AuthResult.Error("Please verify your email before logging in.")
            }
            _currentUser.value = user
            persistSession(user.email, rememberMe)
            AuthResult.Success(user)
        } else {
            AuthResult.Error("Incorrect password. Please try again.")
        }
    }

    override fun signup(
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
            password = password,
            isVerified = false,
            role = UserRole.USER
        )
        users.add(user)
        return AuthResult.Success(user)
    }

    override fun requestPasswordReset(identifier: String): AuthResult {
        val normalized = identifier.trim().lowercase()
        if (normalized.isBlank()) {
            return AuthResult.Error("Enter your email or username first.")
        }
        val user = users.firstOrNull {
            it.email.lowercase() == normalized || it.username.lowercase() == normalized
        } ?: return AuthResult.Error("No account found for that email/username.")
        return AuthResult.Success(user)
    }

    override fun requestEmailVerification(email: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        val user = users.firstOrNull { it.email == normalizedEmail }
            ?: return AuthResult.Error("No account found for this email.")
        return if (user.isVerified) {
            AuthResult.Error("This email is already verified.")
        } else {
            AuthResult.Success(user)
        }
    }

    override fun verifyEmailCode(email: String, code: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        if (code.trim() != "123456") {
            return AuthResult.Error("Invalid verification code.")
        }
        val index = users.indexOfFirst { it.email == normalizedEmail }
        if (index == -1) {
            return AuthResult.Error("No account found for this email.")
        }
        val verifiedUser = users[index].copy(isVerified = true)
        users[index] = verifiedUser
        return AuthResult.Success(verifiedUser)
    }

    override fun logout() {
        _currentUser.value = null
        appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
            .putBoolean(keyRememberMe, false)
            .remove(keySessionEmail)
            .apply()
    }

    private fun persistSession(email: String, rememberMe: Boolean) {
        appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
            .putBoolean(keyRememberMe, rememberMe)
            .putString(keySessionEmail, if (rememberMe) email else null)
            .apply()
    }

    private fun restoreSession() {
        val prefs = appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val rememberMe = prefs.getBoolean(keyRememberMe, false)
        val savedEmail = prefs.getString(keySessionEmail, null)
        if (!rememberMe || savedEmail.isNullOrBlank()) return
        val user = users.firstOrNull { it.email.equals(savedEmail, ignoreCase = true) } ?: return
        _currentUser.value = user
    }
}
