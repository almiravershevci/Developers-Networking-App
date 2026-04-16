package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.domain.model.ProjectPost
import com.example.developernetworkingapp.ui.components.GradientHeroCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.data.ShortcutItem
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.DashboardUiState
import com.example.developernetworkingapp.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardRoute(
    padding: PaddingValues,
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(padding, navController, state, viewModel::loadDashboard)
}

@Composable
fun DashboardScreen(
    padding: PaddingValues,
    navController: NavController,
    state: DashboardUiState,
    onRefresh: () -> Unit
) {
    val content = state.content
    val posts = remember(content?.projectPosts) {
        mutableStateListOf(*(content?.projectPosts ?: emptyList()).toTypedArray())
    }
    var showPostDialog by rememberSaveable { mutableStateOf(false) }
    var postTitle by rememberSaveable { mutableStateOf("") }
    var postStack by rememberSaveable { mutableStateOf("") }
    var postDescription by rememberSaveable { mutableStateOf("") }

    val shortcuts = listOf(
        ShortcutItem("Task Board", AppRoutes.TASKS, Icons.Outlined.Task),
        ShortcutItem("Live Events", AppRoutes.EVENTS, Icons.Outlined.Event),
        ShortcutItem("Chat Hub", AppRoutes.CHAT, Icons.Outlined.ChatBubbleOutline),
        ShortcutItem("Advanced Search", AppRoutes.SEARCH, Icons.Outlined.Task)
    )

    if (showPostDialog) {
        AlertDialog(
            onDismissRequest = { showPostDialog = false },
            title = { Text("Post your project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(postTitle, { postTitle = it }, label = { Text("Project title") })
                    OutlinedTextField(postStack, { postStack = it }, label = { Text("Tech stack") })
                    OutlinedTextField(postDescription, { postDescription = it }, label = { Text("Description") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (postTitle.isNotBlank()) {
                        posts.add(
                            0,
                            ProjectPost(
                                title = postTitle,
                                stack = postStack.ifBlank { "General Stack" },
                                description = postDescription.ifBlank { "Project posted by you." },
                                owner = "You",
                                openRoles = listOf("Mobile", "Backend", "UI/UX"),
                                spotsLeft = 3
                            )
                        )
                        postTitle = ""
                        postStack = ""
                        postDescription = ""
                        showPostDialog = false
                    }
                }) { Text("Publish") }
            },
            dismissButton = { TextButton(onClick = { showPostDialog = false }) { Text("Cancel") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            GradientHeroCard(
                title = content?.heroTitle ?: "Developer Command Center",
                subtitle = content?.heroSubtitle ?: "Build, post, and collaborate in realtime.",
                progress = "Network pulse active"
            )
        }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            content?.greeting ?: "Welcome back",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("Post your project and attract developers.", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = {
                        onRefresh()
                        showPostDialog = true
                    }) {
                        Icon(Icons.Outlined.Add, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Post")
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                content?.stats?.forEach { stat ->
                    ElevatedCard(
                        modifier = Modifier.width(170.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(stat.label, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
                                Text(stat.value, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall)
                                Text(stat.trend, color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
        item { SectionTitle("Quick Access") }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shortcuts.forEach { shortcut ->
                    AssistChip(
                        onClick = { navController.navigate(shortcut.route) },
                        label = { Text(shortcut.label) },
                        leadingIcon = { Icon(shortcut.icon, null) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        )
                    )
                }
            }
        }
        item { SectionTitle("Developer Project Feed") }
        items(posts) { post ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(post.title, style = MaterialTheme.typography.titleMedium)
                    Text("by ${post.owner}", style = MaterialTheme.typography.bodySmall)
                    Text(post.stack, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    Text(post.description, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { }) { Text("Join Project") }
                        TextButton(onClick = { }) { Text("Message Owner") }
                    }
                }
            }
        }
    }
}
/*
package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.domain.model.ProjectPost
import com.example.developernetworkingapp.ui.components.GradientHeroCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.data.ShortcutItem
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.DashboardUiState
import com.example.developernetworkingapp.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardRoute(
    padding: PaddingValues,
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(padding, navController, state, viewModel::loadDashboard)
}

@Composable
fun DashboardScreen(
    padding: PaddingValues,
    navController: NavController,
    state: DashboardUiState,
    onRefresh: () -> Unit
) {
    val content = state.content
    val posts = remember(content?.projectPosts) { mutableStateListOf(*(content?.projectPosts ?: emptyList()).toTypedArray()) }
    var showPostDialog by rememberSaveable { mutableStateOf(false) }
    var postTitle by rememberSaveable { mutableStateOf("") }
    var postStack by rememberSaveable { mutableStateOf("") }
    var postDescription by rememberSaveable { mutableStateOf("") }

    val shortcuts = listOf(
        ShortcutItem("Task Board", AppRoutes.TASKS, Icons.Outlined.Task),
        ShortcutItem("Live Events", AppRoutes.EVENTS, Icons.Outlined.Event),
        ShortcutItem("Chat Hub", AppRoutes.CHAT, Icons.Outlined.ChatBubbleOutline),
        ShortcutItem("Advanced Search", AppRoutes.SEARCH, Icons.Outlined.Task)
    )

    if (showPostDialog) {
        AlertDialog(
            onDismissRequest = { showPostDialog = false },
            title = { Text("Post your project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(postTitle, { postTitle = it }, label = { Text("Project title") })
                    OutlinedTextField(postStack, { postStack = it }, label = { Text("Tech stack") })
                    OutlinedTextField(postDescription, { postDescription = it }, label = { Text("Description") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (postTitle.isNotBlank()) {
                        posts.add(
                            0,
                            ProjectPost(
                                title = postTitle,
                                stack = postStack.ifBlank { "General Stack" },
                                description = postDescription.ifBlank { "Project posted by you." },
                                owner = "You",
                                openRoles = listOf("Mobile", "Backend", "UI/UX"),
                                spotsLeft = 3
                            )
                        )
                        postTitle = ""
                        postStack = ""
                        postDescription = ""
                        showPostDialog = false
                    }
                }) { Text("Publish") }
            },
            dismissButton = { TextButton(onClick = { showPostDialog = false }) { Text("Cancel") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            GradientHeroCard(
                title = content?.heroTitle ?: "Developer Command Center",
                subtitle = content?.heroSubtitle ?: "Build, post, and collaborate in realtime.",
                progress = "Network pulse active"
            )
        }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(content?.greeting ?: "Welcome back", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Post your project and attract developers.", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = {
                        onRefresh()
                        showPostDialog = true
                    }) {
                        Icon(Icons.Outlined.Add, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Post")
                    }
                }
            }
        }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                content?.stats?.forEach { stat ->
                    ElevatedCard(
                        modifier = Modifier.width(170.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(stat.label, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
                                Text(stat.value, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall)
                                Text(stat.trend, color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
        item { SectionTitle("Quick Access") }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                shortcuts.forEach { shortcut ->
                    AssistChip(
                        onClick = { navController.navigate(shortcut.route) },
                        label = { Text(shortcut.label) },
                        leadingIcon = { Icon(shortcut.icon, null) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                    )
                }
            }
        }
        item { SectionTitle("Developer Project Feed") }
        items(posts) { post ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(post.title, style = MaterialTheme.typography.titleMedium)
                    Text("by ${post.owner}", style = MaterialTheme.typography.bodySmall)
                    Text(post.stack, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    Text(post.description, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { }) { Text("Join Project") }
                        TextButton(onClick = { }) { Text("Message Owner") }
                    }
                }
            }
        }
    }
}
package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.domain.model.ProjectPost
import com.example.developernetworkingapp.ui.components.GradientHeroCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.data.ShortcutItem
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.DashboardUiState
import com.example.developernetworkingapp.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardRoute(
    padding: PaddingValues,
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(padding, navController, state, viewModel::loadDashboard)
}

@Composable
fun DashboardScreen(
    padding: PaddingValues,
    navController: NavController,
    state: DashboardUiState,
    onRefresh: () -> Unit
) {
    val content = state.content
    val posts = remember(content?.projectPosts) { mutableStateListOf(*(content?.projectPosts ?: emptyList()).toTypedArray()) }
    var showPostDialog by rememberSaveable { mutableStateOf(false) }
    var postTitle by rememberSaveable { mutableStateOf("") }
    var postStack by rememberSaveable { mutableStateOf("") }
    var postDescription by rememberSaveable { mutableStateOf("") }

    val shortcuts = listOf(
        ShortcutItem("Task Board", AppRoutes.TASKS, Icons.Outlined.Task),
        ShortcutItem("Live Events", AppRoutes.EVENTS, Icons.Outlined.Event),
        ShortcutItem("Chat Hub", AppRoutes.CHAT, Icons.Outlined.ChatBubbleOutline),
        ShortcutItem("Advanced Search", AppRoutes.SEARCH, Icons.Outlined.Task)
    )

    if (showPostDialog) {
        AlertDialog(
            onDismissRequest = { showPostDialog = false },
            title = { Text("Post your project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(postTitle, { postTitle = it }, label = { Text("Project title") })
                    OutlinedTextField(postStack, { postStack = it }, label = { Text("Tech stack") })
                    OutlinedTextField(postDescription, { postDescription = it }, label = { Text("Description") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (postTitle.isNotBlank()) {
                        posts.add(
                            0,
                            ProjectPost(
                                title = postTitle,
                                stack = postStack.ifBlank { "General Stack" },
                                description = postDescription.ifBlank { "Project posted by you." },
                                owner = "You",
                                openRoles = listOf("Mobile", "Backend", "UI/UX"),
                                spotsLeft = 3
                            )
                        )
                        postTitle = ""
                        postStack = ""
                        postDescription = ""
                        showPostDialog = false
                    }
                }) { Text("Publish") }
            },
            dismissButton = { TextButton(onClick = { showPostDialog = false }) { Text("Cancel") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            GradientHeroCard(
                title = content?.heroTitle ?: "Developer Command Center",
                subtitle = content?.heroSubtitle ?: "Build, post, and collaborate in realtime.",
                progress = "Network pulse active"
            )
        }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(content?.greeting ?: "Welcome back", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Post your project and attract developers.", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = {
                        onRefresh()
                        showPostDialog = true
                    }) {
                        Icon(Icons.Outlined.Add, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Post")
                    }
                }
            }
        }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                content?.stats?.forEach { stat ->
                    ElevatedCard(
                        modifier = Modifier.width(170.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(stat.label, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
                                Text(stat.value, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall)
                                Text(stat.trend, color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
        item { SectionTitle("Quick Access") }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                shortcuts.forEach { shortcut ->
                    AssistChip(
                        onClick = { navController.navigate(shortcut.route) },
                        label = { Text(shortcut.label) },
                        leadingIcon = { Icon(shortcut.icon, null) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                    )
                }
            }
        }
        item { SectionTitle("Developer Project Feed") }
        items(posts) { post ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(post.title, style = MaterialTheme.typography.titleMedium)
                    Text("by ${post.owner}", style = MaterialTheme.typography.bodySmall)
                    Text(post.stack, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    Text(post.description, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { }) { Text("Join Project") }
                        TextButton(onClick = { }) { Text("Message Owner") }
                    }
                }
            }
        }
    }
}
package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.domain.model.CollaboratorMatch
import com.example.developernetworkingapp.domain.model.EventHighlight
import com.example.developernetworkingapp.domain.model.FeatureModule
import com.example.developernetworkingapp.domain.model.NewsHighlight
import com.example.developernetworkingapp.domain.model.ProjectPost
import com.example.developernetworkingapp.ui.components.GradientHeroCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.data.ShortcutItem
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.DashboardUiState
import com.example.developernetworkingapp.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardRoute(
    padding: PaddingValues,
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(padding, navController, state, viewModel::loadDashboard)
}

@Composable
fun DashboardScreen(
    padding: PaddingValues,
    navController: NavController,
    state: DashboardUiState,
    onRefresh: () -> Unit
) {
    val content = state.content
    val posts = remember(content?.projectPosts) { mutableStateListOf(*(content?.projectPosts ?: emptyList()).toTypedArray()) }
    var detail by remember { mutableStateOf<DetailData?>(null) }
    var showPostDialog by rememberSaveable { mutableStateOf(false) }
    var postTitle by rememberSaveable { mutableStateOf("") }
    var postStack by rememberSaveable { mutableStateOf("") }
    var postDescription by rememberSaveable { mutableStateOf("") }

    val shortcuts = listOf(
        ShortcutItem("Task Board", AppRoutes.TASKS, Icons.Outlined.Task),
        ShortcutItem("Live Events", AppRoutes.EVENTS, Icons.Outlined.Event),
        ShortcutItem("Chat Hub", AppRoutes.CHAT, Icons.Outlined.ChatBubbleOutline),
        ShortcutItem("Advanced Search", AppRoutes.SEARCH, Icons.Outlined.Task)
    )

    if (showPostDialog) {
        AlertDialog(
            onDismissRequest = { showPostDialog = false },
            title = { Text("Post your project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(postTitle, { postTitle = it }, label = { Text("Project title") })
                    OutlinedTextField(postStack, { postStack = it }, label = { Text("Tech stack") })
                    OutlinedTextField(postDescription, { postDescription = it }, label = { Text("Description") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (postTitle.isNotBlank()) {
                        posts.add(
                            0,
                            ProjectPost(
                                title = postTitle,
                                stack = postStack.ifBlank { "General Stack" },
                                description = postDescription.ifBlank { "Project posted by you." },
                                owner = "You",
                                openRoles = listOf("Mobile", "Backend", "UI/UX"),
                                spotsLeft = 3
                            )
                        )
                        postTitle = ""
                        postStack = ""
                        postDescription = ""
                        showPostDialog = false
                    }
                }) { Text("Publish") }
            },
            dismissButton = { TextButton(onClick = { showPostDialog = false }) { Text("Cancel") } }
        )
    }

    detail?.let { d ->
        AlertDialog(
            onDismissRequest = { detail = null },
            title = { Text(d.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(d.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text(d.description)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        d.tags.forEach { tag ->
                            AssistChip(onClick = {}, label = { Text(tag) }, leadingIcon = { Icon(Icons.Outlined.Info, null) })
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { detail = null }) { Text(d.primaryActionLabel) } },
            dismissButton = { TextButton(onClick = { detail = null }) { Text("Close") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            GradientHeroCard(
                title = content?.heroTitle ?: "Developer Command Center",
                subtitle = content?.heroSubtitle ?: "Build, post, and collaborate in realtime.",
                progress = "Network pulse active"
            )
        }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(content?.greeting ?: "Welcome back", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Post your project and attract developers.", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = {
                        onRefresh()
                        showPostDialog = true
                    }) {
                        Icon(Icons.Outlined.Add, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Post")
                    }
                }
            }
        }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                content?.stats?.forEach { stat -> MetricCard(stat.label, stat.value, stat.trend) }
            }
        }
        item { SectionTitle("Quick Access") }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                shortcuts.forEach { shortcut ->
                    AssistChip(
                        onClick = { navController.navigate(shortcut.route) },
                        label = { Text(shortcut.label) },
                        leadingIcon = { Icon(shortcut.icon, null) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                    )
                }
            }
        }
        item { SectionTitle("Developer Project Feed") }
        items(posts) { post -> ProjectPostCard(post) }
        item { SectionTitle("Feature Modules") }
        items(content?.modules ?: emptyList()) { module -> ModuleCard(module) { detail = moduleDetail(module) } }
        item { SectionTitle("Suggested Collaborators") }
        items(content?.matches ?: emptyList()) { match -> CollaboratorCard(match) { detail = collaboratorDetail(match) } }
        item { SectionTitle("Live Hackathons") }
        items(content?.events ?: emptyList()) { event -> EventCard(event) { detail = eventDetail(event) } }
        item { SectionTitle("Tech News Feed") }
        items(content?.news ?: emptyList()) { news -> NewsCard(news) { detail = newsDetail(news) } }
    }
}

@Composable
private fun MetricCard(title: String, value: String, trend: String) {
    ElevatedCard(
        modifier = Modifier.width(170.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                Text(title, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
                Text(value, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall)
                Text(trend, color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ProjectPostCard(post: ProjectPost) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(post.title, style = MaterialTheme.typography.titleMedium)
                    Text("by ${post.owner}", style = MaterialTheme.typography.bodySmall)
                }
                Surface(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f), shape = RoundedCornerShape(20.dp)) {
                    Text("${post.spotsLeft} spots left", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(post.stack, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            Text(post.description, style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                post.openRoles.forEach { role -> AssistChip(onClick = {}, label = { Text(role) }) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { }) { Text("Join Project") }
                TextButton(onClick = { }) { Text("Message Owner") }
            }
        }
    }
}

@Composable
private fun ModuleCard(module: FeatureModule, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(module.title, style = MaterialTheme.typography.titleMedium)
            Text(module.subtitle, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDetails) { Text("View details") }
                TextButton(onClick = { }) { Text("Use feature") }
            }
        }
    }
}

@Composable
private fun CollaboratorCard(match: CollaboratorMatch, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${match.name} (${match.stack})", style = MaterialTheme.typography.titleMedium)
            Text("Match score: ${match.matchScore}%", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDetails) { Text("View") }
                TextButton(onClick = { }) { Text("Invite") }
            }
        }
    }
}

@Composable
private fun EventCard(event: EventHighlight, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            Text(event.meta, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDetails) { Text("View event") }
                TextButton(onClick = { }) { Text("Join now") }
            }
        }
    }
}

@Composable
private fun NewsCard(news: NewsHighlight, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(news.title, style = MaterialTheme.typography.titleMedium)
            Text(news.source, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = onDetails) { Text("Read more") }
        }
    }
}

private data class DetailData(
    val title: String,
    val subtitle: String,
    val description: String,
    val tags: List<String>,
    val primaryActionLabel: String
)

private fun moduleDetail(module: FeatureModule) = DetailData(
    title = module.title,
    subtitle = "Module",
    description = module.subtitle,
    tags = listOf("Feature", "Collaboration", "Live"),
    primaryActionLabel = "Explore"
)

private fun collaboratorDetail(match: CollaboratorMatch) = DetailData(
    title = match.name,
    subtitle = "Collaboration Match",
    description = "Stack: ${match.stack}\nMatch score: ${match.matchScore}%",
    tags = listOf("Invite", "Message", "Projects"),
    primaryActionLabel = "Invite"
)

private fun eventDetail(event: EventHighlight) = DetailData(
    title = event.title,
    subtitle = "Event",
    description = event.meta,
    tags = listOf("Team Match", "Leaderboard", "Challenges"),
    primaryActionLabel = "Join"
)

private fun newsDetail(news: NewsHighlight) = DetailData(
    title = news.title,
    subtitle = news.source,
    description = "Open this story and discuss it with collaborators.",
    tags = listOf("Tech", "Trending", "Discuss"),
    primaryActionLabel = "Open"
)
package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.domain.model.CollaboratorMatch
import com.example.developernetworkingapp.domain.model.EventHighlight
import com.example.developernetworkingapp.domain.model.FeatureModule
import com.example.developernetworkingapp.domain.model.NewsHighlight
import com.example.developernetworkingapp.domain.model.ProjectPost
import com.example.developernetworkingapp.ui.components.GradientHeroCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.data.ShortcutItem
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.DashboardUiState
import com.example.developernetworkingapp.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardRoute(
    padding: PaddingValues,
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(padding, navController, state, viewModel::loadDashboard)
}

@Composable
fun DashboardScreen(
    padding: PaddingValues,
    navController: NavController,
    state: DashboardUiState,
    onRefresh: () -> Unit
) {
    val content = state.content
    val posts = remember(content?.projectPosts) { mutableStateListOf(*(content?.projectPosts ?: emptyList()).toTypedArray()) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var selectedDetail by remember { mutableStateOf<DetailData?>(null) }

    var postTitle by rememberSaveable { mutableStateOf("") }
    var postStack by rememberSaveable { mutableStateOf("") }
    var postDescription by rememberSaveable { mutableStateOf("") }

    val shortcuts = listOf(
        ShortcutItem("Task Board", AppRoutes.TASKS, Icons.Outlined.Task),
        ShortcutItem("Live Events", AppRoutes.EVENTS, Icons.Outlined.Event),
        ShortcutItem("Chat Hub", AppRoutes.CHAT, Icons.Outlined.ChatBubbleOutline),
        ShortcutItem("Advanced Search", AppRoutes.SEARCH, Icons.Outlined.Task)
    )

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Post your project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(postTitle, { postTitle = it }, label = { Text("Project title") })
                    OutlinedTextField(postStack, { postStack = it }, label = { Text("Tech stack") })
                    OutlinedTextField(postDescription, { postDescription = it }, label = { Text("Description") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (postTitle.isNotBlank()) {
                        posts.add(
                            0,
                            ProjectPost(
                                title = postTitle,
                                stack = postStack.ifBlank { "General Stack" },
                                description = postDescription.ifBlank { "Project posted by you." },
                                owner = "You",
                                openRoles = listOf("Mobile", "Backend", "UI/UX"),
                                spotsLeft = 3
                            )
                        )
                        postTitle = ""
                        postStack = ""
                        postDescription = ""
                        showCreateDialog = false
                    }
                }) { Text("Publish") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
        )
    }

    selectedDetail?.let { detail ->
        AlertDialog(
            onDismissRequest = { selectedDetail = null },
            title = { Text(detail.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(detail.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text(detail.description)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        detail.tags.forEach { tag ->
                            AssistChip(onClick = {}, label = { Text(tag) }, leadingIcon = { Icon(Icons.Outlined.Info, null) })
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { selectedDetail = null }) { Text(detail.primaryActionLabel) } },
            dismissButton = { TextButton(onClick = { selectedDetail = null }) { Text("Close") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            GradientHeroCard(
                title = content?.heroTitle ?: "Developer Command Center",
                subtitle = content?.heroSubtitle ?: "Build, post, and collaborate in realtime.",
                progress = "Network pulse active"
            )
        }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(content?.greeting ?: "Welcome back", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Post your project and attract developers.", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = {
                        onRefresh()
                        showCreateDialog = true
                    }) {
                        Icon(Icons.Outlined.Add, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Post")
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                content?.stats?.forEach { stat ->
                    MetricCard(stat.label, stat.value, stat.trend)
                }
            }
        }

        item { SectionTitle("Quick Access") }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                shortcuts.forEach { shortcut ->
                    AssistChip(
                        onClick = { navController.navigate(shortcut.route) },
                        label = { Text(shortcut.label) },
                        leadingIcon = { Icon(shortcut.icon, null) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                    )
                }
            }
        }

        item { SectionTitle("Developer Project Feed") }
        items(posts) { post ->
            ProjectPostCard(post = post)
        }

        item { SectionTitle("Feature Modules") }
        items(content?.modules ?: emptyList()) { module ->
            InteractiveModuleCard(module) {
                selectedDetail = DetailData(
                    title = module.title,
                    subtitle = "Module",
                    description = module.subtitle,
                    tags = listOf("Feature", "Collaboration", "Live"),
                    primaryActionLabel = "Explore"
                )
            }
        }

        item { SectionTitle("Suggested Collaborators") }
        items(content?.matches ?: emptyList()) { match ->
            CollaboratorCard(match) {
                selectedDetail = collaboratorDetail(match)
            }
        }

        item { SectionTitle("Live Hackathons") }
        items(content?.events ?: emptyList()) { event ->
            EventCard(event) {
                selectedDetail = eventDetail(event)
            }
        }

        item { SectionTitle("Tech News Feed") }
        items(content?.news ?: emptyList()) { news ->
            NewsCard(news) {
                selectedDetail = newsDetail(news)
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, trend: String) {
    ElevatedCard(
        modifier = Modifier.width(170.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                Text(title, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
                Text(value, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall)
                Text(trend, color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ProjectPostCard(post: ProjectPost) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(post.title, style = MaterialTheme.typography.titleMedium)
                    Text("by ${post.owner}", style = MaterialTheme.typography.bodySmall)
                }
                Surface(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f), shape = RoundedCornerShape(20.dp)) {
                    Text(
                        "${post.spotsLeft} spots left",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(post.stack, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            Text(post.description, style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                post.openRoles.forEach { role ->
                    AssistChip(onClick = {}, label = { Text(role) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { }) { Text("Join Project") }
                TextButton(onClick = { }) { Text("Message Owner") }
            }
        }
    }
}

@Composable
private fun InteractiveModuleCard(module: FeatureModule, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(module.title, style = MaterialTheme.typography.titleMedium)
            Text(module.subtitle, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDetails) { Text("View details") }
                TextButton(onClick = { }) { Text("Use feature") }
            }
        }
    }
}

@Composable
private fun CollaboratorCard(match: CollaboratorMatch, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${match.name} (${match.stack})", style = MaterialTheme.typography.titleMedium)
            Text("Match score: ${match.matchScore}%", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDetails) { Text("View") }
                TextButton(onClick = { }) { Text("Invite") }
            }
        }
    }
}

@Composable
private fun EventCard(event: EventHighlight, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            Text(event.meta, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDetails) { Text("View event") }
                TextButton(onClick = { }) { Text("Join now") }
            }
        }
    }
}

@Composable
private fun NewsCard(news: NewsHighlight, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(news.title, style = MaterialTheme.typography.titleMedium)
            Text(news.source, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = onDetails) { Text("Read more") }
        }
    }
}

private data class DetailData(
    val title: String,
    val subtitle: String,
    val description: String,
    val tags: List<String>,
    val primaryActionLabel: String
)

private fun collaboratorDetail(match: CollaboratorMatch) = DetailData(
    title = match.name,
    subtitle = "Collaboration Match",
    description = "Stack: ${match.stack}\nMatch score: ${match.matchScore}%",
    tags = listOf("Invite", "Message", "Projects"),
    primaryActionLabel = "Invite to Project"
)

private fun eventDetail(event: EventHighlight) = DetailData(
    title = event.title,
    subtitle = "Event",
    description = event.meta,
    tags = listOf("Team Match", "Leaderboard", "Challenges"),
    primaryActionLabel = "Join Event"
)

private fun newsDetail(news: NewsHighlight) = DetailData(
    title = news.title,
    subtitle = news.source,
    description = "Open this story and discuss it with your collaborators.",
    tags = listOf("Tech", "Trending", "Discuss"),
    primaryActionLabel = "Open Article"
)
package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.domain.model.ActivityItem
import com.example.developernetworkingapp.domain.model.CollaboratorMatch
import com.example.developernetworkingapp.domain.model.EventHighlight
import com.example.developernetworkingapp.domain.model.FeatureModule
import com.example.developernetworkingapp.domain.model.NewsHighlight
import com.example.developernetworkingapp.domain.model.ProjectPost
import com.example.developernetworkingapp.ui.components.GradientHeroCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.data.ShortcutItem
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.DashboardUiState
import com.example.developernetworkingapp.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardRoute(
    padding: PaddingValues,
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(padding, navController, state, viewModel::loadDashboard)
}

@Composable
fun DashboardScreen(
    padding: PaddingValues,
    navController: NavController,
    state: DashboardUiState,
    onRefresh: () -> Unit
) {
    val content = state.content
    val posts = remember(content?.projectPosts) {
        mutableStateListOf(*(content?.projectPosts ?: emptyList()).toTypedArray())
    }
    var detailSheet by remember { mutableStateOf<DetailSheetData?>(null) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var postTitle by rememberSaveable { mutableStateOf("") }
    var postStack by rememberSaveable { mutableStateOf("") }
    var postDescription by rememberSaveable { mutableStateOf("") }

    val shortcuts = listOf(
        ShortcutItem("Task Board", AppRoutes.TASKS, Icons.Outlined.Task),
        ShortcutItem("Live Events", AppRoutes.EVENTS, Icons.Outlined.Event),
        ShortcutItem("Chat Hub", AppRoutes.CHAT, Icons.Outlined.ChatBubbleOutline),
        ShortcutItem("Advanced Search", AppRoutes.SEARCH, Icons.Outlined.Task)
    )

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Post your project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(postTitle, { postTitle = it }, label = { Text("Project title") })
                    OutlinedTextField(postStack, { postStack = it }, label = { Text("Tech stack") })
                    OutlinedTextField(postDescription, { postDescription = it }, label = { Text("Description") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (postTitle.isNotBlank()) {
                        posts.add(
                            0,
                            ProjectPost(
                                title = postTitle,
                                stack = postStack.ifBlank { "General" },
                                description = postDescription.ifBlank { "New project posted by you." },
                                owner = "You",
                                openRoles = listOf("Mobile", "Backend", "UI/UX"),
                                spotsLeft = 3
                            )
                        )
                        postTitle = ""
                        postStack = ""
                        postDescription = ""
                        showCreateDialog = false
                    }
                }) { Text("Publish") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
        )
    }

    detailSheet?.let { data ->
        AlertDialog(
            onDismissRequest = { detailSheet = null },
            title = { Text(data.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(data.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text(data.description, style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        data.tags.forEach { tag ->
                            AssistChip(onClick = {}, label = { Text(tag) }, leadingIcon = { Icon(Icons.Outlined.Info, null) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    detailSheet = null
                    data.primaryAction()
                }) { Text(data.primaryLabel) }
            },
            dismissButton = { TextButton(onClick = { detailSheet = null }) { Text("Close") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            GradientHeroCard(
                title = content?.heroTitle ?: "Building your personalized feed",
                subtitle = content?.heroSubtitle ?: "Loading collaboration intelligence...",
                progress = "Creator feed + matching + realtime discovery active"
            )
        }
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(content?.greeting ?: "Welcome back", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Share projects and attract teammates.", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = {
                        onRefresh()
                        showCreateDialog = true
                    }) {
                        Icon(Icons.Outlined.Add, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Post Project")
                    }
                }
            }
        }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                content?.stats?.forEach { stat ->
                    MetricGradientCard(stat.label, stat.value, stat.trend)
                }
            }
        }
        item { SectionTitle("Quick Access") }
        item { ShortcutRow(shortcuts, navController) }

        item { SectionTitle("Developer Project Feed") }
        items(posts) { post -> ProjectPostCard(post) }

        item { SectionTitle("Feature Modules") }
        items(content?.modules ?: emptyList()) { module ->
            InteractiveGradientCard(module) {
                detailSheet = DetailSheetData(
                    title = module.title,
                    subtitle = "Platform Capability",
                    description = module.subtitle,
                    tags = listOf("Collaboration", "Live", "Productivity"),
                    primaryLabel = "Open Module",
                    primaryAction = {}
                )
            }
        }

        item { SectionTitle("Suggested Collaborators") }
        items(content?.matches ?: emptyList()) { match ->
            MatchCard(match) { detailSheet = collaboratorDetails(match) }
        }

        item { SectionTitle("Active Projects") }
        items(content?.projects ?: emptyList()) { project ->
            ProjectProgressCard(project.title, project.description, project.progress) {
                detailSheet = DetailSheetData(
                    title = project.title,
                    subtitle = "Project Progress",
                    description = "${project.description}\nCurrent completion: ${project.progress}%",
                    tags = listOf("Roadmap", "Tasks", "Team"),
                    primaryLabel = "Open Project",
                    primaryAction = {}
                )
            }
        }

        item { SectionTitle("Realtime Activity") }
        items(content?.activity ?: emptyList()) { activity ->
            ActivityCard(activity) { detailSheet = activityDetails(activity) }
        }

        item { SectionTitle("Live Hackathons") }
        items(content?.events ?: emptyList()) { event ->
            EventCard(event) { detailSheet = eventDetails(event) }
        }

        item { SectionTitle("Tech News Feed") }
        items(content?.news ?: emptyList()) { news ->
            NewsCard(news) { detailSheet = newsDetails(news) }
        }
    }
}

@Composable
private fun MetricGradientCard(title: String, value: String, trend: String) {
    ElevatedCard(
        modifier = Modifier.width(170.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
                Text(value, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall)
                Text(trend, color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ShortcutRow(shortcuts: List<ShortcutItem>, navController: NavController) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        shortcuts.forEach { shortcut ->
            AssistChip(
                onClick = { navController.navigate(shortcut.route) },
                label = { Text(shortcut.label) },
                leadingIcon = { Icon(shortcut.icon, null) },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
private fun ProjectPostCard(post: ProjectPost) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(post.title, style = MaterialTheme.typography.titleMedium)
                    Text("by ${post.owner}", style = MaterialTheme.typography.bodySmall)
                }
                Surface(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f), shape = RoundedCornerShape(24.dp)) {
                    Text(
                        "${post.spotsLeft} spots left",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text(post.stack, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Text(post.description, style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                post.openRoles.forEach { role -> AssistChip(onClick = {}, label = { Text(role) }) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {}) { Text("Join Project") }
                TextButton(onClick = {}) { Text("Message Owner") }
            }
        }
    }
}

@Composable
private fun InteractiveGradientCard(module: FeatureModule, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(module.title, style = MaterialTheme.typography.titleMedium)
                Text(module.subtitle, style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onDetails) { Text("Explore") }
                    TextButton(onClick = {}) { Text("Join") }
                }
            }
        }
    }
}

@Composable
private fun MatchCard(match: CollaboratorMatch, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${match.name} (${match.stack})", style = MaterialTheme.typography.titleMedium)
            Text("Match score: ${match.matchScore}%", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDetails) { Text("View profile") }
                TextButton(onClick = {}) { Text("Invite") }
            }
        }
    }
}

@Composable
private fun ProjectProgressCard(title: String, description: String, progress: Int, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall)
            Text("Progress: $progress%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDetails) { Text("View details") }
                TextButton(onClick = {}) { Text("Join team") }
            }
        }
    }
}

@Composable
private fun ActivityCard(item: ActivityItem, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Text(item.time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = onDetails) { Text("Open activity") }
        }
    }
}

@Composable
private fun EventCard(event: EventHighlight, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            Text(event.meta, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDetails) { Text("View event") }
                TextButton(onClick = {}) { Text("Join now") }
            }
        }
    }
}

@Composable
private fun NewsCard(news: NewsHighlight, onDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(news.title, style = MaterialTheme.typography.titleMedium)
            Text(news.source, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = onDetails) { Text("Read more") }
        }
    }
}

private data class DetailSheetData(
    val title: String,
    val subtitle: String,
    val description: String,
    val tags: List<String>,
    val primaryLabel: String,
    val primaryAction: () -> Unit
)

private fun collaboratorDetails(match: CollaboratorMatch) = DetailSheetData(
    title = match.name,
    subtitle = "Collaboration Match",
    description = "Stack: ${match.stack}\nMatch Score: ${match.matchScore}%",
    tags = listOf("Invite", "Message", "Projects"),
    primaryLabel = "Invite to Project",
    primaryAction = {}
)

private fun activityDetails(item: ActivityItem) = DetailSheetData(
    title = "Activity Detail",
    subtitle = item.time,
    description = item.title,
    tags = listOf("Timeline", "Project", "Mentions"),
    primaryLabel = "Open Thread",
    primaryAction = {}
)

private fun eventDetails(event: EventHighlight) = DetailSheetData(
    title = event.title,
    subtitle = "Hackathon / Event",
    description = event.meta,
    tags = listOf("Team Match", "Leaderboard", "Challenges"),
    primaryLabel = "Join Event",
    primaryAction = {}
)

private fun newsDetails(news: NewsHighlight) = DetailSheetData(
    title = news.title,
    subtitle = news.source,
    description = "Open this story to track updates and discuss with collaborators.",
    tags = listOf("Tech", "Trending", "Discuss"),
    primaryLabel = "Open Article",
    primaryAction = {}
)
package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.domain.model.ActivityItem
import com.example.developernetworkingapp.domain.model.CollaboratorMatch
import com.example.developernetworkingapp.domain.model.EventHighlight
import com.example.developernetworkingapp.domain.model.FeatureModule
import com.example.developernetworkingapp.domain.model.NewsHighlight
import com.example.developernetworkingapp.ui.components.GradientHeroCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.domain.model.ProjectPost
import com.example.developernetworkingapp.ui.data.ShortcutItem
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.DashboardUiState
import com.example.developernetworkingapp.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardRoute(
    padding: PaddingValues,
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        padding = padding,
        navController = navController,
        state = state,
        onRefresh = viewModel::loadDashboard
    )
}

@Composable
fun DashboardScreen(
    padding: PaddingValues,
    navController: NavController,
    state: DashboardUiState,
    onRefresh: () -> Unit
) {
    val content = state.content
    val posts = remember(content?.projectPosts) { mutableStateListOf(*(content?.projectPosts ?: emptyList()).toTypedArray()) }
    var selectedDetails by remember { mutableStateOf<DetailSheetData?>(null) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var postTitle by rememberSaveable { mutableStateOf("") }
    var postStack by rememberSaveable { mutableStateOf("") }
    var postDescription by rememberSaveable { mutableStateOf("") }
    val shortcuts = listOf(
        ShortcutItem("Task Board", AppRoutes.TASKS, Icons.Outlined.Task),
        ShortcutItem("Live Events", AppRoutes.EVENTS, Icons.Outlined.Event),
        ShortcutItem("Chat Hub", AppRoutes.CHAT, Icons.Outlined.ChatBubbleOutline),
        ShortcutItem("Advanced Search", AppRoutes.SEARCH, Icons.Outlined.Task)
    )

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Post your project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = postTitle, onValueChange = { postTitle = it }, label = { Text("Project title") })
                    OutlinedTextField(value = postStack, onValueChange = { postStack = it }, label = { Text("Tech stack") })
                    OutlinedTextField(value = postDescription, onValueChange = { postDescription = it }, label = { Text("What are you building?") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postTitle.isNotBlank()) {
                            posts.add(
                                0,
                                ProjectPost(
                                    title = postTitle,
                                    stack = postStack.ifBlank { "General Stack" },
                                    description = postDescription.ifBlank { "New project posted by you." },
                                    owner = "You",
                                    openRoles = listOf("Mobile", "Backend", "UI/UX"),
                                    spotsLeft = 3
                                )
                            )
                            postTitle = ""
                            postStack = ""
                            postDescription = ""
                            showCreateDialog = false
                        }
                    }
                ) { Text("Publish") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    selectedDetails?.let { data ->
        AlertDialog(
            onDismissRequest = { selectedDetails = null },
            title = { Text(data.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(data.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text(data.description, style = MaterialTheme.typography.bodyMedium)
                    data.tags.forEach { tag ->
                        AssistChip(onClick = {}, label = { Text(tag) }, leadingIcon = { Icon(Icons.Outlined.Info, null) })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedDetails = null
                        data.primaryAction()
                    }
                ) { Text(data.primaryLabel) }
            },
            dismissButton = {
                TextButton(onClick = { selectedDetails = null }) { Text("Close") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            GradientHeroCard(
                title = content?.heroTitle ?: "Building your personalized feed",
                subtitle = content?.heroSubtitle ?: "Loading collaboration intelligence...",
                progress = "Your creator feed, matching engine, and live project discovery are active"
            )
        }
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = content?.greeting ?: "Preparing your dashboard...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Create a post, attract developers, and grow your active project community.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(onClick = {
                        onRefresh()
                        showCreateDialog = true
                    }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Post project")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Post Project")
                    }
                }
            }
        }
        item {
            BoxWithConstraints {
                val cardWidth = if (maxWidth > 600.dp) 0.24f else 0.48f
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    content?.stats?.forEach { stat ->
                        MetricGradientCard(
                            title = stat.label,
                            value = stat.value,
                            trend = stat.trend,
                            modifier = Modifier
                                .fillMaxWidth(cardWidth)
                                .width(170.dp)
                        )
                    }
                }
            }
        }
        item { SectionTitle("Quick Access") }
        item { ShortcutRow(shortcuts = shortcuts, navController = navController) }
        item { SectionTitle("Post Something New") }
        item {
            FeedComposerCard(
                onPostClick = { showCreateDialog = true }
            )
        }
        item { SectionTitle("Developer Project Feed") }
        items(posts) { post ->
            ProjectPostCard(post = post)
        }
        item { SectionTitle("Feature Modules") }
        items(content?.modules ?: emptyList()) { module ->
            InteractiveGradientCard(
                title = module.title,
                subtitle = module.subtitle,
                primaryLabel = "Explore module",
                onPrimaryClick = {
                    selectedDetails = DetailSheetData(
                        title = module.title,
                        subtitle = "Core platform capability",
                        description = module.subtitle,
                        tags = listOf("Productivity", "Collaboration", "Live"),
                        primaryLabel = "View Module",
                        primaryAction = {}
                    )
                }
            )
        }
        item { SectionTitle("Suggested Collaborators") }
        items(content?.matches ?: emptyList()) { match ->
            MatchCard(
                title = "${match.name} (${match.stack})",
                subtitle = "Match score: ${match.matchScore}% - Invite or start 1:1 chat"
            ) {
                selectedDetails = collaboratorDetails(match)
            )
        }
        item { SectionTitle("Active Projects") }
        items(content?.projects ?: emptyList()) { project ->
            ProjectProgressCard(
                project.title,
                project.description,
                project.progress
            ) {
                selectedDetails = DetailSheetData(
                    title = project.title,
                    subtitle = "Project Progress",
                    description = "${project.description}\n\nCurrent completion: ${project.progress}%",
                    tags = listOf("Roadmap", "Tasks", "Team"),
                    primaryLabel = "Open Project",
                    primaryAction = {}
                )
            }
        }
        item { SectionTitle("Realtime Activity") }
        items(content?.activity ?: emptyList()) { item ->
            ActivityCard(item) {
                selectedDetails = activityDetails(item)
            }
        }
        item { SectionTitle("Live Hackathons") }
        items(content?.events ?: emptyList()) { event ->
            EventCard(event) {
                selectedDetails = eventDetails(event)
            }
        }
        item { SectionTitle("Tech News Feed") }
        items(content?.news ?: emptyList()) { news ->
            NewsCard(news) {
                selectedDetails = newsDetails(news)
            }
        }
    }
}

@Composable
private fun MetricGradientCard(
    title: String,
    value: String,
    trend: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
                Text(value, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall)
                Text(trend, color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ProjectProgressCard(
    title: String,
    description: String,
    progress: Int,
    onViewClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall)
            Text("Progress: $progress%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onViewClick) { Text("View details") }
                TextButton(onClick = { }) { Text("Join team") }
            }
        }
    }
}

@Composable
private fun ShortcutRow(shortcuts: List<ShortcutItem>, navController: NavController) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        shortcuts.forEach { shortcut ->
            AssistChip(
                onClick = { navController.navigate(shortcut.route) },
                label = { Text(shortcut.label) },
                leadingIcon = { Icon(imageVector = shortcut.icon, contentDescription = shortcut.label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun FeedComposerCard(
    onPostClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Share what you're building", style = MaterialTheme.typography.titleMedium)
            Text(
                "Post a startup idea, project roadmap, or open developer role so others can join your stack.",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onPostClick, label = { Text("Post a project") }, leadingIcon = { Icon(Icons.Outlined.Add, null) })
                AssistChip(onClick = { }, label = { Text("Need backend dev") }, leadingIcon = { Icon(Icons.Outlined.Groups, null) })
            }
        }
    }
}

@Composable
private fun ProjectPostCard(post: ProjectPost) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(post.title, style = MaterialTheme.typography.titleMedium)
                    Text("by ${post.owner}", style = MaterialTheme.typography.bodySmall)
                }
                Surface(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "${post.spotsLeft} spots left",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(post.stack, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            Text(post.description, style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                post.openRoles.forEach { role ->
                    AssistChip(onClick = {}, label = { Text(role) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { }) { Text("Join Project") }
                TextButton(onClick = { }) { Text("Message Owner") }
            }
        }
    }
}

@Composable
private fun InteractiveGradientCard(
    title: String,
    subtitle: String,
    primaryLabel: String,
    onPrimaryClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onPrimaryClick) { Text(primaryLabel) }
                    TextButton(onClick = { }) { Text("Join") }
                }
            }
        }
    }
}

@Composable
private fun MatchCard(title: String, subtitle: String, onViewClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onViewClick) { Text("View profile") }
                TextButton(onClick = { }) { Text("Invite") }
            }
        }
    }
}

@Composable
private fun ActivityCard(item: ActivityItem, onViewClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Text(item.time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = onViewClick) { Text("Open activity") }
        }
    }
}

@Composable
private fun EventCard(event: EventHighlight, onViewClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            Text(event.meta, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onViewClick) { Text("View event") }
                TextButton(onClick = { }) { Text("Join now") }
            }
        }
    }
}

@Composable
private fun NewsCard(news: NewsHighlight, onViewClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(news.title, style = MaterialTheme.typography.titleMedium)
            Text(news.source, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = onViewClick) { Text("Read more") }
        }
    }
}

private data class DetailSheetData(
    val title: String,
    val subtitle: String,
    val description: String,
    val tags: List<String>,
    val primaryLabel: String,
    val primaryAction: () -> Unit
)

private fun collaboratorDetails(match: CollaboratorMatch) = DetailSheetData(
    title = match.name,
    subtitle = "Collaboration Match",
    description = "Stack: ${match.stack}\n\nMatch Score: ${match.matchScore}%\n\nSuggested actions: invite to project, start direct chat, or connect for hackathons.",
    tags = listOf("Invite", "Message", "Projects"),
    primaryLabel = "Invite to Project",
    primaryAction = {}
)

private fun activityDetails(item: ActivityItem) = DetailSheetData(
    title = "Activity Detail",
    subtitle = item.time,
    description = item.title,
    tags = listOf("Timeline", "Project", "Mentions"),
    primaryLabel = "Open Thread",
    primaryAction = {}
)

private fun eventDetails(event: EventHighlight) = DetailSheetData(
    title = event.title,
    subtitle = "Hackathon / Event",
    description = event.meta,
    tags = listOf("Team Match", "Leaderboard", "Challenges"),
    primaryLabel = "Join Event",
    primaryAction = {}
)

private fun newsDetails(news: NewsHighlight) = DetailSheetData(
    title = news.title,
    subtitle = news.source,
    description = "Open this story to track updates and discuss with collaborators in your project feed.",
    tags = listOf("Tech", "Trending", "Discuss"),
    primaryLabel = "Open Article",
    primaryAction = {}
)
*/
