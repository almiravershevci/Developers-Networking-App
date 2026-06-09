package com.example.developernetworkingapp.data.datasource.firebase

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/** Snapshot of the signed-in Firebase user without exposing Firebase types to repositories. */
data class FirebaseAuthSession(
    val uid: String,
    val email: String,
    val isEmailVerified: Boolean,
    val displayName: String? = null,
)

data class GoogleSignInResult(
    val session: FirebaseAuthSession,
    val isNewUser: Boolean,
)

private fun FirebaseUser.toSession(): FirebaseAuthSession = FirebaseAuthSession(
    uid = uid,
    email = email.orEmpty(),
    isEmailVerified = isEmailVerified,
    displayName = displayName,
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

    suspend fun signInWithGoogle(idToken: String): GoogleSignInResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: error("Google sign-in succeeded but user is null.")
        return GoogleSignInResult(
            session = user.toSession(),
            isNewUser = result.additionalUserInfo?.isNewUser == true,
        )
    }

    suspend fun createUserWithEmail(email: String, password: String): FirebaseAuthSession {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        return result.user?.toSession() ?: error("Registration succeeded but user is null.")
    }

    suspend fun reauthenticateWithEmail(email: String, password: String) {
        val user = auth.currentUser ?: error("No signed-in user.")
        val credential = EmailAuthProvider.getCredential(email.trim(), password)
        user.reauthenticate(credential).await()
    }

    suspend fun reauthenticateWithGoogle(idToken: String) {
        val user = auth.currentUser ?: error("No signed-in user.")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        user.reauthenticate(credential).await()
    }

    suspend fun deleteCurrentUser() {
        val user = auth.currentUser ?: error("No signed-in user.")
        user.delete().await()
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
