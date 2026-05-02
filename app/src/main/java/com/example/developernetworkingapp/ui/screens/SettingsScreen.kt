package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.theme.AppDesignTokens

@Composable
fun SettingsRoute(padding: PaddingValues, @Suppress("UNUSED_PARAMETER") navController: NavController) {
    SettingsScreen(padding = padding)
}

@Composable
fun SettingsScreen(padding: PaddingValues) {
    var pushEnabled by rememberSaveable { mutableStateOf(true) }
    var emailDigests by rememberSaveable { mutableStateOf(true) }
    var profilePublic by rememberSaveable { mutableStateOf(true) }
    var analyticsOptIn by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val versionName = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item { SectionTitle("Notifications") }
        item {
            ListItem(
                headlineContent = { Text("Push notifications") },
                supportingContent = { Text("Alerts for mentions, invites, and messages") },
                trailingContent = {
                    Switch(
                        checked = pushEnabled,
                        onCheckedChange = { pushEnabled = it }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Email digests") },
                supportingContent = { Text("Weekly summary of activity") },
                trailingContent = {
                    Switch(
                        checked = emailDigests,
                        onCheckedChange = { emailDigests = it }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { SectionTitle("Privacy & profile") }
        item {
            ListItem(
                headlineContent = { Text("Public profile") },
                supportingContent = { Text("Show your profile to other developers") },
                trailingContent = {
                    Switch(
                        checked = profilePublic,
                        onCheckedChange = { profilePublic = it }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Product analytics") },
                supportingContent = { Text("Help improve the app with anonymous usage data") },
                trailingContent = {
                    Switch(
                        checked = analyticsOptIn,
                        onCheckedChange = { analyticsOptIn = it }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { SectionTitle("About") }
        item {
            ListItem(
                headlineContent = { Text("App version") },
                supportingContent = { Text(versionName.ifBlank { "1.0" }) },
                leadingContent = {
                    Icon(Icons.Outlined.Info, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
