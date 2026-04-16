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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.LoginUiState
import com.example.developernetworkingapp.ui.state.SignupUiState
import com.example.developernetworkingapp.ui.viewmodel.LoginViewModel
import com.example.developernetworkingapp.ui.viewmodel.SignupViewModel

@Composable
fun AdvancedLoginScreen(navController: NavController) {
    val viewModel: LoginViewModel = viewModel()
    val signupViewModel: SignupViewModel = viewModel()
    val loginState = viewModel.uiState.collectAsStateWithLifecycle().value
    val signupState = signupViewModel.uiState.collectAsStateWithLifecycle().value

    UnifiedAuthScreen(
        navController = navController,
        loginState = loginState,
        signupState = signupState,
        startInSignup = false,
        onLoginEmailChange = viewModel::updateEmail,
        onLoginPasswordChange = viewModel::updatePassword,
        onSignupNameChange = signupViewModel::updateName,
        onSignupUsernameChange = signupViewModel::updateUsername,
        onSignupEmailChange = signupViewModel::updateEmail,
        onSignupPasswordChange = signupViewModel::updatePassword
    )
}

@Composable
fun AdvancedSignupScreen(navController: NavController) {
    AdvancedLoginScreen(navController)
}

@Composable
private fun UnifiedAuthScreen(
    navController: NavController,
    loginState: LoginUiState,
    signupState: SignupUiState,
    startInSignup: Boolean,
    onLoginEmailChange: (String) -> Unit,
    onLoginPasswordChange: (String) -> Unit,
    onSignupNameChange: (String) -> Unit,
    onSignupUsernameChange: (String) -> Unit,
    onSignupEmailChange: (String) -> Unit,
    onSignupPasswordChange: (String) -> Unit
) {
    var showSignup by rememberSaveable { mutableStateOf(startInSignup) }
    Column(
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
            .padding(20.dp)
    ) {
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
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
                    OutlinedTextField(value = signupState.form.name, onValueChange = onSignupNameChange, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = signupState.form.username, onValueChange = onSignupUsernameChange, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = signupState.form.email, onValueChange = onSignupEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = signupState.form.password, onValueChange = onSignupPasswordChange, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                } else {
                    OutlinedTextField(value = loginState.form.email, onValueChange = onLoginEmailChange, label = { Text("Email or username") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = loginState.form.password, onValueChange = onLoginPasswordChange, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Forgot password?", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { })
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(AppRoutes.DASHBOARD) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (showSignup) "Create account" else "Login")
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
