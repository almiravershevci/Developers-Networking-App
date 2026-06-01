package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.ConversationDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.example.developernetworkingapp.data.datasource.firebase.schema.MessageDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.MessageKind
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Chat microservice: conversations inbox and per-thread messages (Firestore).
 */
class FirestoreChatDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun observeConversationsForUser(userId: String): Flow<List<ConversationDoc>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = db.collection(FirestorePaths.CONVERSATIONS)
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ConversationDoc::class.java)?.copy(
                        id = doc.id,
                        lastMessageAt = doc.readTimestamp("lastMessageAt"),
                        createdAt = doc.readTimestamp("createdAt"),
                    )
                }.orEmpty()
                trySend(conversations)
            }
        awaitClose { registration?.remove() }
    }

    fun observeMessages(conversationId: String): Flow<List<MessageDoc>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = db.collection(FirestorePaths.CONVERSATIONS)
            .document(conversationId)
            .collection(FirestorePaths.MESSAGES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents
                    ?.sortedBy { messageSortKey(it) }
                    ?.mapNotNull { doc ->
                        doc.toObject(MessageDoc::class.java)?.copy(
                            id = doc.id,
                            createdAt = doc.readTimestamp("createdAt"),
                        ) ?: messageFromSnapshot(doc)
                    }.orEmpty()
                trySend(messages)
            }
        awaitClose { registration?.remove() }
    }

    suspend fun fetchMessagesOnce(conversationId: String): List<MessageDoc> {
        val snap = db.collection(FirestorePaths.CONVERSATIONS)
            .document(conversationId)
            .collection(FirestorePaths.MESSAGES)
            .get()
            .await()
        return snap.documents
            .sortedBy { messageSortKey(it) }
            .mapNotNull { doc ->
                doc.toObject(MessageDoc::class.java)?.copy(
                    id = doc.id,
                    createdAt = doc.readTimestamp("createdAt"),
                ) ?: messageFromSnapshot(doc)
            }
    }

    suspend fun fetchConversation(conversationId: String): ConversationDoc? {
        val snap = db.collection(FirestorePaths.CONVERSATIONS)
            .document(conversationId)
            .get()
            .await()
        if (!snap.exists()) return null
        return snap.toObject(ConversationDoc::class.java)?.copy(id = snap.id)
    }

    suspend fun sendTextMessage(
        conversationId: String,
        senderId: String,
        body: String,
    ) {
        val trimmed = body.trim()
        require(trimmed.isNotEmpty())

        val conversationRef = db.collection(FirestorePaths.CONVERSATIONS).document(conversationId)
        val messageRef = conversationRef.collection(FirestorePaths.MESSAGES).document()

        val messagePayload = mapOf(
            "schemaVersion" to 1,
            "senderId" to senderId,
            "body" to trimmed,
            "messageKind" to MessageKind.TEXT,
            "readByUserIds" to listOf(senderId),
            "createdAt" to FieldValue.serverTimestamp(),
        )
        val conversationPatch = mapOf(
            "lastMessagePreview" to trimmed.take(120),
            "lastMessageAt" to FieldValue.serverTimestamp(),
        )

        val batch = db.batch()
        batch.set(messageRef, messagePayload)
        batch.update(conversationRef, conversationPatch)
        batch.commit().await()
    }

    suspend fun markMessagesRead(
        conversationId: String,
        readerUserId: String,
        messages: List<MessageDoc>,
    ) {
        val unread = messages.filter { readerUserId !in it.readByUserIds }
        if (unread.isEmpty()) return

        val batch = db.batch()
        unread.forEach { message ->
            val ref = db.collection(FirestorePaths.CONVERSATIONS)
                .document(conversationId)
                .collection(FirestorePaths.MESSAGES)
                .document(message.id)
            batch.update(ref, "readByUserIds", FieldValue.arrayUnion(readerUserId))
        }
        runCatching { batch.commit().await() }
    }

    private fun messageFromSnapshot(doc: com.google.firebase.firestore.DocumentSnapshot): MessageDoc? {
        val body = doc.getString("body") ?: return null
        return MessageDoc(
            id = doc.id,
            senderId = doc.getString("senderId").orEmpty(),
            body = body,
            messageKind = doc.getString("messageKind") ?: MessageKind.TEXT,
            readByUserIds = (doc.get("readByUserIds") as? List<*>)?.filterIsInstance<String>().orEmpty(),
            createdAt = doc.readTimestamp("createdAt"),
        )
    }
}

fun messageSortKey(doc: com.google.firebase.firestore.DocumentSnapshot): Long =
    doc.readTimestamp("createdAt")?.toDate()?.time ?: 0L
