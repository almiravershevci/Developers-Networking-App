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
import com.example.developernetworkingapp.domain.model.SearchResult
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.state.SearchUiState
import com.example.developernetworkingapp.ui.viewmodel.SearchViewModel

@Composable
fun SearchRoute(padding: PaddingValues) {
    val viewModel: SearchViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(padding, state, viewModel::updateQuery)
}

@Composable
fun SearchScreen(padding: PaddingValues, state: SearchUiState, onQueryChange: (String) -> Unit) {
    var selectedResult by remember { mutableStateOf<SearchResult?>(null) }

    selectedResult?.let { result ->
        AlertDialog(
            onDismissRequest = { selectedResult = null },
            title = { Text(result.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(result.subtitle, style = MaterialTheme.typography.bodyMedium)
                    Text("Owner: ${result.owner}", style = MaterialTheme.typography.bodySmall)
                    Text("Location: ${result.location}", style = MaterialTheme.typography.bodySmall)
                    Text("Stack: ${result.stack}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text("Team size: ${result.membersCount} developers", style = MaterialTheme.typography.bodySmall)
                    Text(result.description, style = MaterialTheme.typography.bodySmall)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        result.rolesNeeded.forEach { role ->
                            AssistChip(onClick = {}, label = { Text(role) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedResult = null }) { Text("Join the stack") }
            },
            dismissButton = {
                TextButton(onClick = { selectedResult = null }) { Text("Close") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
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
                        onClick = {},
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
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(result.title, style = MaterialTheme.typography.titleMedium)
                    Text(result.subtitle, style = MaterialTheme.typography.bodySmall)
                    Text(result.stack, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        result.rolesNeeded.forEach { role ->
                            AssistChip(onClick = {}, label = { Text(role) })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { selectedResult = result }) { Text("View details") }
                        TextButton(onClick = { selectedResult = result }) { Text("Join stack") }
                    }
                }
            }
        }
    }
}
