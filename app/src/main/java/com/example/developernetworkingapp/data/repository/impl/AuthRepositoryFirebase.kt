package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.AuthResult
import com.example.developernetworkingapp.data.repository.AuthUser
import com.example.developernetworkingapp.data.repository.UserRole
import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.example.developernetworkingapp.data.datasource.firebase.FirebaseAuthDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirebaseAuthSession
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.GitHubSignInCanceledException
import com.example.developernetworkingapp.data.datasource.firebase.OAuthSignInResult
import com.example.developernetworkingapp.data.datasource.firebase.schema.AccountRole
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Production auth repository: Firebase Auth (identity) + Firestore (profile + username registry).
 * Network work runs on [Dispatchers.IO] — never blocks the UI thread.
 */
class AuthRepositoryFirebase(
    context: Context,
    private val authDataSource: FirebaseAuthDataSource = FirebaseAuthDataSource(),
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource(),
) : AuthRepository {

    private val appContext = context.applicationContext
    private val prefsName = "auth_prefs"
    private val keyRememberMe = "remember_me"
    private val keySessionEmail = "session_email"
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        ioScope.launch { restoreFirebaseSession() }
    }

    override suspend fun login(identifier: String, password: String, rememberMe: Boolean): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val email = userDataSource.resolveEmailForIdentifier(identifier)
                val session = authDataSource.signInWithEmail(email, password)
                if (!session.isEmailVerified) {
                    runCatching { authDataSource.sendEmailVerification() }
                    return@withContext AuthResult.PendingEmailVerification(session.email)
                }
                val profile = userDataSource.fetchUserProfile(session.uid)
                    ?: return@withContext AuthResult.Error("Profile not found. Contact support.")
                mapProfileBlock(profile)?.let { return@withContext AuthResult.Error(it) }
                val syncedProfile = ensureAdminRoleIfEligible(session.uid, email, profile)
                val authUser = mapSessionToAuthUser(session, syncedProfile)
                    ?: return@withContext AuthResult.Error("Profile not found. Contact support.")
                _currentUser.value = authUser
                persistSession(authUser.email, rememberMe)
                AuthResult.Success(authUser)
            } catch (e: Exception) {
                AuthResult.Error(mapAuthError(e))
            }
        }

    override suspend fun signInWithGoogle(idToken: String, rememberMe: Boolean): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                completeOAuthSignIn(
                    result = authDataSource.signInWithGoogle(idToken),
                    rememberMe = rememberMe,
                    providerLabel = "Google",
                )
            } catch (e: Exception) {
                AuthResult.Error(mapAuthError(e))
            }
        }

    override suspend fun signInWithGitHub(activity: Activity, rememberMe: Boolean): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                completeOAuthSignIn(
                    result = authDataSource.signInWithGitHub(activity),
                    rememberMe = rememberMe,
                    providerLabel = "GitHub",
                )
            } catch (e: GitHubSignInCanceledException) {
                AuthResult.Error(e.message ?: "GitHub sign-in was canceled.")
            } catch (e: Exception) {
                AuthResult.Error(mapAuthError(e))
            }
        }

    private suspend fun completeOAuthSignIn(
        result: OAuthSignInResult,
        rememberMe: Boolean,
        providerLabel: String,
    ): AuthResult {
        val existingProfile = userDataSource.fetchUserProfile(result.session.uid)
        if (result.isNewUser || existingProfile == null) {
            val email = result.session.email.trim().lowercase()
            if (email.isBlank()) {
                authDataSource.signOut()
                return AuthResult.Error("$providerLabel account has no email address.")
            }
            val displayName = result.session.displayName?.takeIf { it.isNotBlank() }
                ?: email.substringBefore("@")
            val username = userDataSource.generateAvailableUsername(
                email.substringBefore("@").replace(".", ""),
            )
            val accountRole = if (email == ADMIN_EMAIL) ADMIN_ROLE else USER_ROLE
            userDataSource.createUserProfile(
                uid = result.session.uid,
                email = email,
                username = username,
                displayName = displayName,
                accountRole = accountRole,
            )
        }
        if (!result.session.isEmailVerified) {
            authDataSource.signOut()
            return AuthResult.Error("Please verify your email before logging in.")
        }
        val profile = userDataSource.fetchUserProfile(result.session.uid)
            ?: return AuthResult.Error("Profile not found. Contact support.")
        mapProfileBlock(profile)?.let { return AuthResult.Error(it) }
        val authUser = mapSessionToAuthUser(result.session, profile)
            ?: return AuthResult.Error("Profile not found. Contact support.")
        _currentUser.value = authUser
        persistSession(authUser.email, rememberMe)
        return AuthResult.Success(authUser)
    }

    override suspend fun signup(
        name: String,
        username: String,
        email: String,
        password: String,
        rememberMe: Boolean,
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            val normalizedEmail = email.trim().lowercase()
            val normalizedUsername = username.trim().lowercase()
            if (userDataSource.isUsernameTaken(normalizedUsername)) {
                return@withContext AuthResult.Error("This username is already taken.")
            }
            val session = authDataSource.createUserWithEmail(normalizedEmail, password)
            val accountRole = if (normalizedEmail == ADMIN_EMAIL) ADMIN_ROLE else USER_ROLE
            userDataSource.createUserProfile(
                uid = session.uid,
                email = normalizedEmail,
                username = username,
                displayName = name,
                accountRole = accountRole,
            )
            authDataSource.sendEmailVerification()
            val authUser = mapSessionToAuthUser(session, passwordPlaceholder = password)
                ?: return@withContext AuthResult.Error("Account created but profile could not be loaded.")
            _currentUser.value = authUser.copy(isVerified = false)
            persistSession(normalizedEmail, rememberMe)
            AuthResult.Success(authUser.copy(isVerified = false))
        } catch (e: Exception) {
            AuthResult.Error(mapAuthError(e))
        }
    }

    override suspend fun requestPasswordReset(identifier: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val normalized = identifier.trim()
            if (normalized.isBlank()) {
                return@withContext AuthResult.Error("Enter your email or username first.")
            }
            val email = userDataSource.resolveEmailForIdentifier(normalized)
            authDataSource.sendPasswordResetEmail(email)
            val placeholder = AuthUser(
                name = "",
                username = "",
                email = email,
                password = "",
                isVerified = true,
            )
            AuthResult.Success(placeholder)
        } catch (e: Exception) {
            AuthResult.Error(mapAuthError(e))
        }
    }

    override suspend fun requestEmailVerification(email: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val session = authDataSource.currentSession()
                ?: return@withContext AuthResult.Error(
                    "Go to Login, enter your password, and tap Login — we'll send a fresh verification email.",
                )
            authDataSource.sendEmailVerification()
            val authUser = mapSessionToAuthUser(session)
                ?: return@withContext AuthResult.Error("No account found for this email.")
            AuthResult.Success(authUser)
        } catch (e: Exception) {
            AuthResult.Error(mapAuthError(e))
        }
    }

    override suspend fun verifyEmailCode(email: String, code: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            if (authDataSource.currentSession() == null) {
                return@withContext AuthResult.Error(
                    "No active session. Sign in again after verifying from your email link.",
                )
            }
            val reloaded = authDataSource.reloadCurrentSession()
            if (!reloaded.isEmailVerified) {
                return@withContext AuthResult.Error(
                    "Email not verified yet. Open the link in your inbox, then tap Verify again.",
                )
            }
            userDataSource.markEmailVerifiedInProfile(reloaded.uid)
            val authUser = mapSessionToAuthUser(reloaded)
                ?: return@withContext AuthResult.Error("No account found for this email.")
            _currentUser.value = authUser.copy(isVerified = true)
            AuthResult.Success(authUser.copy(isVerified = true))
        } catch (e: Exception) {
            AuthResult.Error(mapAuthError(e))
        }
    }

    override fun logout() {
        authDataSource.signOut()
        _currentUser.value = null
        appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit {
            putBoolean(keyRememberMe, false)
            remove(keySessionEmail)
        }
    }

    override suspend fun deleteAccount(password: String?, googleIdToken: String?): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val session = authDataSource.currentSession()
                    ?: return@withContext AuthResult.Error("Not signed in.")
                val profile = userDataSource.fetchUserProfile(session.uid)
                    ?: return@withContext AuthResult.Error("Profile not found.")

                when {
                    !googleIdToken.isNullOrBlank() ->
                        authDataSource.reauthenticateWithGoogle(googleIdToken)
                    !password.isNullOrBlank() ->
                        authDataSource.reauthenticateWithEmail(session.email, password)
                }

                userDataSource.deleteUserAccount(session.uid, profile.usernameLower)
                authDataSource.deleteCurrentUser()

                _currentUser.value = null
                appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit {
                    putBoolean(keyRememberMe, false)
                    remove(keySessionEmail)
                }

                AuthResult.Success(
                    AuthUser(
                        name = "",
                        username = "",
                        email = "",
                        password = "",
                        isVerified = true,
                    ),
                )
            } catch (e: Exception) {
                AuthResult.Error(mapDeleteAccountError(e, password, googleIdToken))
            }
        }

    private suspend fun ensureAdminRoleIfEligible(
        uid: String,
        email: String,
        profile: com.example.developernetworkingapp.data.datasource.firebase.schema.UserProfileDoc,
    ): com.example.developernetworkingapp.data.datasource.firebase.schema.UserProfileDoc {
        if (email.trim().lowercase() != ADMIN_EMAIL || profile.accountRole == ADMIN_ROLE) {
            return profile
        }
        userDataSource.updateAccountRole(uid, ADMIN_ROLE)
        return profile.copy(accountRole = ADMIN_ROLE)
    }

    private suspend fun mapSessionToAuthUser(
        session: FirebaseAuthSession,
        profile: com.example.developernetworkingapp.data.datasource.firebase.schema.UserProfileDoc? = null,
        passwordPlaceholder: String = "",
    ): AuthUser? {
        val userProfile = profile ?: userDataSource.fetchUserProfile(session.uid) ?: return null
        val role = if (userProfile.accountRole == ADMIN_ROLE) UserRole.ADMIN else UserRole.USER
        return AuthUser(
            name = userProfile.displayName,
            username = userProfile.usernameLower,
            email = userProfile.email.ifBlank { session.email },
            password = passwordPlaceholder,
            isVerified = session.isEmailVerified || userProfile.emailVerified,
            role = role,
        )
    }

    private suspend fun restoreFirebaseSession() {
        val session = authDataSource.currentSession() ?: return
        try {
            val profile = userDataSource.fetchUserProfile(session.uid) ?: return
            if (mapProfileBlock(profile) != null) {
                authDataSource.signOut()
                return
            }
            val authUser = mapSessionToAuthUser(session, profile) ?: return
            if (authUser.isVerified) {
                _currentUser.value = authUser
            }
        } catch (_: Exception) {
            authDataSource.signOut()
        }
    }

    private fun persistSession(email: String, rememberMe: Boolean) {
        appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit {
            putBoolean(keyRememberMe, rememberMe)
            putString(keySessionEmail, if (rememberMe) email else null)
        }
    }

    private fun mapAuthError(error: Exception): String {
        if (error is FirebaseAuthException) {
            return when (error.errorCode) {
                "ERROR_USER_NOT_FOUND",
                "ERROR_INVALID_EMAIL",
                -> "No account found for this email/username."
                "ERROR_WRONG_PASSWORD",
                "ERROR_INVALID_CREDENTIAL",
                -> "Incorrect password. Please try again."
                "ERROR_EMAIL_ALREADY_IN_USE",
                -> "An account with this email already exists."
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL",
                -> "An account already exists with this email. Sign in with your original method."
                "ERROR_TOO_MANY_REQUESTS",
                -> "Too many attempts. Wait a few minutes or use a real device, then try Resend."
                else -> error.message ?: "Authentication failed."
            }
        }
        return error.message ?: "Something went wrong. Try again."
    }

    private fun mapDeleteAccountError(
        error: Exception,
        password: String?,
        googleIdToken: String?,
    ): String {
        if (error is FirebaseAuthException && error.errorCode == "ERROR_REQUIRES_RECENT_LOGIN") {
            return when {
                !googleIdToken.isNullOrBlank() ->
                    "Could not verify your identity. Sign in with Google again, then retry."
                password.isNullOrBlank() ->
                    "Enter your password to confirm account deletion."
                else -> "Could not verify your identity. Sign out, sign in again, then retry."
            }
        }
        return mapAuthError(error)
    }

    private fun mapProfileBlock(profile: com.example.developernetworkingapp.data.datasource.firebase.schema.UserProfileDoc): String? =
        when (profile.accountRole) {
            AccountRole.BANNED -> "This account has been banned."
            AccountRole.DEACTIVATED -> "This account is deactivated."
            else -> null
        }

    private companion object {
        const val ADMIN_EMAIL = "admin@devconnect.app"
        const val ADMIN_ROLE = "admin"
        const val USER_ROLE = "user"
    }
}
