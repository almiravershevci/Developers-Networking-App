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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val examples = listOf(
        "Example workflow: discovery -> planning -> sprint execution -> review -> release",
        "Example metric: cycle time reduced by 18% after dependency mapping",
        "Example team setup: 1 product owner, 2 mobile devs, 1 backend dev, 1 QA"
    )
    val checklist = listOf(
        "Define clear ownership and deadlines",
        "Track blockers in daily updates",
        "Review risk and scope each sprint",
        "Document outcomes for handoff"
    )
    LazyColumn(
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
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item { SectionTitle("Detail View") }
        item {
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
        }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Need task-level board for this project?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = { navController.navigate(AppRoutes.projectsRoute(title)) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Open Project Workspace") }
                }
            }
        }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Implementation Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "This section documents how this module/project/event should run in practice, what success looks like, and which checks keep delivery predictable.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    examples.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Execution Checklist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    checklist.forEach { item -> Text("• $item", style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }
        item {
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
    collaboratorId: String,
    score: Int
) {
    data class CollaboratorSeed(
        val stack: String,
        val location: String,
        val availability: String,
        val summary: String,
        val projects: List<Pair<String, String>>,
        val collaborationHistory: List<String>
    )

    data class CollaboratorUiData(
        val name: String,
        val seed: CollaboratorSeed
    )

    val collaboratorSeeds = mapOf(
        "mina" to CollaboratorUiData("Mina", CollaboratorSeed(
            stack = "Android + Firebase",
            location = "Prishtina (UTC+2)",
            availability = "14-18 hrs/week",
            summary = "Specializes in Android architecture, realtime sync flows, and stable release delivery for mobile-first teams.",
            projects = listOf(
                "DevPulse Mobile Alerts" to "Built Firebase-driven push workflows with segmented notification rules for different user cohorts.",
                "Compose Performance Lab" to "Reduced startup latency by introducing baseline profile optimization and render tracing.",
                "Remote Team Sprintboard" to "Delivered a compact sprint dashboard focused on async collaboration and mobile status updates."
            ),
            collaborationHistory = listOf(
                "Partnered with backend engineers to define reliable event contracts for mobile sync.",
                "Led weekly release quality reviews across QA and Android contributors.",
                "Mentored 3 junior Android developers on clean architecture and testing."
            )
        )),
        "khaled" to CollaboratorUiData("Khaled", CollaboratorSeed(
            stack = "Backend + AI APIs",
            location = "Tirana (UTC+2)",
            availability = "10-14 hrs/week",
            summary = "Focuses on API reliability, AI integration, and performance tuning for high-traffic collaboration platforms.",
            projects = listOf(
                "Talent Graph Ranking Engine" to "Implemented weighted skill-ranking logic with caching and explainable scoring output.",
                "Realtime Notification Gateway" to "Designed queue-backed notification delivery with retry strategy and failure dashboards.",
                "LLM Workflow Assistant API" to "Integrated AI suggestion endpoints with guardrails, logging, and fallback behaviors."
            ),
            collaborationHistory = listOf(
                "Coordinated API schemas with mobile and frontend teams to avoid integration drift.",
                "Introduced endpoint SLO tracking and alert thresholds for production incidents.",
                "Reviewed and improved database indexing strategy for search-heavy workloads."
            )
        )),
        "nora" to CollaboratorUiData("Nora", CollaboratorSeed(
            stack = "UI/UX + React Native",
            location = "Skopje (UTC+1)",
            availability = "12-16 hrs/week",
            summary = "Combines UX research with React Native execution to ship intuitive, high-retention collaboration experiences.",
            projects = listOf(
                "CollabFlow Design System" to "Created reusable UI primitives and interaction patterns used across multiple product modules.",
                "Mentor Connect Mobile" to "Designed and built onboarding funnels that increased week-one activation rates.",
                "Cross-Platform Team Rooms" to "Shipped collaborative room interfaces with clear task context and lightweight presence cues."
            ),
            collaborationHistory = listOf(
                "Ran user interview rounds and translated findings into prioritized UX improvements.",
                "Collaborated with product owners to map journeys and reduce onboarding friction.",
                "Worked with frontend engineers on accessible design implementation and consistency."
            )
        ))
    )

    val collaborator = collaboratorSeeds[collaboratorId] ?: CollaboratorUiData(
        name = collaboratorId.replace("-", " ").split(" ").joinToString(" ") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }.ifBlank { "Collaborator" },
        seed = CollaboratorSeed(
            stack = "Full Stack Collaboration",
            location = "Remote (UTC+2)",
            availability = "10-12 hrs/week",
            summary = "Generalist collaborator contributing to product delivery across design, frontend, and backend tasks.",
            projects = listOf(
                "Cross-Team Delivery Board" to "Supported implementation planning and sprint execution with clear ownership tracking.",
                "Feature Rollout Toolkit" to "Helped define release checklists, rollout phases, and post-launch monitoring steps.",
                "Community Collaboration Hub" to "Contributed to core workflows for teammate discovery, updates, and async feedback."
            ),
            collaborationHistory = listOf(
                "Worked across teams to unblock dependencies during sprint execution.",
                "Helped document technical decisions and handoff notes for continuity.",
                "Contributed to testing and validation before production rollouts."
            )
        ),
    )

    val name = collaborator.name
    val stack = collaborator.seed.stack
    val experienceYears = (score % 7) + 2
    val completedProjects = score + 6
    val collaborations = (score % 12) + 9
    val matchBadge = when {
        score >= 95 -> Triple("Elite Fit", Color(0xFF0F766E), "Top-tier stack alignment and delivery confidence")
        score >= 90 -> Triple("Great Fit", Color(0xFF1D4ED8), "Strong compatibility for active project collaboration")
        score >= 80 -> Triple("Strong Fit", Color(0xFF7C3AED), "Solid potential with minor onboarding ramp")
        else -> Triple("Potential Fit", Color(0xFFB45309), "Worth exploring based on overlap and availability")
    }
    val email = "${name.lowercase().replace(" ", ".")}@devnet.app"
    val uriHandler = LocalUriHandler.current
    val personProjects = collaborator.seed.projects.map { (projectTitle, _) -> "$name • $projectTitle" }

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
                AssistChip(
                    onClick = {},
                    label = { Text("${matchBadge.first} • $score%") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = matchBadge.second.copy(alpha = 0.16f),
                        labelColor = matchBadge.second
                    )
                )
                Text(matchBadge.third, style = MaterialTheme.typography.bodySmall)
                Text("Match score: $score%", style = MaterialTheme.typography.bodyMedium)
                Text("Experience: $experienceYears years", style = MaterialTheme.typography.bodyMedium)
                Text("Completed collaborations: $completedProjects", style = MaterialTheme.typography.bodyMedium)
                Text("Active collaboration count: $collaborations", style = MaterialTheme.typography.bodyMedium)
                Text(
                    collaborator.seed.summary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("Contact: $email", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Text("Location: ${collaborator.seed.location}", style = MaterialTheme.typography.bodyMedium)
                Text("Availability: ${collaborator.seed.availability}", style = MaterialTheme.typography.bodyMedium)
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
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Projects Built", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                collaborator.seed.projects.forEach { (projectTitle, projectSummary) ->
                    Text("• $projectTitle", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(projectSummary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Collaboration History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                collaborator.seed.collaborationHistory.forEach { item ->
                    Text("• $item", style = MaterialTheme.typography.bodyMedium)
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
