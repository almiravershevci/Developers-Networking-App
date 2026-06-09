package com.example.developernetworkingapp.ui.auth

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun rememberGitHubSignInLauncher(
    onSignIn: (Activity) -> Unit,
    onFailure: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val activity = LocalActivity.current as? ComponentActivity
    val scope = rememberCoroutineScope()

    return {
        val host = activity ?: context as? Activity
        if (host == null) {
            onFailure("GitHub sign-in requires an active screen.")
        } else {
            scope.launch {
                onSignIn(host)
            }
        }
    }
}
