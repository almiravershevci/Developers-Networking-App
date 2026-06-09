package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.datasource.firebase.FirestoreChatDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ChatRepositoryFirestoreTest {

    private val chatDataSource: FirestoreChatDataSource = mock()
    private val userDataSource: FirestoreUserDataSource = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseUser: FirebaseUser = mock()

    private lateinit var repository: ChatRepositoryFirestore

    @Before
    fun setUp() {
        repository = ChatRepositoryFirestore(
            chatDataSource = chatDataSource,
            userDataSource = userDataSource,
            firebaseAuth = firebaseAuth,
        )
    }

    @Test
    fun sendMessage_returnsFailureWhenSignedOut() = runTest {
        whenever(firebaseAuth.currentUser).thenReturn(null)

        val result = repository.sendMessage("conv_1", "Hello team")

        assertTrue(result.isFailure)
        assertEquals("Sign in required", result.exceptionOrNull()?.message)
        verify(chatDataSource, never()).sendTextMessage(
            conversationId = "conv_1",
            senderId = "any",
            body = "Hello team",
        )
    }

    @Test
    fun sendMessage_returnsFailureWhenEmailNotVerified() = runTest {
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("user_1")
        whenever(firebaseUser.isEmailVerified).thenReturn(false)

        val result = repository.sendMessage("conv_1", "Hello team")

        assertTrue(result.isFailure)
        assertEquals("Verify your email to send messages", result.exceptionOrNull()?.message)
    }

    @Test
    fun sendMessage_rejectsBlankMessage() = runTest {
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("user_1")
        whenever(firebaseUser.isEmailVerified).thenReturn(true)

        val result = repository.sendMessage("conv_1", "   ")

        assertTrue(result.isFailure)
        assertEquals("Message cannot be empty", result.exceptionOrNull()?.message)
        verify(chatDataSource, never()).sendTextMessage(
            conversationId = "conv_1",
            senderId = "user_1",
            body = "   ",
        )
    }

    @Test
    fun sendMessage_callsDataSourceWhenValid() = runTest {
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("user_1")
        whenever(firebaseUser.isEmailVerified).thenReturn(true)

        val result = repository.sendMessage("conv_1", "  Ship it  ")

        assertTrue(result.isSuccess)
        verify(chatDataSource).sendTextMessage(
            conversationId = "conv_1",
            senderId = "user_1",
            body = "  Ship it  ",
        )
    }
}
