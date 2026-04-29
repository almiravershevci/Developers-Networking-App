package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.LoginUiState
import com.example.developernetworkingapp.ui.state.SignupUiState
import com.example.developernetworkingapp.ui.state.VerificationUiState
import com.example.developernetworkingapp.ui.viewmodel.LoginViewModel
import com.example.developernetworkingapp.ui.viewmodel.SignupViewModel
import com.example.developernetworkingapp.ui.viewmodel.VerificationViewModel

@Composable
fun AdvancedLoginScreen(navController: NavController) {
    val viewModel: LoginViewModel = viewModel()
    val signupViewModel: SignupViewModel = viewModel()
    val loginState = viewModel.uiState.collectAsStateWithLifecycle().value
    val signupState = signupViewModel.uiState.collectAsStateWithLifecycle().value

    UnifiedAuthScreen(
        loginState = loginState,
        signupState = signupState,
        startInSignup = false,
        onLoginEmailChange = viewModel::updateEmail,
        onLoginPasswordChange = viewModel::updatePassword,
        onSignupNameChange = signupViewModel::updateName,
        onSignupUsernameChange = signupViewModel::updateUsername,
        onSignupEmailChange = signupViewModel::updateEmail,
        onSignupPasswordChange = signupViewModel::updatePassword,
        onSignupConfirmPasswordChange = signupViewModel::updateConfirmPassword,
        onLoginRememberMeChange = viewModel::updateRememberMe,
        onSignupRememberMeChange = signupViewModel::updateRememberMe,
        onForgotPassword = viewModel::requestPasswordReset,
        onLoginSubmit = {
            viewModel.login {
                navController.navigate(AppRoutes.DASHBOARD) {
                    popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        },
        onSignupSubmit = {
            signupViewModel.signup { email ->
                navController.navigate(AppRoutes.verifyEmailRoute(email)) {
                    popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    )
}

@Composable
fun AdvancedSignupScreen(navController: NavController) {
    val loginViewModel: LoginViewModel = viewModel()
    val signupViewModel: SignupViewModel = viewModel()
    val loginState = loginViewModel.uiState.collectAsStateWithLifecycle().value
    val signupState = signupViewModel.uiState.collectAsStateWithLifecycle().value

    UnifiedAuthScreen(
        loginState = loginState,
        signupState = signupState,
        startInSignup = true,
        onLoginEmailChange = loginViewModel::updateEmail,
        onLoginPasswordChange = loginViewModel::updatePassword,
        onSignupNameChange = signupViewModel::updateName,
        onSignupUsernameChange = signupViewModel::updateUsername,
        onSignupEmailChange = signupViewModel::updateEmail,
        onSignupPasswordChange = signupViewModel::updatePassword,
        onSignupConfirmPasswordChange = signupViewModel::updateConfirmPassword,
        onLoginRememberMeChange = loginViewModel::updateRememberMe,
        onSignupRememberMeChange = signupViewModel::updateRememberMe,
        onForgotPassword = loginViewModel::requestPasswordReset,
        onLoginSubmit = {
            loginViewModel.login {
                navController.navigate(AppRoutes.DASHBOARD) {
                    popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        },
        onSignupSubmit = {
            signupViewModel.signup { email ->
                navController.navigate(AppRoutes.verifyEmailRoute(email)) {
                    popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    )
}

@Composable
fun EmailVerificationRoute(navController: NavController, email: String) {
    val viewModel: VerificationViewModel = viewModel()
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    androidx.compose.runtime.LaunchedEffect(email) {
        viewModel.setEmail(email)
        viewModel.resendCode()
    }
    EmailVerificationScreen(
        state = state,
        onCodeChange = viewModel::updateCode,
        onResend = viewModel::resendCode,
        onVerify = {
            viewModel.verify {
                navController.navigate(AppRoutes.LOGIN) {
                    popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    )
}

@Composable
private fun EmailVerificationScreen(
    state: VerificationUiState,
    onCodeChange: (String) -> Unit,
    onResend: () -> Unit,
    onVerify: () -> Unit
) {
    AuthCardContainer(horizontalPadding = 20.dp) {
        Text("Verify your email", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(6.dp))
        Text("We sent a verification code to ${state.email}.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.code,
            onValueChange = onCodeChange,
            label = { Text("6-digit code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Demo code: 123456") }
        )
        state.errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        state.infoMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            Text(if (state.isLoading) "Verifying..." else "Verify email")
        }
        TextButton(onClick = onResend, modifier = Modifier.align(Alignment.End)) {
            Text("Resend code")
        }
    }
}

@Composable
private fun UnifiedAuthScreen(
    loginState: LoginUiState,
    signupState: SignupUiState,
    startInSignup: Boolean,
    onLoginEmailChange: (String) -> Unit,
    onLoginPasswordChange: (String) -> Unit,
    onSignupNameChange: (String) -> Unit,
    onSignupUsernameChange: (String) -> Unit,
    onSignupEmailChange: (String) -> Unit,
    onSignupPasswordChange: (String) -> Unit,
    onSignupConfirmPasswordChange: (String) -> Unit,
    onLoginRememberMeChange: (Boolean) -> Unit,
    onSignupRememberMeChange: (Boolean) -> Unit,
    onForgotPassword: () -> Unit,
    onLoginSubmit: () -> Unit,
    onSignupSubmit: () -> Unit
) {
    AuthCardContainer(horizontalPadding = 20.dp) {
        var showSignup by rememberSaveable { mutableStateOf(startInSignup) }
        var showLoginPassword by rememberSaveable { mutableStateOf(false) }
        var showSignupPassword by rememberSaveable { mutableStateOf(false) }
        var showSignupConfirmPassword by rememberSaveable { mutableStateOf(false) }
        Text(
            text = if (showSignup) "Create your account" else "Welcome back",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (showSignup) "Set up your profile and start joining projects." else "Log in to your developer network.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AuthModeChip("Login", !showSignup) { showSignup = false }
            AuthModeChip("Sign up", showSignup) { showSignup = true }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (showSignup) {
            OutlinedTextField(value = signupState.form.name, onValueChange = onSignupNameChange, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = signupState.form.username, onValueChange = onSignupUsernameChange, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = signupState.form.email, onValueChange = onSignupEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = signupState.form.password,
                onValueChange = onSignupPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showSignupPassword) VisualTransformation.None else PasswordVisualTransformation(),
                supportingText = { Text("8+ chars, include upper/lowercase, number, symbol") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = signupState.form.confirmPassword,
                onValueChange = onSignupConfirmPasswordChange,
                label = { Text("Confirm password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showSignupConfirmPassword) VisualTransformation.None else PasswordVisualTransformation()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { showSignupPassword = !showSignupPassword }) {
                    Text(if (showSignupPassword) "Hide password" else "Show password")
                }
                TextButton(onClick = { showSignupConfirmPassword = !showSignupConfirmPassword }) {
                    Text(if (showSignupConfirmPassword) "Hide confirm" else "Show confirm")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = signupState.rememberMe, onCheckedChange = onSignupRememberMeChange)
                Text("Remember me on this device", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            OutlinedTextField(value = loginState.form.email, onValueChange = onLoginEmailChange, label = { Text("Email or username") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = loginState.form.password,
                onValueChange = onLoginPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Forgot password?", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onForgotPassword() })
                TextButton(onClick = { showLoginPassword = !showLoginPassword }) {
                    Text(if (showLoginPassword) "Hide password" else "Show password")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = loginState.rememberMe, onCheckedChange = onLoginRememberMeChange)
                Text("Remember me on this device", style = MaterialTheme.typography.bodyMedium)
            }
        }
        val errorMessage = if (showSignup) signupState.errorMessage else loginState.errorMessage
        val successMessage = if (showSignup) signupState.successMessage else null
        val infoMessage = if (showSignup) null else loginState.infoMessage
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        if (successMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(successMessage, color = Color(0xFF1B8A3A), style = MaterialTheme.typography.bodyMedium)
        }
        if (infoMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(infoMessage, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (showSignup) onSignupSubmit() else onLoginSubmit() },
            modifier = Modifier.fillMaxWidth(),
            enabled = if (showSignup) !signupState.isLoading else !loginState.isLoading
        ) {
            val loading = if (showSignup) signupState.isLoading else loginState.isLoading
            Text(
                if (loading) {
                    if (showSignup) "Creating account..." else "Logging in..."
                } else {
                    if (showSignup) "Create account" else "Login"
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { }, modifier = Modifier.weight(1f)) { Text("Google") }
            OutlinedButton(onClick = { }, modifier = Modifier.weight(1f)) { Text("GitHub") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (showSignup) "Already have an account?"
                else "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (showSignup) "Login" else "Sign up",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { showSignup = !showSignup }
            )
        }
    }
}

@Composable
private fun AuthCardContainer(horizontalPadding: Dp, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = horizontalPadding, vertical = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(20.dp)
            ) {
                Column {
                    Text("DevConnect", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Post your project, grow your team, join real builds, and collaborate with developers who match your stack.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                Column(modifier = Modifier.padding(18.dp), content = content)
            }
        }
    }
}

@Composable
private fun AuthModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(label, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}
