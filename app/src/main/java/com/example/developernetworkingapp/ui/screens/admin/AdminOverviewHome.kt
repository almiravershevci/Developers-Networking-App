package com.example.developernetworkingapp.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.developernetworkingapp.domain.model.AdminDashboardSnapshot
import com.example.developernetworkingapp.domain.model.AdminProjectStatus
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.AdminViewModel

@Composable
fun AdminOverviewHome(
    adminNav: NavController,
    adminName: String,
    viewModel: AdminViewModel
) {
    val dash by viewModel.dashboard.collectAsStateWithLifecycle()
    val pendingApprovals = dash.pendingApprovalsCount()
    val engagementLabel = if (dash.activityTrendUp) "68%" else "55%"
    val engagementHint = if (dash.activityTrendUp) "↑ vs last week" else "↓ vs last week"

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item {
            Text(
                if (adminName.isNotBlank()) "Welcome, $adminName" else "Admin overview",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Monitor the platform at a glance. Tap a module to manage users, projects, content, and more.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        item { SectionTitle("Key metrics") }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminMetricTile("Total users", "${dash.users.size}", Modifier.weight(1f))
                AdminMetricTile("Active projects", "${dash.projects.count { it.status == AdminProjectStatus.ACTIVE }}", Modifier.weight(1f))
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminMetricTile("Pending approvals", "$pendingApprovals", Modifier.weight(1f))
                AdminMetricTile("Engagement", engagementLabel, Modifier.weight(1f), supporting = engagementHint)
            }
        }
        item { SectionTitle("Quick stats") }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("• New users (7d): +${dash.quickStatNewUsers7d}", style = MaterialTheme.typography.bodyMedium)
                    Text("• New projects (7d): +${dash.quickStatNewProjects7d}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "• Activity index: ${if (dash.activityTrendUp) "trending up" else "cooling down"} vs last week",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        item { SectionTitle("Activity feed") }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(AppDesignTokens.cardInnerPadding)) {
                    dash.auditLog.take(6).forEach { entry ->
                        AdminActivityRow(entry.timeLabel, entry.message)
                    }
                }
            }
        }
        item { SectionTitle("Management modules") }
        item {
            AdminNavTile(
                icon = Icons.Outlined.People,
                title = "User management",
                subtitle = "Directory, profiles, status, bans, activity logs",
                onClick = { adminNav.navigate(AdminNavRoutes.USERS) }
            )
        }
        item {
            AdminNavTile(
                icon = Icons.Outlined.Folder,
                title = "Project management",
                subtitle = "Approvals, edits, archive & delete",
                onClick = { adminNav.navigate(AdminNavRoutes.PROJECTS) }
            )
        }
        item {
            AdminNavTile(
                icon = Icons.AutoMirrored.Outlined.Article,
                title = "Content moderation",
                subtitle = "Queue, approvals, reports, takedowns",
                onClick = { adminNav.navigate(AdminNavRoutes.CONTENT) }
            )
        }
        item {
            AdminNavTile(
                icon = Icons.Outlined.Campaign,
                title = "Notifications & messaging",
                subtitle = "Message center, composer, push scheduling",
                onClick = { adminNav.navigate(AdminNavRoutes.MESSAGING) }
            )
        }
        item {
            AdminNavTile(
                icon = Icons.Outlined.Analytics,
                title = "Reports & analytics",
                subtitle = "Users, projects, messaging, exports",
                onClick = { adminNav.navigate(AdminNavRoutes.ANALYTICS) }
            )
        }
        item {
            AdminNavTile(
                icon = Icons.Outlined.Settings,
                title = "Platform settings",
                subtitle = "Policies, API keys, privacy, branding",
                onClick = { adminNav.navigate(AdminNavRoutes.PLATFORM_SETTINGS) }
            )
        }
        item {
            AdminNavTile(
                icon = Icons.Outlined.SupportAgent,
                title = "Feedback & help desk",
                subtitle = "Tickets, feedback, FAQs",
                onClick = { adminNav.navigate(AdminNavRoutes.SUPPORT) }
            )
        }
        item {
            AdminNavTile(
                icon = Icons.Outlined.AdminPanelSettings,
                title = "Admin access & audit",
                subtitle = "Admins, RBAC, audit logs",
                onClick = { adminNav.navigate(AdminNavRoutes.ACCESS) }
            )
        }
    }
}

private fun AdminDashboardSnapshot.pendingApprovalsCount(): Int {
    val projectPending = projects.count { it.status == AdminProjectStatus.PENDING_APPROVAL }
    val contentPending = contentQueue.size
    val reportsOpen = reports.count { it.active }
    return projectPending + contentPending + reportsOpen
}

@Composable
private fun AdminMetricTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    supporting: String? = null
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelLarge, maxLines = 2)
            if (supporting != null) {
                Text(supporting, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun AdminNavTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
