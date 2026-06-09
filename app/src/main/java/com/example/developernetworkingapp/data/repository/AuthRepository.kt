package com.example.developernetworkingapp.data.repository

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

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
    /** Signed in but email not verified; session kept so verification email can be resent. */
    data class PendingEmailVerification(val email: String) : AuthResult()
}

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>
    suspend fun login(identifier: String, password: String, rememberMe: Boolean): AuthResult
    suspend fun signInWithGoogle(idToken: String, rememberMe: Boolean): AuthResult
    suspend fun signInWithGitHub(activity: Activity, rememberMe: Boolean): AuthResult
    suspend fun signup(name: String, username: String, email: String, password: String, rememberMe: Boolean): AuthResult
    suspend fun requestPasswordReset(identifier: String): AuthResult
    suspend fun requestEmailVerification(email: String): AuthResult
    suspend fun verifyEmailCode(email: String, code: String): AuthResult
    suspend fun deleteAccount(password: String? = null, googleIdToken: String? = null): AuthResult
    fun logout()
}
