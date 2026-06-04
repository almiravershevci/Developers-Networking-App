package com.example.developernetworkingapp.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.developernetworkingapp.di.appViewModel
import com.example.developernetworkingapp.ui.screens.AdminDashboardRoute
import com.example.developernetworkingapp.ui.screens.ChatRoute
import com.example.developernetworkingapp.ui.screens.DashboardRoute
import com.example.developernetworkingapp.ui.screens.EventFeedRoute
import com.example.developernetworkingapp.ui.screens.NotificationRoute
import com.example.developernetworkingapp.ui.screens.ProfileRoute
import com.example.developernetworkingapp.ui.screens.ProjectBoardRoute
import com.example.developernetworkingapp.ui.screens.SearchRoute
import com.example.developernetworkingapp.ui.screens.SettingsRoute
import com.example.developernetworkingapp.ui.screens.TaskManagementRoute
import com.example.developernetworkingapp.ui.viewmodel.MainShellViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(navController: NavController) {
    val shellViewModel: MainShellViewModel = appViewModel()
    val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val currentRoute by navController.currentBackStackEntryAsState()
    val route = currentRoute?.destination?.route ?: AppRoutes.DASHBOARD
    val selectedProjectName = currentRoute?.arguments?.getString("project").orEmpty()

    LaunchedEffect(route) {
        shellViewModel.onRouteChanged(route)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(shellState.screenTitle) },
                actions = {
                    IconButton(onClick = { navController.navigate(AppRoutes.NOTIFICATIONS) }) {
                        NotificationNavIcon(
                            unreadCount = shellState.unreadNotificationCount,
                            contentDescription = "Notifications",
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { navController.navigate(AppRoutes.PROFILE) }) {
                        Icon(
                            imageVector = Icons.Outlined.PersonOutline,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (shellState.showBottomBar) {
                NavigationBar {
                    shellViewModel.bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = shellState.currentRoute == tab.route,
                            onClick = {
                                if (shellState.currentRoute != tab.route) {
                                    navController.navigate(tab.route)
                                }
                            },
                            icon = {
                                if (tab.route == AppRoutes.NOTIFICATIONS) {
                                    NotificationNavIcon(
                                        unreadCount = shellState.unreadNotificationCount,
                                        contentDescription = tab.label,
                                        icon = tab.icon,
                                    )
                                } else {
                                    Icon(imageVector = tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        when (shellState.currentRoute) {
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
            else -> DashboardRoute(padding, navController)
        }
    }
}

@Composable
private fun NotificationNavIcon(
    unreadCount: Int,
    contentDescription: String,
    icon: ImageVector = Icons.Outlined.NotificationsNone,
) {
    val tint = MaterialTheme.colorScheme.primary
    if (unreadCount > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text(
                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                    )
                }
            },
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
            )
        }
    } else {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}
