package com.example.developernetworkingapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardNavHost(
    outerPadding: PaddingValues,
    outerNavController: NavController,
    adminName: String,
    viewModel: AdminViewModel
) {
    val adminNav = rememberNavController()
    val backStackEntry by adminNav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AdminNavRoutes.HOME
    val title = adminScreenTitle(currentRoute)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier
            .padding(outerPadding)
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (currentRoute != AdminNavRoutes.HOME) {
                        IconButton(onClick = { adminNav.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    TextButton(onClick = { outerNavController.navigate(AppRoutes.PROFILE) }) {
                        Text("Profile")
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = adminNav,
            startDestination = AdminNavRoutes.HOME,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            composable(AdminNavRoutes.HOME) {
                AdminOverviewHome(
                    adminNav = adminNav,
                    adminName = adminName,
                    viewModel = viewModel
                )
            }
            composable(AdminNavRoutes.USERS) { AdminUsersSection(viewModel) }
            composable(AdminNavRoutes.PROJECTS) { AdminProjectsSection(viewModel) }
            composable(AdminNavRoutes.CONTENT) { AdminContentSection(viewModel) }
            composable(AdminNavRoutes.MESSAGING) { AdminMessagingSection(viewModel) }
            composable(AdminNavRoutes.ANALYTICS) { AdminAnalyticsSection(viewModel) }
            composable(AdminNavRoutes.PLATFORM_SETTINGS) { AdminPlatformSettingsSection(viewModel) }
            composable(AdminNavRoutes.SUPPORT) { AdminSupportSection(viewModel) }
            composable(AdminNavRoutes.ACCESS) { AdminAccessSection(viewModel) }
        }
    }
}

private fun adminScreenTitle(route: String): String = when (route) {
    AdminNavRoutes.HOME -> "Overview"
    AdminNavRoutes.USERS -> "Users"
    AdminNavRoutes.PROJECTS -> "Projects"
    AdminNavRoutes.CONTENT -> "Content"
    AdminNavRoutes.MESSAGING -> "Messaging"
    AdminNavRoutes.ANALYTICS -> "Analytics"
    AdminNavRoutes.PLATFORM_SETTINGS -> "Settings"
    AdminNavRoutes.SUPPORT -> "Support"
    AdminNavRoutes.ACCESS -> "Access"
    else -> "Admin"
}
