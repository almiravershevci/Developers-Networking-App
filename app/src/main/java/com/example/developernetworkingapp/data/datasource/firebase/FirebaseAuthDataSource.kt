package com.example.developernetworkingapp.data.datasource.firebase

import android.app.Activity
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.tasks.await

/** Snapshot of the signed-in Firebase user without exposing Firebase types to repositories. */
data class FirebaseAuthSession(
    val uid: String,
    val email: String,
    val isEmailVerified: Boolean,
    val displayName: String? = null,
)

data class OAuthSignInResult(
    val session: FirebaseAuthSession,
    val isNewUser: Boolean,
)

typealias GoogleSignInResult = OAuthSignInResult

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

    suspend fun signInWithGoogle(idToken: String): OAuthSignInResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: error("Google sign-in succeeded but user is null.")
        return OAuthSignInResult(
            session = user.toSession(),
            isNewUser = result.additionalUserInfo?.isNewUser == true,
        )
    }

    suspend fun signInWithGitHub(activity: Activity): OAuthSignInResult {
        val provider = OAuthProvider.newBuilder("github.com")
            .setScopes(listOf("user:email"))
            .build()
        return try {
            val result = auth.startActivityForSignInWithProvider(activity, provider).await()
            val user = result.user ?: error("GitHub sign-in succeeded but user is null.")
            OAuthSignInResult(
                session = user.toSession(),
                isNewUser = result.additionalUserInfo?.isNewUser == true,
            )
        } catch (e: FirebaseAuthException) {
            if (e.errorCode == "ERROR_WEB_CONTEXT_CANCELED") {
                throw GitHubSignInCanceledException()
            }
            throw e
        }
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

    suspend fun sendEmailVerification(
        androidPackageName: String = ANDROID_PACKAGE_NAME,
        continueUrl: String = AUTH_CONTINUE_URL,
    ) {
        val user = auth.currentUser ?: error("No signed-in user to verify.")
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl(continueUrl)
            .setHandleCodeInApp(true)
            .setAndroidPackageName(androidPackageName, true, null)
            .build()
        user.sendEmailVerification(actionCodeSettings).await()
    }

    private companion object {
        const val ANDROID_PACKAGE_NAME = "com.example.developernetworkingapp"
        const val AUTH_CONTINUE_URL = "https://developers-networking-app.firebaseapp.com"
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

class GitHubSignInCanceledException : Exception("GitHub sign-in was canceled.")
