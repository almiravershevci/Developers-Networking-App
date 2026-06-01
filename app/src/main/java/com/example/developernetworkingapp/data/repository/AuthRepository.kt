package com.example.developernetworkingapp.data.repository

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
}

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>
    suspend fun login(identifier: String, password: String, rememberMe: Boolean): AuthResult
    suspend fun signup(name: String, username: String, email: String, password: String, rememberMe: Boolean): AuthResult
    suspend fun requestPasswordReset(identifier: String): AuthResult
    suspend fun requestEmailVerification(email: String): AuthResult
    suspend fun verifyEmailCode(email: String, code: String): AuthResult
    fun logout()
}
