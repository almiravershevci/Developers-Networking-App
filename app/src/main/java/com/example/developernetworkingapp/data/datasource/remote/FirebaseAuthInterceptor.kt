package com.example.developernetworkingapp.data.datasource.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches the current user's Firebase ID token so the Node API works for any signed-in teammate.
 */
class FirebaseAuthInterceptor(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val user = firebaseAuth.currentUser
        if (user != null) {
            runCatching {
                val token = Tasks.await(user.getIdToken(false)).token
                if (!token.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
            }
        }
        return chain.proceed(requestBuilder.build())
    }
}
