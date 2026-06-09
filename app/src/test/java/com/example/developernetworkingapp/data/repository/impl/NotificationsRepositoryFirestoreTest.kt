package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.datasource.firebase.FirestoreNotificationsDataSource
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

class NotificationsRepositoryFirestoreTest {

    private val dataSource: FirestoreNotificationsDataSource = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseUser: FirebaseUser = mock()

    private lateinit var repository: NotificationsRepositoryFirestore

    @Before
    fun setUp() {
        repository = NotificationsRepositoryFirestore(
            dataSource = dataSource,
            firebaseAuth = firebaseAuth,
        )
    }

    @Test
    fun markAsRead_returnsFailureWhenSignedOut() = runTest {
        whenever(firebaseAuth.currentUser).thenReturn(null)

        val result = repository.markAsRead("notif_1")

        assertTrue(result.isFailure)
        assertEquals("Sign in required", result.exceptionOrNull()?.message)
        verify(dataSource, never()).markNotificationRead("notif_1")
    }

    @Test
    fun markAsRead_callsDataSourceWhenSignedIn() = runTest {
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("user_1")

        val result = repository.markAsRead("notif_1")

        assertTrue(result.isSuccess)
        verify(dataSource).markNotificationRead("notif_1")
    }
}
