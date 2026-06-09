@file:Suppress("DEPRECATION")

package com.example.developernetworkingapp.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.developernetworkingapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun rememberGoogleSignInLauncher(
    onIdToken: (String) -> Unit,
    onFailure: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val webClientId = context.getString(R.string.default_web_client_id)

    val googleSignInClient = remember(webClientId) {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val token = account.idToken
            if (token.isNullOrBlank()) {
                onFailure("Google sign-in did not return an ID token.")
            } else {
                onIdToken(token)
            }
        } catch (e: ApiException) {
            if (e.statusCode == 12501) {
                return@rememberLauncherForActivityResult
            }
            onFailure("Google sign-in failed (${e.statusCode}).")
        }
    }

    return {
        if (webClientId.isBlank()) {
            onFailure(
                "Google Sign-In is not configured. Enable Google in Firebase Console and set default_web_client_id in strings.xml.",
            )
        } else {
            launcher.launch(googleSignInClient.signInIntent)
        }
    }
}
