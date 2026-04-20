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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.ui.components.EnhancedCard
import com.example.developernetworkingapp.ui.components.InteractiveButton
import com.example.developernetworkingapp.ui.components.PremiumInfoCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.ProfileUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.ProfileViewModel

val ElectricCyan = Color(0xFF00FFFF)
val NeonBlue = Color(0xFF1E90FF)
val NeonViolet = Color(0xFF8A2BE2)
val VibrantOrange = Color(0xFFFF4500)
val BrightPink = Color(0xFFFF1493)
val ElectricGreen = Color(0xFF00FF7F)

@Composable
fun ProfileRoute(padding: PaddingValues, navController: NavController) {
    val viewModel: ProfileViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(padding, state, navController)
}

@Composable
fun ProfileScreen(padding: PaddingValues, state: ProfileUiState, navController: NavController) {
    val content = state.content
    var showPortfolioDialog by remember { mutableStateOf(false) }
    var showInsightsDialog by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(content?.name ?: "") }
    var editRole by remember { mutableStateOf(content?.role ?: "") }
    var editBio by remember { mutableStateOf(content?.bio ?: "") }

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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🏆 Code Contributor - 50+ PRs merged", style = MaterialTheme.typography.bodyMedium)
                    Text("🚀 Project Leader - Led 3 successful projects", style = MaterialTheme.typography.bodyMedium)
                    Text("👥 Team Player - Collaborated with 20+ developers", style = MaterialTheme.typography.bodyMedium)
                    Text("📚 Knowledge Sharer - Published 10+ tutorials", style = MaterialTheme.typography.bodyMedium)
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
                        // Handle save action
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
                            Text("⭐ 4.8 Rating • 127 Projects", style = MaterialTheme.typography.bodyMedium, color = VibrantOrange)
                        }
                    }
                    Text(content?.bio ?: "Passionate developer building amazing apps and connecting with fellow creators.", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { showEditProfileDialog = true }) { Text("Edit Profile") }
                }
            }
        }

        item { SectionTitle("Skills & Expertise") }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (content?.stacks ?: listOf("Kotlin", "Android", "React", "Node.js", "Python")).forEach { stack ->
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EnhancedCard(modifier = Modifier.weight(1f), onClick = { showPortfolioDialog = true }) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Portfolio", style = MaterialTheme.typography.titleMedium, color = NeonBlue)
                        Text("GitHub, LinkedIn, Personal Site", style = MaterialTheme.typography.bodyMedium)
                        InteractiveButton(text = "View Links", onClick = { showPortfolioDialog = true })
                    }
                }
                EnhancedCard(modifier = Modifier.weight(1f), onClick = { showAchievementsDialog = true }) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Achievements", style = MaterialTheme.typography.titleMedium, color = VibrantOrange)
                        Text("4 badges earned", style = MaterialTheme.typography.bodyMedium)
                        InteractiveButton(text = "View Badges", onClick = { showAchievementsDialog = true })
                    }
                }
            }
        }

        item {
            EnhancedCard(modifier = Modifier.fillMaxWidth(), onClick = { showInsightsDialog = true }) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("GitHub Insights", style = MaterialTheme.typography.titleMedium, color = ElectricGreen)
                    Text("Commits: 1,247 | PRs: 89 | Stars: 156", style = MaterialTheme.typography.bodyMedium)
                    Text("Most active in Kotlin and Android projects", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InteractiveButton(text = "View Details", onClick = { showInsightsDialog = true })
                        TextButton(onClick = {
                            navController.navigate(
                                AppRoutes.detailRoute(
                                    title = "GitHub Sync",
                                    subtitle = "Sync started",
                                    description = "Repository activity, PR stats, and contribution insights are now syncing with your profile.",
                                    sourceRoute = AppRoutes.PROFILE
                                )
                            )
                        }) { Text("Sync Now") }
                    }
                }
            }
        }

        item { SectionTitle("Activity Stats") }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    "Projects" to "23",
                    "Collaborations" to "45",
                    "Messages" to "189",
                    "Events" to "12"
                ).forEach { (label, value) ->
                    EnhancedCard(modifier = Modifier.weight(1f), onClick = {
                        navController.navigate(
                            AppRoutes.detailRoute(
                                title = label,
                                subtitle = "Activity stat",
                                description = "Detailed trend chart, weekly changes, and linked records for $label: $value.",
                                sourceRoute = AppRoutes.PROFILE
                            )
                        )
                    }) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, style = MaterialTheme.typography.headlineMedium, color = NeonViolet, fontWeight = FontWeight.Bold)
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        item { SectionTitle("Recent Activity") }
        items(5) { index ->
            EnhancedCard(modifier = Modifier.fillMaxWidth(), onClick = {
                navController.navigate(
                    AppRoutes.detailRoute(
                        title = "Recent Activity ${index + 1}",
                        subtitle = "Profile timeline",
                        description = "Full activity thread with timestamped updates, linked projects, and collaborator actions.",
                        sourceRoute = AppRoutes.PROFILE
                    )
                )
            }) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Joined project: Mobile App Redesign", style = MaterialTheme.typography.bodyMedium)
                    Text("2 hours ago", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
