package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.developernetworkingapp.di.appViewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.data.repository.UserRole
import com.example.developernetworkingapp.ui.components.EnhancedCard
import com.example.developernetworkingapp.ui.components.InteractiveButton
import com.example.developernetworkingapp.ui.components.NotificationBanner
import com.example.developernetworkingapp.ui.components.PremiumInfoCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.ProfileUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.ProfileUiEvent
import com.example.developernetworkingapp.ui.viewmodel.ProfileViewModel
import com.example.developernetworkingapp.ui.viewmodel.SessionViewModel
import kotlinx.coroutines.flow.SharedFlow

val ElectricCyan = Color(0xFF00FFFF)
val NeonBlue = Color(0xFF1E90FF)
val NeonViolet = Color(0xFF8A2BE2)
val VibrantOrange = Color(0xFFFF4500)
val BrightPink = Color(0xFFFF1493)
val ElectricGreen = Color(0xFF00FF7F)

@Composable
fun ProfileRoute(padding: PaddingValues, navController: NavController) {
    val viewModel: ProfileViewModel = appViewModel()
    val sessionViewModel: SessionViewModel = appViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by sessionViewModel.currentUser.collectAsStateWithLifecycle()

    CollectProfileNavEvents(navController)

    ProfileScreen(
        padding = padding,
        state = state,
        events = viewModel.events,
        navController = navController,
        isAdmin = currentUser?.role == UserRole.ADMIN,
        onSaveProfile = viewModel::saveProfile,
        onSyncStarted = viewModel::notifySyncStarted,
        onLogout = viewModel::logout,
    )
}

@Composable
fun ProfileScreen(
    padding: PaddingValues,
    state: ProfileUiState,
    events: SharedFlow<ProfileUiEvent>,
    navController: NavController,
    isAdmin: Boolean,
    onSaveProfile: (String, String, String) -> Unit,
    onSyncStarted: () -> Unit,
    onLogout: () -> Unit
) {
    val content = state.content
    var showPortfolioDialog by remember { mutableStateOf(false) }
    var showInsightsDialog by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var activeNotification by rememberSaveable { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf(content?.name ?: "") }
    var editRole by remember { mutableStateOf(content?.role ?: "") }
    var editBio by remember { mutableStateOf(content?.bio ?: "") }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is ProfileUiEvent.ShowNotification -> activeNotification = event.message
            }
        }
    }

    activeNotification?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(AppDesignTokens.notificationAutoHideMs)
            activeNotification = null
        }
        NotificationBanner(message = message, onDismiss = { activeNotification = null })
    }

    if (showPortfolioDialog) {
        AlertDialog(
            onDismissRequest = { showPortfolioDialog = false },
            title = { Text("Portfolio Links") },
            text = { Text(content?.portfolio ?: "No links yet.") },
            confirmButton = { Button(onClick = { showPortfolioDialog = false }) { Text("Open") } },
            dismissButton = { TextButton(onClick = { showPortfolioDialog = false }) { Text("Close") } }
        )
    }
    if (showInsightsDialog) {
        AlertDialog(
            onDismissRequest = { showInsightsDialog = false },
            title = { Text("Contribution Insights") },
            text = { Text(content?.insights ?: "No insights yet.") },
            confirmButton = { Button(onClick = { showInsightsDialog = false }) { Text("View activity") } },
            dismissButton = { TextButton(onClick = { showInsightsDialog = false }) { Text("Close") } }
        )
    }
    if (showAchievementsDialog) {
        AlertDialog(
            onDismissRequest = { showAchievementsDialog = false },
            title = { Text("Developer Achievements") },
            text = {
                val profile = content
                val badges = buildList {
                    if (profile == null) return@buildList
                    if (profile.activeProjectsCount > 0) {
                        add("Active on ${profile.activeProjectsCount} project(s)")
                    }
                    if (profile.collaborationsCount > 0) {
                        add("${profile.collaborationsCount} collaboration(s) in your network")
                    }
                    profile.stacks.take(4).forEach { stack ->
                        add("Stack: $stack")
                    }
                    if (profile.activityItems.isNotEmpty()) {
                        add("Latest: ${profile.activityItems.first().title}")
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (badges.isEmpty()) {
                        Text(
                            "Complete projects and collaborate to build your achievement history.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        badges.forEach { badge ->
                            Text(badge, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { showAchievementsDialog = false }) { Text("View all") } },
            dismissButton = { TextButton(onClick = { showAchievementsDialog = false }) { Text("Close") } }
        )
    }
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // TextField for Name
                    TextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // TextField for Role
                    TextField(
                        value = editRole,
                        onValueChange = { editRole = it },
                        label = { Text("Role") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // TextField for Bio
                    TextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveProfile(editName, editRole, editBio)
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        ElectricCyan.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigate(AppRoutes.SETTINGS) }) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        item {
            EnhancedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showEditProfileDialog = true }
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(
                            Brush.linearGradient(listOf(NeonBlue, NeonViolet))
                        ))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(content?.name ?: "Loading user...", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(content?.role ?: "Loading role...", style = MaterialTheme.typography.titleMedium, color = ElectricCyan)
                            Text(
                                content?.statsLine?.takeIf { it.isNotBlank() } ?: "Building your profile",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VibrantOrange,
                            )
                        }
                    }
                    Text(content?.bio ?: "Passionate developer building amazing apps and connecting with fellow creators.", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { showEditProfileDialog = true }) { Text("Edit Profile") }
                        if (isAdmin) {
                            FilledTonalButton(onClick = { navController.navigate(AppRoutes.ADMIN_DASHBOARD) }) {
                                Text("Admin Dashboard")
                            }
                        }
                        TextButton(
                            onClick = onLogout
                        ) { Text("Log out") }
                    }
                }
            }
        }

        item { SectionTitle("Skills & Expertise") }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (content?.stacks.orEmpty()).forEach { stack ->
                    AssistChip(
                        onClick = {
                            navController.navigate(
                                AppRoutes.detailRoute(
                                    title = stack,
                                    subtitle = "Skill insight",
                                    description = "See active collaborators, related projects, and open opportunities for $stack.",
                                    sourceRoute = AppRoutes.SEARCH
                                )
                            )
                        },
                        label = { Text(stack) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = BrightPink.copy(alpha = 0.1f),
                            labelColor = BrightPink
                        )
                    )
                }
            }
        }

        item { SectionTitle("Portfolio & Achievements") }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EnhancedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { showPortfolioDialog = true }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Portfolio", style = MaterialTheme.typography.titleMedium, color = NeonBlue)
                        Text("GitHub, LinkedIn, Personal Site", style = MaterialTheme.typography.bodyMedium)
                        InteractiveButton(text = "View Links", onClick = { showPortfolioDialog = true })
                    }
                }
                EnhancedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { showAchievementsDialog = true }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Achievements", style = MaterialTheme.typography.titleMedium, color = VibrantOrange)
                        Text(
                            "${content?.collaborationsCount ?: 0} collaborations",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        InteractiveButton(text = "View Badges", onClick = { showAchievementsDialog = true })
                    }
                }
            }
        }

        item {
            EnhancedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                onClick = { showInsightsDialog = true }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("GitHub Insights", style = MaterialTheme.typography.titleMedium, color = ElectricGreen)
                    Text(
                        content?.insights?.takeIf { it.isNotBlank() } ?: "Connect GitHub to sync contribution insights.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InteractiveButton(
                            text = "View Details",
                            onClick = { showInsightsDialog = true },
                            modifier = Modifier.weight(1f),
                            fillMaxWidth = false
                        )
                        TextButton(
                            onClick = {
                                onSyncStarted()
                                navController.navigate(
                                    AppRoutes.detailRoute(
                                        title = "GitHub Sync",
                                        subtitle = "Sync started",
                                        description = "Repository activity, PR stats, and contribution insights are now syncing with your profile.",
                                        sourceRoute = AppRoutes.PROFILE
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Sync Now") }
                    }
                }
            }
        }

        item { SectionTitle("Activity Stats") }
        item {
            val activityStats = listOf(
                Triple("Projects", "${content?.activeProjectsCount ?: 0}", AppRoutes.PROJECTS),
                Triple("Collaborations", "${content?.collaborationsCount ?: 0}", AppRoutes.PROJECTS),
                Triple("Messages", "${content?.unreadMessagesCount ?: 0}", AppRoutes.CHAT),
                Triple("Tasks", "${content?.openTasksCount ?: 0}", AppRoutes.TASKS),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activityStats.forEach { (label, value, route) ->
                    EnhancedCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onClick = { navController.navigate(route) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                value,
                                style = MaterialTheme.typography.headlineSmall,
                                color = NeonViolet,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        item { SectionTitle("Recent Activity") }
        val activityItems = content?.activityItems.orEmpty()
        if (activityItems.isEmpty()) {
            item {
                Text(
                    "No activity in Firestore yet. Run firestore seed or complete actions in the app.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            items(activityItems.size) { index ->
                val item = activityItems[index]
                EnhancedCard(modifier = Modifier.fillMaxWidth(), onClick = { }) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.title, style = MaterialTheme.typography.bodyMedium)
                        Text(item.time, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item { SectionTitle("Account") }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = AppDesignTokens.cardShape,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Sign out from this device", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "You can log back in anytime with your credentials.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log out")
                    }
                }
            }
        }
    }
}
