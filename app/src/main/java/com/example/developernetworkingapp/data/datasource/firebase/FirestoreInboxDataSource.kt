package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.example.developernetworkingapp.data.datasource.firebase.schema.NotificationKind
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * User-to-user inbox writes (join requests, task assignments, moderation notices).
 * Requires inbox create rules that allow signed-in users to notify other users.
 */
class FirestoreInboxDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun createNotification(
        recipientUserId: String,
        title: String,
        body: String,
        deepLink: String? = null,
    ): String {
        val ref = db.collection(FirestorePaths.INBOX).document()
        val payload = mapOf(
            "schemaVersion" to 1,
            "recipientUserId" to recipientUserId,
            "notificationKind" to NotificationKind.FEED,
            "title" to title.trim(),
            "body" to body.trim(),
            "deepLink" to deepLink,
            "read" to false,
            "createdAt" to FieldValue.serverTimestamp(),
        )
        ref.set(payload).await()
        return ref.id
    }
}
