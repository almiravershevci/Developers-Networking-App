package com.example.developernetworkingapp.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.ClipData
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.developernetworkingapp.domain.model.AdminPermissionPreset
import com.example.developernetworkingapp.domain.model.AdminProjectRow
import com.example.developernetworkingapp.domain.model.AdminProjectStatus
import com.example.developernetworkingapp.domain.model.AdminUserRow
import com.example.developernetworkingapp.domain.model.AdminUserStatus
import com.example.developernetworkingapp.domain.model.TicketStatus
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.AdminViewModel

@Composable
fun AdminUsersSection(viewModel: AdminViewModel) {
    val dash by viewModel.dashboard.collectAsStateWithLifecycle()
    var editingUser by remember { mutableStateOf<AdminUserRow?>(null) }
    var banTarget by remember { mutableStateOf<AdminUserRow?>(null) }
    var activityFor by remember { mutableStateOf<AdminUserRow?>(null) }

    editingUser?.let { user ->
        var stack by remember(user.id) { mutableStateOf(user.techStack) }
        var bio by remember(user.id) { mutableStateOf(user.bio) }
        AlertDialog(
            onDismissRequest = { editingUser = null },
            title = { Text("Edit ${user.username}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(stack, onValueChange = { stack = it }, label = { Text("Tech stack") }, modifier = Modifier.fillMaxWidth())
                    TextField(bio, onValueChange = { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateUserProfile(user.id, stack, bio)
                    editingUser = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingUser = null }) { Text("Cancel") } }
        )
    }

    activityFor?.let { user ->
        val related = dash.auditLog.filter {
            it.message.contains(user.id, ignoreCase = true) ||
                it.message.contains(user.username, ignoreCase = true)
        }
        AlertDialog(
            onDismissRequest = { activityFor = null },
            title = { Text("Activity — ${user.username}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (related.isEmpty()) {
                        Text("No audit entries reference this user yet.")
                    } else {
                        related.take(12).forEach { entry ->
                            Text("• ${entry.timeLabel} — ${entry.message}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { activityFor = null }) { Text("Close") } }
        )
    }

    banTarget?.let { user ->
        AlertDialog(
            onDismissRequest = { banTarget = null },
            title = { Text("Ban ${user.username}?") },
            text = { Text("They will lose access immediately. You can reverse via support processes.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.banUser(user.id)
                    banTarget = null
                }) { Text("Ban") }
            },
            dismissButton = { TextButton(onClick = { banTarget = null }) { Text("Cancel") } }
        )
    }

    val copyToClipboard = rememberCopyToClipboardHandler()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item {
            AdminSectionIntro(
                title = "User management",
                body = "Status changes and profile edits update in-memory state and the audit log. Connect your admin API to persist."
            )
        }
        item { SectionTitle("User list") }
        items(dash.users, key = { it.id }) { user ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.username, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                            Text(user.email, style = MaterialTheme.typography.bodySmall)
                            Text(user.techStack, style = MaterialTheme.typography.bodySmall)
                            Text(user.bio, style = MaterialTheme.typography.bodySmall)
                            Text("Last login: ${user.lastLoginLabel}", style = MaterialTheme.typography.labelSmall)
                        }
                        AdminStatusChip(user.status.toDisplayString())
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { editingUser = user }) { Text("Profile") }
                        TextButton(onClick = { activityFor = user }) { Text("Activity log") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.deactivateUser(user.id) },
                            enabled = user.status == AdminUserStatus.ACTIVE
                        ) { Text("Deactivate") }
                        OutlinedButton(
                            onClick = { banTarget = user },
                            enabled = user.status != AdminUserStatus.BANNED
                        ) { Text("Ban") }
                        Button(
                            onClick = { viewModel.activateUser(user.id) },
                            enabled = user.status != AdminUserStatus.ACTIVE
                        ) { Text("Activate") }
                    }
                }
            }
        }
        item {
            OutlinedButton(
                onClick = {
                    val csv = viewModel.bulkExportUsersCsv()
                    copyToClipboard(csv)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Bulk export users (CSV)") }
        }
    }
}

@Composable
fun AdminProjectsSection(viewModel: AdminViewModel) {
    val dash by viewModel.dashboard.collectAsStateWithLifecycle()
    var editingProject by remember { mutableStateOf<AdminProjectRow?>(null) }

    editingProject?.let { project ->
        var name by remember(project.id) { mutableStateOf(project.name) }
        var tech by remember(project.id) { mutableStateOf(project.techSummary) }
        AlertDialog(
            onDismissRequest = { editingProject = null },
            title = { Text("Edit project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    TextField(tech, onValueChange = { tech = it }, label = { Text("Tech stack") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateProject(project.id, name, tech)
                    editingProject = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingProject = null }) { Text("Cancel") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item {
            AdminSectionIntro(
                title = "Project management",
                body = "Approvals change project state; archives hide projects from active listings."
            )
        }
        item { SectionTitle("Project list") }
        items(dash.projects, key = { it.id }) { project ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(project.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${project.creatorSummary} • ${project.memberCount} members", style = MaterialTheme.typography.bodySmall)
                    Text("Created ${project.createdLabel} • ${project.techSummary}", style = MaterialTheme.typography.bodySmall)
                    AdminStatusChip(project.status.toDisplayString())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (project.status == AdminProjectStatus.PENDING_APPROVAL) {
                            Button(onClick = { viewModel.approveProject(project.id) }) { Text("Approve") }
                            OutlinedButton(onClick = { viewModel.rejectProject(project.id) }) { Text("Reject") }
                        }
                        TextButton(onClick = { editingProject = project }) { Text("Edit") }
                        OutlinedButton(
                            onClick = { viewModel.archiveProject(project.id) },
                            enabled = project.status != AdminProjectStatus.ARCHIVED && project.status != AdminProjectStatus.REJECTED
                        ) { Text("Archive") }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminContentSection(viewModel: AdminViewModel) {
    val dash by viewModel.dashboard.collectAsStateWithLifecycle()
    val activeReports = dash.reports.filter { it.active }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item {
            AdminSectionIntro(
                title = "Content moderation",
                body = "Approving clears the queue; holds flag items for another review pass."
            )
        }
        item { SectionTitle("Content dashboard") }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Queued posts (${dash.contentQueue.size})", style = MaterialTheme.typography.titleMedium)
                    dash.contentQueue.forEach { item ->
                        Text(
                            "• ${item.title} — ${item.author}${if (item.held) " (held)" else ""}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.approveAllQueuedContent() },
                            enabled = dash.contentQueue.isNotEmpty()
                        ) { Text("Approve all visible") }
                        OutlinedButton(
                            onClick = { viewModel.holdQueuedContentForReview() },
                            enabled = dash.contentQueue.any { !it.held }
                        ) { Text("Hold for review") }
                    }
                }
            }
        }
        item { SectionTitle("Reports (${activeReports.size})") }
        items(activeReports, key = { it.id }) { report ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(report.summary, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.removeReportedContent(report.id) }) { Text("Remove content") }
                        OutlinedButton(onClick = { viewModel.dismissReport(report.id) }) { Text("Dismiss report") }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMessagingSection(viewModel: AdminViewModel) {
    val dash by viewModel.dashboard.collectAsStateWithLifecycle()
    var showCompose by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("All active users") }

    if (showCompose) {
        AlertDialog(
            onDismissRequest = { showCompose = false },
            title = { Text("Compose notification") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    TextField(body, onValueChange = { body = it }, label = { Text("Body") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    TextField(audience, onValueChange = { audience = it }, label = { Text("Audience") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && body.isNotBlank()) {
                            viewModel.sendNotification(title, body, audience)
                            showCompose = false
                            title = ""
                            body = ""
                        }
                    },
                    enabled = title.isNotBlank() && body.isNotBlank()
                ) { Text("Send") }
            },
            dismissButton = { TextButton(onClick = { showCompose = false }) { Text("Cancel") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item {
            AdminSectionIntro(
                title = "Notifications & messaging",
                body = "Outbound sends append to history; scheduling sets the next maintenance window label."
            )
        }
        item { SectionTitle("Message center") }
        items(dash.outboundNotifications, key = { it.id }) { n ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(AppDesignTokens.cardInnerPadding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(n.title, fontWeight = FontWeight.SemiBold)
                    Text(n.body, style = MaterialTheme.typography.bodySmall)
                    Text("${n.audience} • ${n.sentAtLabel}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item {
            FilledTonalButton(onClick = { showCompose = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Compose notification")
            }
        }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Push scheduling", style = MaterialTheme.typography.titleMedium)
                    Text("FCM topic: ${dash.pushTopic} • Next: ${dash.nextPushSlotLabel}", style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = { viewModel.schedulePush() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Schedule push")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAnalyticsSection(viewModel: AdminViewModel) {
    val copyToClipboard = rememberCopyToClipboardHandler()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item {
            AdminSectionIntro(
                title = "Reports & analytics",
                body = "Exports produce CSV text you can paste into Sheets; charts plug in when analytics API exists."
            )
        }
        item { SectionTitle("User analytics") }
        item {
            AdminPlaceholderCard(
                title = "Active users / retention",
                description = "Wire time-series queries for DAU/WAU and retention cohorts."
            )
        }
        item { SectionTitle("Project analytics") }
        item {
            AdminPlaceholderCard(
                title = "Projects created vs completed",
                description = "Track funnel from approval → active → archived."
            )
        }
        item { SectionTitle("Messaging analytics") }
        item {
            AdminPlaceholderCard(
                title = "Messages & engagement",
                description = "Median latency and flagged-rate KPIs."
            )
        }
        item { SectionTitle("Activity reports") }
        item {
            FilledTonalButton(
                onClick = {
                    val csv = viewModel.exportAnalyticsCsv()
                    copyToClipboard(csv)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export CSV")
            }
        }
    }
}

@Composable
fun AdminPlatformSettingsSection(viewModel: AdminViewModel) {
    val dash by viewModel.dashboard.collectAsStateWithLifecycle()
    val settings = dash.platformSettings

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item {
            AdminSectionIntro(
                title = "Settings & configuration",
                body = "Toggles call through the repository for immediate state updates."
            )
        }
        item { SectionTitle("Platform") }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Default notification policy", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = settings.defaultNotificationsEnabled,
                            onCheckedChange = { viewModel.setDefaultNotifications(it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Strict transport encryption", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = settings.strictTransportEncryption,
                            onCheckedChange = { viewModel.setStrictEncryption(it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Share analytics with product team", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = settings.analyticsSharingEnabled,
                            onCheckedChange = { viewModel.setAnalyticsSharing(it) }
                        )
                    }
                }
            }
        }
        item { SectionTitle("API keys") }
        item {
            AdminPlaceholderCard(
                title = "Integration keys",
                description = "Rotate keys periodically; store secrets server-side only."
            )
        }
        item {
            OutlinedButton(onClick = { viewModel.rotateApiKeys() }, modifier = Modifier.fillMaxWidth()) {
                Text("Rotate integration keys")
            }
        }
        item { SectionTitle("Theme & branding") }
        item {
            Text(
                "Current draft: ${settings.themeDraftLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = { viewModel.saveThemeDraft() }, modifier = Modifier.fillMaxWidth()) {
                Text("Save theme draft")
            }
        }
    }
}

@Composable
fun AdminSupportSection(viewModel: AdminViewModel) {
    val dash by viewModel.dashboard.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item {
            AdminSectionIntro(
                title = "Feedback & help desk",
                body = "Tickets transition Open → Assigned → Closed."
            )
        }
        item { SectionTitle("Support tickets") }
        items(dash.tickets, key = { it.id }) { ticket ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ticket.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        AdminStatusChip(ticket.status.toDisplayString())
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.assignTicket(ticket.id) },
                            enabled = ticket.status == TicketStatus.OPEN
                        ) { Text("Assign to me") }
                        Button(
                            onClick = { viewModel.closeTicket(ticket.id) },
                            enabled = ticket.status != TicketStatus.CLOSED
                        ) { Text("Close") }
                    }
                }
            }
        }
        item { SectionTitle("Feedback") }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(AppDesignTokens.cardInnerPadding)) {
                    Text("\"${dash.feedbackSuggestion}\"", style = MaterialTheme.typography.bodyMedium)
                    Text("${dash.feedbackVotes} votes", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item { SectionTitle("FAQs & articles") }
        item {
            FilledTonalButton(onClick = { viewModel.addHelpArticle() }, modifier = Modifier.fillMaxWidth()) {
                Text("New help article")
            }
        }
    }
}

@Composable
fun AdminAccessSection(viewModel: AdminViewModel) {
    val dash by viewModel.dashboard.collectAsStateWithLifecycle()
    var editingAdmin by remember { mutableStateOf<String?>(null) }

    editingAdmin?.let { adminId ->
        val account = dash.adminAccounts.firstOrNull { it.id == adminId }
        var selected by remember(adminId) {
            mutableStateOf(account?.permissionPreset ?: AdminPermissionPreset.MODERATOR)
        }
        AlertDialog(
            onDismissRequest = { editingAdmin = null },
            title = { Text("Permissions") },
            text = {
                Column {
                    AdminPermissionPreset.values().forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selected == preset,
                                    onClick = { selected = preset },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selected == preset, onClick = null)
                            Text(
                                when (preset) {
                                    AdminPermissionPreset.SUPER_ADMIN -> "Super admin"
                                    AdminPermissionPreset.MODERATOR -> "Moderator"
                                    AdminPermissionPreset.ANALYST -> "Analyst"
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateAdminPreset(adminId, selected)
                    editingAdmin = null
                }) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { editingAdmin = null }) { Text("Cancel") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item {
            AdminSectionIntro(
                title = "Admin access control",
                body = "RBAC presets map to future JWT scopes; audit log captures sensitive actions."
            )
        }
        item { SectionTitle("Admin directory") }
        items(dash.adminAccounts, key = { it.id }) { admin ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(admin.displayName, fontWeight = FontWeight.SemiBold)
                    Text(admin.roleLabel, style = MaterialTheme.typography.bodySmall)
                    Text("Last seen ${admin.lastSeenLabel}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { editingAdmin = admin.id }) { Text("Edit permissions") }
                }
            }
        }
        item { SectionTitle("RBAC") }
        item {
            AdminPlaceholderCard(
                title = "Role templates",
                description = "Super admin: full access • Moderator: content/users • Analyst: read-only analytics."
            )
        }
        item { SectionTitle("Audit logs") }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(AppDesignTokens.cardInnerPadding)) {
                    dash.auditLog.take(25).forEach { entry ->
                        AdminActivityRow(entry.timeLabel, entry.message)
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberCopyToClipboardHandler(): (String) -> Unit {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    return { text ->
        scope.launch {
            clipboard.setClipEntry(
                ClipEntry(ClipData.newPlainText("devconnect", text))
            )
        }
    }
}
