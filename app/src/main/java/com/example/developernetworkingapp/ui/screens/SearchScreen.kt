package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.domain.model.SearchResult
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.SearchUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.SearchViewModel

@Composable
fun SearchRoute(padding: PaddingValues, navController: NavController) {
    val viewModel: SearchViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(padding, state, viewModel::updateQuery, navController)
}

@Composable
fun SearchScreen(
    padding: PaddingValues,
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    navController: NavController
) {
    var selectedResult by remember { mutableStateOf<SearchResult?>(null) }

    selectedResult?.let { result ->
        AlertDialog(
            onDismissRequest = { selectedResult = null },
            title = { Text(result.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(result.subtitle, style = MaterialTheme.typography.bodyMedium)
                    Text("Owner: ${result.owner}", style = MaterialTheme.typography.bodyMedium)
                    Text("Location: ${result.location}", style = MaterialTheme.typography.bodyMedium)
                    Text("Stack: ${result.stack}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Team size: ${result.membersCount} developers", style = MaterialTheme.typography.bodyMedium)
                    Text(result.description, style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        result.rolesNeeded.forEach { role ->
                            AssistChip(
                                onClick = {
                                    navController.navigate(
                                        AppRoutes.detailRoute(
                                            title = role,
                                            subtitle = "Role requirement",
                                            description = "Role needed in ${result.title} by ${result.owner}. Review expectations, tools, and availability before joining.",
                                            sourceRoute = AppRoutes.SEARCH
                                        )
                                    )
                                },
                                label = { Text(role) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedResult = null
                    navController.navigate(AppRoutes.CHAT)
                }) { Text("Join the stack") }
            },
            dismissButton = {
                TextButton(onClick = { selectedResult = null }) { Text("Close") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                label = { Text("Search by stack, location, skills, availability") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { SectionTitle("Quick Filters") }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (state.content?.filters ?: emptyList()).forEach { label ->
                    AssistChip(
                        onClick = {
                            navController.navigate(
                                AppRoutes.detailRoute(
                                    title = "Search Filter",
                                    subtitle = label,
                                    description = "Browse developers and teams filtered by $label to find relevant collaboration opportunities.",
                                    sourceRoute = AppRoutes.SEARCH
                                )
                            )
                        },
                        label = { Text(label) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    )
                }
            }
        }
        item { SectionTitle("Search Results") }
        items(state.content?.results ?: emptyList()) { result ->
            ElevatedCard(
                onClick = { selectedResult = result },
                shape = AppDesignTokens.cardLargeShape,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(AppDesignTokens.cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(result.title, style = MaterialTheme.typography.titleMedium)
                    Text(result.subtitle, style = MaterialTheme.typography.bodyMedium)
                    Text(result.stack, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        result.rolesNeeded.forEach { role ->
                            AssistChip(
                                onClick = {
                                    navController.navigate(
                                        AppRoutes.detailRoute(
                                            title = result.title,
                                            subtitle = "Role: $role",
                                            description = "${result.subtitle}\n\nOwner: ${result.owner}\nLocation: ${result.location}\nStack: ${result.stack}",
                                            sourceRoute = AppRoutes.SEARCH
                                        )
                                    )
                                },
                                label = { Text(role) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = {
                            navController.navigate(
                                AppRoutes.detailRoute(
                                    title = result.title,
                                    subtitle = result.subtitle,
                                    description = "Owner: ${result.owner}\nLocation: ${result.location}\nStack: ${result.stack}\nTeam size: ${result.membersCount}\n\n${result.description}",
                                    sourceRoute = AppRoutes.PROJECTS
                                )
                            )
                        }) { Text("View details") }
                        TextButton(onClick = {
                            navController.navigate(
                                AppRoutes.collaboratorProfileRoute(
                                    name = result.owner,
                                    stack = result.stack,
                                    score = (result.membersCount * 7).coerceAtMost(99)
                                )
                            )
                        }) { Text("View owner") }
                    }
                }
            }
        }
    }
}
