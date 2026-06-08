package com.example.developernetworkingapp.notifications

import android.content.Context
import android.util.Log
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.authStateChanges
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Registers the device FCM token on the signed-in user's Firestore profile.
 * Works for every teammate — each device adds its token to users/{uid}.fcmTokens.
 */
class FcmTokenRegistrar(
    @Suppress("UNUSED_PARAMETER") context: Context,
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch {
            firebaseAuth.authStateChanges().collect { user ->
                if (user != null && user.isEmailVerified) {
                    syncCurrentDeviceToken(user.uid)
                }
            }
        }
    }

    companion object {
        private const val TAG = "FcmTokenRegistrar"

        fun syncToken(context: Context, userId: String, token: String) {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            scope.launch {
                runCatching {
                    FirestoreUserDataSource().upsertFcmToken(userId, token)
                }.onFailure { error ->
                    Log.w(TAG, "FCM token upsert failed for $userId", error)
                }
            }
        }
    }

    private suspend fun syncCurrentDeviceToken(userId: String) {
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            userDataSource.upsertFcmToken(userId, token)
        }.onFailure { error ->
            Log.w(TAG, "FCM token registration failed for $userId", error)
        }
    }
}
