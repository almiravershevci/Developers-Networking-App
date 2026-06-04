package com.example.developernetworkingapp.ui.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Task
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.developernetworkingapp.ui.navigation.AppRoutes

data class NavTab(
    val label: String,
    val route: String,
    val icon: ImageVector
)

data class ShortcutItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

object AppShellData {
    val bottomTabs = listOf(
        NavTab("Home", AppRoutes.DASHBOARD, Icons.Outlined.Dashboard),
        NavTab("Projects", AppRoutes.PROJECTS, Icons.Outlined.Task),
        NavTab("Chat", AppRoutes.CHAT, Icons.Outlined.ChatBubbleOutline),
        NavTab("Search", AppRoutes.SEARCH, Icons.Outlined.Search),
        NavTab("Alerts", AppRoutes.NOTIFICATIONS, Icons.Outlined.NotificationsNone)
    )
}
