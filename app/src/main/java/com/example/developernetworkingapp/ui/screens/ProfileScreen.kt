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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.developernetworkingapp.ui.components.PremiumInfoCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.state.ProfileUiState
import com.example.developernetworkingapp.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileRoute(padding: PaddingValues) {
    val viewModel: ProfileViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(padding, state)
}

@Composable
fun ProfileScreen(padding: PaddingValues, state: ProfileUiState) {
    val content = state.content
    var showPortfolioDialog by remember { mutableStateOf(false) }
    var showInsightsDialog by remember { mutableStateOf(false) }

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

    LazyColumn(
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
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(content?.name ?: "Loading user...", style = MaterialTheme.typography.titleLarge)
                        Text(content?.role ?: "Loading role...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item { Text(content?.bio ?: "Preparing profile...", style = MaterialTheme.typography.bodyMedium) }
        item { SectionTitle("Tech Stack") }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (content?.stacks ?: emptyList()).forEach { stack ->
                    AssistChip(onClick = {}, label = { Text(stack) })
                }
            }
        }
        item {
            ElevatedCard(
                onClick = { showPortfolioDialog = true },
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Portfolio", style = MaterialTheme.typography.titleMedium)
                    Text(content?.portfolio ?: "", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showPortfolioDialog = true }) { Text("Open links") }
                        TextButton(onClick = {}) { Text("Edit") }
                    }
                }
            }
        }
        item {
            ElevatedCard(
                onClick = { showInsightsDialog = true },
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("GitHub Insights", style = MaterialTheme.typography.titleMedium)
                    Text(content?.insights ?: "", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showInsightsDialog = true }) { Text("View details") }
                        TextButton(onClick = {}) { Text("Sync now") }
                    }
                }
            }
        }
    }
}
