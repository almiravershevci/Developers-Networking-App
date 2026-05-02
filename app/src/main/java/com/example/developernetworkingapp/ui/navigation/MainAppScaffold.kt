package com.example.developernetworkingapp.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.developernetworkingapp.ui.data.MockUiData
import com.example.developernetworkingapp.ui.screens.DashboardRoute
import com.example.developernetworkingapp.ui.screens.ChatRoute
import com.example.developernetworkingapp.ui.screens.EventFeedRoute
import com.example.developernetworkingapp.ui.screens.NotificationRoute
import com.example.developernetworkingapp.ui.screens.ProfileRoute
import com.example.developernetworkingapp.ui.screens.ProjectBoardRoute
import com.example.developernetworkingapp.ui.screens.AdminDashboardRoute
import com.example.developernetworkingapp.ui.screens.SearchRoute
import com.example.developernetworkingapp.ui.screens.SettingsRoute
import com.example.developernetworkingapp.ui.screens.TaskManagementRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(navController: NavController) {
    val tabs = MockUiData.bottomTabs
    val currentRoute by navController.currentBackStackEntryAsState()
    val route = currentRoute?.destination?.route ?: AppRoutes.DASHBOARD
    val normalizedRoute = normalizeRoute(route)
    val selectedProjectName = currentRoute?.arguments?.getString("project").orEmpty()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(screenTitle(normalizedRoute)) },
                actions = {
                    IconButton(onClick = { navController.navigate(AppRoutes.NOTIFICATIONS) }) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { navController.navigate(AppRoutes.PROFILE) }) {
                        Icon(
                            imageVector = Icons.Outlined.PersonOutline,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (normalizedRoute != AppRoutes.ADMIN_DASHBOARD) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = normalizedRoute == tab.route,
                            onClick = {
                                if (normalizedRoute != tab.route) navController.navigate(tab.route)
                            },
                            icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        when (normalizedRoute) {
            AppRoutes.DASHBOARD -> DashboardRoute(padding, navController)
            AppRoutes.PROJECTS -> ProjectBoardRoute(padding, navController, selectedProjectName)
            AppRoutes.CHAT -> ChatRoute(padding, navController)
            AppRoutes.SEARCH -> SearchRoute(padding, navController)
            AppRoutes.NOTIFICATIONS -> NotificationRoute(padding, navController)
            AppRoutes.PROFILE -> ProfileRoute(padding, navController)
            AppRoutes.SETTINGS -> SettingsRoute(padding, navController)
            AppRoutes.ADMIN_DASHBOARD -> AdminDashboardRoute(padding, navController)
            AppRoutes.TASKS -> TaskManagementRoute(padding, navController)
            AppRoutes.EVENTS -> EventFeedRoute(padding, navController)
        }
    }
}

private fun normalizeRoute(route: String): String {
    return when {
        route.startsWith(AppRoutes.PROJECTS) -> AppRoutes.PROJECTS
        route.startsWith(AppRoutes.DASHBOARD) -> AppRoutes.DASHBOARD
        route.startsWith(AppRoutes.CHAT) -> AppRoutes.CHAT
        route.startsWith(AppRoutes.SEARCH) -> AppRoutes.SEARCH
        route.startsWith(AppRoutes.NOTIFICATIONS) -> AppRoutes.NOTIFICATIONS
        route.startsWith(AppRoutes.PROFILE) -> AppRoutes.PROFILE
        route.startsWith(AppRoutes.SETTINGS) -> AppRoutes.SETTINGS
        route.startsWith(AppRoutes.ADMIN_DASHBOARD) -> AppRoutes.ADMIN_DASHBOARD
        route.startsWith(AppRoutes.TASKS) -> AppRoutes.TASKS
        route.startsWith(AppRoutes.EVENTS) -> AppRoutes.EVENTS
        else -> route
    }
}

private fun screenTitle(route: String): String {
    return when (route) {
        AppRoutes.DASHBOARD -> "Developer Command Center"
        AppRoutes.PROJECTS -> "Project Collaboration"
        AppRoutes.CHAT -> "Realtime Chat"
        AppRoutes.SEARCH -> "Talent Search"
        AppRoutes.NOTIFICATIONS -> "Notifications"
        AppRoutes.PROFILE -> "Profile"
        AppRoutes.SETTINGS -> "Settings"
        AppRoutes.ADMIN_DASHBOARD -> "Admin Dashboard"
        AppRoutes.TASKS -> "Task Manager"
        AppRoutes.EVENTS -> "Hackathons & Events"
        else -> "Developer App"
    }
}
