package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.developernetworkingapp.di.appViewModel
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.event.SettingsNavEvent
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.SettingsUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsRoute(padding: PaddingValues, navController: NavController) {
    val viewModel: SettingsViewModel = appViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel, navController) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                SettingsNavEvent.AccountDeleted ->
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
            }
        }
    }

    LaunchedEffect(context) {
        val versionName = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        }.getOrDefault("")
        viewModel.setAppVersion(versionName)
    }

    SettingsScreen(
        padding = padding,
        navController = navController,
        state = state,
        onPushEnabledChange = viewModel::setPushEnabled,
        onEmailDigestsChange = viewModel::setEmailDigests,
        onProfilePublicChange = viewModel::setProfilePublic,
        onAnalyticsOptInChange = viewModel::setAnalyticsOptIn,
        onDeleteAccount = viewModel::deleteAccount,
        onDismissDeleteAccountDialog = viewModel::clearDeleteAccountFeedback,
    )
}

@Composable
fun SettingsScreen(
    padding: PaddingValues,
    navController: NavController,
    state: SettingsUiState,
    onPushEnabledChange: (Boolean) -> Unit,
    onEmailDigestsChange: (Boolean) -> Unit,
    onProfilePublicChange: (Boolean) -> Unit,
    onAnalyticsOptInChange: (Boolean) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onDismissDeleteAccountDialog: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isDeletingAccount) {
                    showDeleteDialog = false
                    deletePassword = ""
                    onDismissDeleteAccountDialog()
                }
            },
            title = { Text("Delete account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "This permanently removes your profile, username, and notifications. " +
                            "Projects, messages, and other shared data may remain.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text("Password (email accounts)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text("Required for email/password accounts. Google-only users can leave blank.")
                        },
                    )
                    state.deleteAccountError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onDeleteAccount(deletePassword) },
                    enabled = !state.isDeletingAccount,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(if (state.isDeletingAccount) "Deleting..." else "Delete permanently")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!state.isDeletingAccount) {
                            showDeleteDialog = false
                            deletePassword = ""
                            onDismissDeleteAccountDialog()
                        }
                    },
                    enabled = !state.isDeletingAccount,
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            )
            .padding(padding)
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding,
    ) {
        item { SectionTitle("Notifications") }
        item {
            ListItem(
                headlineContent = { Text("Push notifications") },
                supportingContent = { Text("Alerts for mentions, invites, and messages") },
                trailingContent = {
                    Switch(
                        checked = state.pushEnabled,
                        onCheckedChange = onPushEnabledChange,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Email digests") },
                supportingContent = { Text("Weekly summary of activity") },
                trailingContent = {
                    Switch(
                        checked = state.emailDigests,
                        onCheckedChange = onEmailDigestsChange,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item { SectionTitle("Privacy & profile") }
        item {
            ListItem(
                headlineContent = { Text("Public profile") },
                supportingContent = { Text("Show your profile to other developers") },
                trailingContent = {
                    Switch(
                        checked = state.profilePublic,
                        onCheckedChange = onProfilePublicChange,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Product analytics") },
                supportingContent = { Text("Help improve the app with anonymous usage data") },
                trailingContent = {
                    Switch(
                        checked = state.analyticsOptIn,
                        onCheckedChange = onAnalyticsOptInChange,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item { SectionTitle("Administration") }
        item {
            ListItem(
                headlineContent = { Text("Admin console") },
                supportingContent = {
                    Text("Manage users, projects, events, and platform settings (admin role required)")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(AppRoutes.ADMIN_DASHBOARD) },
            )
        }
        item { SectionTitle("Account") }
        item {
            ListItem(
                headlineContent = { Text("Delete account") },
                supportingContent = { Text("Permanently remove your account and profile data") },
                trailingContent = {
                    Button(
                        onClick = {
                            deletePassword = ""
                            onDismissDeleteAccountDialog()
                            showDeleteDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    ) {
                        Text("Delete")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item { SectionTitle("About") }
        item {
            ListItem(
                headlineContent = { Text("App version") },
                supportingContent = { Text(state.appVersion.ifBlank { "1.0" }) },
                leadingContent = {
                    Icon(Icons.Outlined.Info, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
