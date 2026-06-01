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
import com.example.developernetworkingapp.ui.components.PremiumInfoCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.SearchUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.SearchViewModel

@Composable
fun SearchRoute(padding: PaddingValues, navController: NavController) {
    val viewModel: SearchViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(padding, state, viewModel::updateQuery, viewModel::loadTrendingTopics, navController)
}

@Composable
fun SearchScreen(
    padding: PaddingValues,
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onReloadTrends: () -> Unit,
    navController: NavController
) {
    var selectedResult by remember { mutableStateOf<SearchResult?>(null) }
    var showJoinStackForm by remember { mutableStateOf(false) }
    var applicantName by remember { mutableStateOf("") }
    var applicantSurname by remember { mutableStateOf("") }
    var applicantEmail by remember { mutableStateOf("") }
    var applicantRole by remember { mutableStateOf("") }
    val query = state.query.trim().lowercase()
    val filteredResults = remember(state.content?.results, query) {
        val allResults = state.content?.results ?: emptyList()
        if (query.isBlank()) {
            allResults
        } else {
            allResults.filter { result ->
                result.title.lowercase().contains(query) ||
                    result.subtitle.lowercase().contains(query) ||
                    result.stack.lowercase().contains(query) ||
                    result.owner.lowercase().contains(query) ||
                    result.location.lowercase().contains(query) ||
                    result.rolesNeeded.any { it.lowercase().contains(query) } ||
                    result.description.lowercase().contains(query)
            }
        }
    }

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
                                onClick = { onQueryChange(role) },
                                label = { Text(role) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showJoinStackForm = true
                }) { Text("Join the stack") }
            },
            dismissButton = {
                TextButton(onClick = { selectedResult = null }) { Text("Close") }
            }
        )
    }

    if (showJoinStackForm && selectedResult != null) {
        AlertDialog(
            onDismissRequest = { showJoinStackForm = false },
            title = { Text("Join ${selectedResult!!.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = applicantName,
                        onValueChange = { applicantName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = applicantSurname,
                        onValueChange = { applicantSurname = it },
                        label = { Text("Surname") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = applicantEmail,
                        onValueChange = { applicantEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = applicantRole,
                        onValueChange = { applicantRole = it },
                        label = { Text("Role you want for this stack") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showJoinStackForm = false
                        selectedResult = null
                        applicantName = ""
                        applicantSurname = ""
                        applicantEmail = ""
                        applicantRole = ""
                    },
                    enabled = applicantName.isNotBlank() &&
                        applicantSurname.isNotBlank() &&
                        applicantEmail.isNotBlank() &&
                        applicantRole.isNotBlank()
                ) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = { showJoinStackForm = false }) { Text("Cancel") }
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    label = { Text("Search by stack, location, skills, availability") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.query.isNotBlank()) {
                    TextButton(onClick = { onQueryChange("") }) {
                        Text("Clear filter")
                    }
                }
            }
        }
        state.content?.statusMessage?.let { message ->
            item {
                PremiumInfoCard(
                    title = "Talent search",
                    subtitle = message,
                )
            }
        }
        item { SectionTitle("Quick Filters") }
        if (state.errorMessage != null) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.errorMessage, color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton(onClick = onReloadTrends) { Text("Retry live API") }
                    }
                }
            }
        }
        if (state.trendingTopics.isNotEmpty()) {
            item { SectionTitle("Live API Trends") }
            item {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.trendingTopics.forEach { topic ->
                        AssistChip(
                            onClick = { onQueryChange(topic) },
                            label = { Text(topic.take(28)) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        )
                    }
                }
            }
        }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (state.content?.filters ?: emptyList()).forEach { label ->
                    AssistChip(
                        onClick = { onQueryChange(label) },
                        label = { Text(label) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    )
                }
            }
        }
        item { SectionTitle("Search Results") }
        if (filteredResults.isEmpty()) {
            item {
                ElevatedCard(
                    shape = AppDesignTokens.cardShape,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(modifier = Modifier.padding(AppDesignTokens.cardInnerPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("No projects match \"$query\".", style = MaterialTheme.typography.titleSmall)
                        Text("Try another stack, role, or location keyword.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        items(filteredResults) { result ->
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
                                onClick = { onQueryChange(role) },
                                label = { Text(role) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = {
                            if (result.projectId.isNotBlank()) {
                                navController.navigate(AppRoutes.projectsRoute(result.projectId))
                            } else {
                                navController.navigate(
                                    AppRoutes.detailRoute(
                                        title = result.title,
                                        subtitle = result.subtitle,
                                        description = "Owner: ${result.owner}\nLocation: ${result.location}\nStack: ${result.stack}\nTeam size: ${result.membersCount}\n\n${result.description}",
                                        sourceRoute = AppRoutes.PROJECTS,
                                    ),
                                )
                            }
                        }) { Text("View details") }
                        TextButton(onClick = {
                            navController.navigate(
                                AppRoutes.detailRoute(
                                    title = "${result.owner} Profile",
                                    subtitle = "${result.stack} • Match ${(result.membersCount * 7).coerceAtMost(99)}%",
                                    description = "Owner: ${result.owner}\nStack: ${result.stack}\nLocation: ${result.location}\nTeam size: ${result.membersCount}\n\nProject focus:\n${result.description}\n\nThis profile summarizes collaborator fit, active work style, and relevant technical strengths.",
                                    sourceRoute = AppRoutes.SEARCH
                                )
                            )
                        }) { Text("View owner") }
                    }
                }
            }
        }
    }
}
