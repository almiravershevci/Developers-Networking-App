package com.example.developernetworkingapp.data.datasource.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/** Snapshot of the signed-in Firebase user without exposing Firebase types to repositories. */
data class FirebaseAuthSession(
    val uid: String,
    val email: String,
    val isEmailVerified: Boolean,
)

private fun FirebaseUser.toSession(): FirebaseAuthSession = FirebaseAuthSession(
    uid = uid,
    email = email.orEmpty(),
    isEmailVerified = isEmailVerified,
)

/**
 * Thin wrapper around Firebase Authentication (identity provider).
 */
class FirebaseAuthDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    fun currentSession(): FirebaseAuthSession? = auth.currentUser?.toSession()

    suspend fun signInWithEmail(email: String, password: String): FirebaseAuthSession {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        return result.user?.toSession() ?: error("Sign-in succeeded but user is null.")
    }

    suspend fun createUserWithEmail(email: String, password: String): FirebaseAuthSession {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        return result.user?.toSession() ?: error("Registration succeeded but user is null.")
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    suspend fun sendEmailVerification() {
        val user = auth.currentUser ?: error("No signed-in user to verify.")
        user.sendEmailVerification().await()
    }

    suspend fun reloadCurrentSession(): FirebaseAuthSession {
        val user = auth.currentUser ?: error("No signed-in user.")
        user.reload().await()
        return user.toSession()
    }

    fun signOut() {
        auth.signOut()
    }
}
