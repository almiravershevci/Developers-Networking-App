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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.di.CollaboratorProfileViewModelFactory
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.viewmodel.CollaboratorProfileViewModel
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.theme.AppDesignTokens

@Composable
fun CollaboratorProfileRoute(
    padding: PaddingValues,
    navController: NavController,
    collaboratorId: String,
    score: Int,
) {
    val viewModel: CollaboratorProfileViewModel = viewModel(
        factory = CollaboratorProfileViewModelFactory(collaboratorId, score),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CollaboratorProfileScreen(
        padding = padding,
        navController = navController,
        state = state,
    )
}

@Composable
fun GenericDetailScreen(
    padding: PaddingValues,
    navController: NavController,
    title: String,
    subtitle: String,
    description: String,
    sourceRoute: String
) {
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
        if (sourceRoute == AppRoutes.PROJECTS) {
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
    state: com.example.developernetworkingapp.ui.viewmodel.CollaboratorProfileUiState,
) {
    val collaborator = state.profile
    val uriHandler = LocalUriHandler.current
    if (state.isLoading || collaborator == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        ) {
            Text("Loading profile from Firestore…", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }
    val name = collaborator.name
    val stack = collaborator.stack
    val score = collaborator.matchScore
    val matchBadge = when {
        score >= 95 -> Triple("Elite Fit", Color(0xFF0F766E), "Top-tier stack alignment")
        score >= 90 -> Triple("Great Fit", Color(0xFF1D4ED8), "Strong collaboration fit")
        score >= 80 -> Triple("Strong Fit", Color(0xFF7C3AED), "Solid potential match")
        else -> Triple("Potential Fit", Color(0xFFB45309), "Worth exploring")
    }
    val email = collaborator.email

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
                Text(collaborator.summary, style = MaterialTheme.typography.bodyMedium)
                if (email.isNotBlank()) {
                    Text("Contact: $email", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
                Text("Headline: ${collaborator.location}", style = MaterialTheme.typography.bodyMedium)
                Text(collaborator.availability, style = MaterialTheme.typography.bodyMedium)
                state.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (email.isNotBlank()) {
                Button(onClick = { uriHandler.openUri("mailto:$email?subject=Collaboration%20from%20DevConnect") }) {
                    Text("Email")
                }
            }
            TextButton(onClick = { navController.navigate(AppRoutes.DASHBOARD) }) { Text("Back to Dashboard") }
        }
    }
}
