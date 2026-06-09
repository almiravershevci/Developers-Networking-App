package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProfileVisibility
import com.example.developernetworkingapp.data.datasource.firebase.schema.UserProfileDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.UsernameRegistryDoc
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Firestore access for user profiles and username registry (unique handles).
 */
class FirestoreUserDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun isUsernameTaken(usernameLower: String): Boolean {
        val snap = db.collection(FirestorePaths.USERNAMES).document(usernameLower).get().await()
        return snap.exists()
    }

    suspend fun resolveEmailForIdentifier(identifier: String): String {
        val normalized = identifier.trim().lowercase()
        if (normalized.contains("@")) return normalized

        val usernameSnap = db.collection(FirestorePaths.USERNAMES).document(normalized).get().await()
        if (!usernameSnap.exists()) {
            error("No account found for this email/username.")
        }
        val userId = usernameSnap.getString("userId")
            ?: error("Username registry entry is invalid.")
        val userSnap = db.collection(FirestorePaths.USERS).document(userId).get().await()
        if (!userSnap.exists()) {
            error("User profile not found.")
        }
        val email = userSnap.getString("email")
        if (email.isNullOrBlank()) {
            error("Account email is missing. Contact support.")
        }
        return email.trim().lowercase()
    }

    suspend fun createUserProfile(
        uid: String,
        email: String,
        username: String,
        displayName: String,
        accountRole: String,
    ) {
        val usernameLower = username.trim().lowercase()
        val now = Timestamp.now()
        val profile = hashMapOf(
            "schemaVersion" to 2,
            "displayName" to displayName.trim(),
            "usernameLower" to usernameLower,
            "email" to email.trim().lowercase(),
            "headline" to "",
            "bio" to "",
            "skillTags" to emptyList<String>(),
            "profileVisibility" to ProfileVisibility.PUBLIC,
            "accountRole" to accountRole,
            "createdAt" to now,
            "updatedAt" to now,
            "lastActiveAt" to now,
        )
        val registry = hashMapOf(
            "usernameLower" to usernameLower,
            "userId" to uid,
        )

        db.collection(FirestorePaths.USERS).document(uid).set(profile).await()
        db.collection(FirestorePaths.USERNAMES).document(usernameLower).set(registry).await()
    }

    suspend fun fetchUserProfile(uid: String): UserProfileDoc? {
        val snap = db.collection(FirestorePaths.USERS).document(uid).get().await()
        if (!snap.exists()) return null
        return snap.toObject(UserProfileDoc::class.java)?.copy(id = snap.id)
    }

    suspend fun fetchUserProfiles(userIds: Collection<String>): Map<String, UserProfileDoc> {
        if (userIds.isEmpty()) return emptyMap()
        val result = mutableMapOf<String, UserProfileDoc>()
        for (uid in userIds.distinct()) {
            fetchUserProfile(uid)?.let { result[uid] = it }
        }
        return result
    }

    suspend fun updateUserProfile(
        uid: String,
        displayName: String,
        headline: String,
        bio: String,
    ) {
        db.collection(FirestorePaths.USERS).document(uid)
            .set(
                mapOf(
                    "displayName" to displayName.trim(),
                    "headline" to headline.trim(),
                    "bio" to bio.trim(),
                    "updatedAt" to Timestamp.now(),
                ),
                SetOptions.merge(),
            )
            .await()
    }

    suspend fun upsertFcmToken(uid: String, token: String) {
        val trimmed = token.trim()
        require(uid.isNotBlank() && trimmed.isNotEmpty())

        db.collection(FirestorePaths.USERS).document(uid)
            .set(
                mapOf(
                    "fcmTokens" to FieldValue.arrayUnion(trimmed),
                    "updatedAt" to Timestamp.now(),
                ),
                SetOptions.merge(),
            )
            .await()
    }

    suspend fun markEmailVerifiedInProfile(uid: String) {
        db.collection(FirestorePaths.USERS).document(uid)
            .set(
                mapOf(
                    "emailVerified" to true,
                    "updatedAt" to Timestamp.now(),
                ),
                SetOptions.merge(),
            )
            .await()
    }

    suspend fun generateAvailableUsername(base: String): String {
        val sanitized = base.trim().lowercase()
            .filter { it.isLetterOrDigit() }
            .take(20)
        val root = sanitized.takeIf { it.length >= 3 } ?: "user${sanitized.ifBlank { "dev" }}"
        var candidate = root
        var suffix = 0
        while (isUsernameTaken(candidate)) {
            suffix++
            candidate = "$root$suffix"
        }
        return candidate
    }

    suspend fun deleteUserAccount(uid: String, usernameLower: String) {
        val inboxSnap = db.collection(FirestorePaths.INBOX)
            .whereEqualTo("recipientUserId", uid)
            .get()
            .await()

        val batch = db.batch()
        inboxSnap.documents.forEach { batch.delete(it.reference) }
        batch.delete(db.collection(FirestorePaths.USERS).document(uid))
        batch.delete(db.collection(FirestorePaths.USERNAMES).document(usernameLower))
        batch.commit().await()
    }
}
