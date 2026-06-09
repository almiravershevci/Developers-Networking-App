package com.example.developernetworkingapp.ui.viewmodel

import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.AuthResult
import com.example.developernetworkingapp.data.repository.AuthUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository: AuthRepository = mock()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(authRepository.currentUser).thenReturn(MutableStateFlow(null))
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_showsValidationErrorWhenFieldsBlank() = runTest {
        viewModel.login()
        advanceUntilIdle()

        assertEquals("Email/username and password are required.", viewModel.uiState.value.errorMessage)
        verify(authRepository, never()).login(any(), any(), any())
    }

    @Test
    fun login_clearsErrorOnSuccess() = runTest {
        val user = AuthUser("Jane", "jane", "jane@example.com", "secret")
        whenever(authRepository.login("jane", "secret", true))
            .thenReturn(AuthResult.Success(user))

        viewModel.updateEmail("jane")
        viewModel.updatePassword("secret")
        viewModel.login()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun requestPasswordReset_delegatesToRepository() = runTest {
        whenever(authRepository.requestPasswordReset(""))
            .thenReturn(AuthResult.Error("Enter your email or username first."))

        viewModel.requestPasswordReset()
        advanceUntilIdle()

        verify(authRepository).requestPasswordReset("")
    }
}
