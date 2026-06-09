package com.example.developernetworkingapp.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.developernetworkingapp.ui.auth.AuthTestTags
import com.example.developernetworkingapp.ui.state.LoginUiState
import com.example.developernetworkingapp.ui.state.SignupUiState
import com.example.developernetworkingapp.ui.theme.DeveloperNetworkingAppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loginForm_displaysCoreFieldsAndActions() {
        composeRule.setContent {
            DeveloperNetworkingAppTheme {
                AuthFormScreen(
                    loginState = LoginUiState(),
                    signupState = SignupUiState(),
                    startInSignup = false,
                    onLoginEmailChange = {},
                    onLoginPasswordChange = {},
                    onSignupNameChange = {},
                    onSignupUsernameChange = {},
                    onSignupEmailChange = {},
                    onSignupPasswordChange = {},
                    onSignupConfirmPasswordChange = {},
                    onLoginRememberMeChange = {},
                    onSignupRememberMeChange = {},
                    onForgotPassword = {},
                    onLoginSubmit = {},
                    onSignupSubmit = {},
                    onGoogleSignIn = {},
                    onGitHubSignIn = {},
                    googleSignInAvailable = false,
                )
            }
        }

        composeRule.onNodeWithTag(AuthTestTags.LOGIN_EMAIL_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(AuthTestTags.LOGIN_PASSWORD_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(AuthTestTags.LOGIN_SUBMIT_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(AuthTestTags.FORGOT_PASSWORD_LINK).assertIsDisplayed()
        composeRule.onNodeWithTag(AuthTestTags.GITHUB_SIGN_IN_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithText("Welcome back").assertIsDisplayed()
    }

    @Test
    fun loginForm_acceptsCredentialsAndSubmits() {
        var submitted = false

        composeRule.setContent {
            DeveloperNetworkingAppTheme {
                AuthFormScreen(
                    loginState = LoginUiState(),
                    signupState = SignupUiState(),
                    startInSignup = false,
                    onLoginEmailChange = {},
                    onLoginPasswordChange = {},
                    onSignupNameChange = {},
                    onSignupUsernameChange = {},
                    onSignupEmailChange = {},
                    onSignupPasswordChange = {},
                    onSignupConfirmPasswordChange = {},
                    onLoginRememberMeChange = {},
                    onSignupRememberMeChange = {},
                    onForgotPassword = {},
                    onLoginSubmit = { submitted = true },
                    onSignupSubmit = {},
                    onGoogleSignIn = {},
                    onGitHubSignIn = {},
                    googleSignInAvailable = false,
                )
            }
        }

        composeRule.onNodeWithTag(AuthTestTags.LOGIN_EMAIL_FIELD).performTextInput("jane")
        composeRule.onNodeWithTag(AuthTestTags.LOGIN_PASSWORD_FIELD).performTextInput("secret123")
        composeRule.onNodeWithTag(AuthTestTags.LOGIN_SUBMIT_BUTTON).performClick()

        assertTrue(submitted)
    }

    @Test
    fun authForm_switchesToSignupMode() {
        composeRule.setContent {
            DeveloperNetworkingAppTheme {
                AuthFormScreen(
                    loginState = LoginUiState(),
                    signupState = SignupUiState(),
                    startInSignup = false,
                    onLoginEmailChange = {},
                    onLoginPasswordChange = {},
                    onSignupNameChange = {},
                    onSignupUsernameChange = {},
                    onSignupEmailChange = {},
                    onSignupPasswordChange = {},
                    onSignupConfirmPasswordChange = {},
                    onLoginRememberMeChange = {},
                    onSignupRememberMeChange = {},
                    onForgotPassword = {},
                    onLoginSubmit = {},
                    onSignupSubmit = {},
                    onGoogleSignIn = {},
                    onGitHubSignIn = {},
                    googleSignInAvailable = false,
                )
            }
        }

        composeRule.onNodeWithTag(AuthTestTags.SIGNUP_MODE_CHIP).performClick()
        composeRule.onNodeWithText("Create your account").assertIsDisplayed()
        composeRule.onNodeWithTag(AuthTestTags.SIGNUP_SUBMIT_BUTTON).assertIsDisplayed()
    }
}
