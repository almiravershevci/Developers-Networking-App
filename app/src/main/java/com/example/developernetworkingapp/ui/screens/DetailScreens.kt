package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.AssistChip
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavController
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.theme.AppDesignTokens

@Composable
fun GenericDetailScreen(
    padding: PaddingValues,
    navController: NavController,
    title: String,
    subtitle: String,
    description: String,
    sourceRoute: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing)
    ) {
        SectionTitle("Detail View")
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { navController.navigate(sourceRoute) }) {
                Text("Open Related Section")
            }
            TextButton(onClick = { navController.navigate(AppRoutes.DASHBOARD) }) {
                Text("Back to Home")
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun CollaboratorProfileScreen(
    padding: PaddingValues,
    navController: NavController,
    name: String,
    stack: String,
    score: Int
) {
    val experienceYears = (score % 7) + 2
    val completedProjects = score + 6
    val email = "${name.lowercase().replace(" ", ".")}@devnet.app"
    val uriHandler = LocalUriHandler.current
    val personProjects = listOf(
        "$name • API Reliability Sprint",
        "$name • ${stack.substringBefore("+").trim()} Performance Lab",
        "$name • Remote Collaboration Toolkit"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing)
    ) {
        SectionTitle("Collaborator Profile")
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(stack, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text("Match score: $score%", style = MaterialTheme.typography.bodyMedium)
                Text("Experience: $experienceYears years", style = MaterialTheme.typography.bodyMedium)
                Text("Completed collaborations: $completedProjects", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Focuses on reliable delivery, clean architecture, and async communication in distributed teams.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("Contact: $email", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    personProjects.forEach { project ->
                        AssistChip(
                            onClick = {
                                navController.navigate(
                                    AppRoutes.detailRoute(
                                        title = project,
                                        subtitle = "Project by $name",
                                        description = "Detailed scope, timeline, team roles, and contribution history for this collaborator project.",
                                        sourceRoute = AppRoutes.PROJECTS
                                    )
                                )
                            },
                            label = { Text(project.take(24)) }
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { uriHandler.openUri("mailto:$email?subject=Collaboration%20request%20from%20DevNet") }) {
                Text("Message")
            }
            Button(
                onClick = {
                    navController.navigate(
                        AppRoutes.detailRoute(
                            title = "$name's Projects",
                            subtitle = "Portfolio projects",
                            description = personProjects.joinToString(separator = "\n• ", prefix = "• "),
                            sourceRoute = AppRoutes.PROJECTS
                        )
                    )
                }
            ) { Text("View Projects") }
            TextButton(onClick = { navController.navigate(AppRoutes.SEARCH) }) { Text("Back to Search") }
        }
    }
}
