package com.example.developernetworkingapp.ui.screens

// ...existing imports...
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.developernetworkingapp.domain.model.NewsHighlight
import com.example.developernetworkingapp.domain.model.ProjectPost
import com.example.developernetworkingapp.ui.components.GradientHeroCard
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.data.ShortcutItem
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.DashboardUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.DashboardViewModel
import com.example.developernetworkingapp.ui.theme.VibrantOrange
import com.example.developernetworkingapp.ui.theme.ElectricCyan
import com.example.developernetworkingapp.ui.theme.ElectricGreen
import kotlin.math.absoluteValue

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
    val posts = remember(content?.projectPosts) {
        mutableStateListOf(*(content?.projectPosts ?: emptyList()).toTypedArray())
    }
    var composerText by rememberSaveable { mutableStateOf("") }
    var composerStack by rememberSaveable { mutableStateOf("") }
    var composerBackendNeed by rememberSaveable { mutableStateOf("") }
    var composerSpotsInput by rememberSaveable { mutableStateOf("3") }
    var showInviteDialog by rememberSaveable { mutableStateOf(false) }
    var selectedCollaborator by remember { mutableStateOf<CollaboratorMatch?>(null) }
    var showJoinProjectDialog by rememberSaveable { mutableStateOf(false) }
    var selectedProject by remember { mutableStateOf<String?>(null) }
    var notificationMessage by rememberSaveable { mutableStateOf("") }
    var showNotification by rememberSaveable { mutableStateOf(false) }
    val shortcuts = listOf(
        ShortcutItem("Task Board", AppRoutes.TASKS, Icons.Outlined.Task),
        ShortcutItem("Live Events", AppRoutes.EVENTS, Icons.Outlined.Event),
        ShortcutItem("Chat Hub", AppRoutes.CHAT, Icons.Outlined.ChatBubbleOutline),
        ShortcutItem("Advanced Search", AppRoutes.SEARCH, Icons.Outlined.Task)
    )

    // Notification Display
    if (showNotification) {
        LaunchedEffect(showNotification) {
            kotlinx.coroutines.delay(4000)
            showNotification = false
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ElectricGreen.copy(alpha = 0.9f),
                tonalElevation = 8.dp,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        notificationMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { showNotification = false }) {
                        Text("✕", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    if (showInviteDialog && selectedCollaborator != null) {
        InviteCollaboratorDialog(
            collaborator = selectedCollaborator!!,
            onDismissRequest = { showInviteDialog = false; selectedCollaborator = null }
        )
    }

    if (showJoinProjectDialog && selectedProject != null) {
        JoinProjectTeamDialog(
            projectTitle = selectedProject!!,
            onDismissRequest = { showJoinProjectDialog = false; selectedProject = null },
            onSubmit = {
                notificationMessage = "✓ Application submitted! We'll notify you when the project owner reviews your request."
                showNotification = true
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
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
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
                shape = AppDesignTokens.cardShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
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
                        notificationMessage = "Feed refreshed with latest activity."
                        showNotification = true
                    }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Post project")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Refresh Feed")
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
                    MetricGradientCard(
                        title = stat.label,
                        value = stat.value,
                        trend = stat.trend,
                        modifier = Modifier
                            .width(170.dp)
                    )
                }
            }
        }
        item { SectionTitle("Quick Access") }
        item { ShortcutRow(shortcuts = shortcuts, navController = navController) }
        item { SectionTitle("Post Something New") }
        item {
            FeedComposerCard(
                composerText = composerText,
                onComposerTextChange = { composerText = it },
                stack = composerStack,
                onStackChange = { composerStack = it },
                backendStackNeed = composerBackendNeed,
                onBackendStackNeedChange = { composerBackendNeed = it },
                spotsInput = composerSpotsInput,
                onSpotsInputChange = { composerSpotsInput = it.filter { ch -> ch.isDigit() }.take(2) },
                onPostProject = {
                    if (composerText.isBlank()) {
                        notificationMessage = "Add a short project update before posting."
                        showNotification = true
                    } else {
                        val spots = composerSpotsInput.toIntOrNull()?.coerceIn(1, 20) ?: 3
                        val backendNeed = composerBackendNeed.takeIf { it.isNotBlank() } ?: "Backend (any stack)"
                        posts.add(
                            0,
                            ProjectPost(
                                title = composerText.take(48),
                                stack = composerStack.ifBlank { "General Stack" },
                                description = composerText,
                                owner = "You",
                                openRoles = listOf("Mobile", backendNeed, "UI/UX"),
                                spotsLeft = spots
                            )
                        )
                        composerText = ""
                        composerStack = ""
                        composerBackendNeed = ""
                        composerSpotsInput = "3"
                        notificationMessage = "Project update posted to your feed."
                        showNotification = true
                    }
                }
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
                onPrimaryClick = { navController.navigate(modulePrimaryRoute(module.title)) },
                onSecondaryClick = { navController.navigate(moduleSecondaryRoute(module.title)) }
            )
        }
        item { SectionTitle("Suggested Collaborators") }
        items(content?.matches ?: emptyList()) { match ->
            MatchCard(
                title = "${match.name} (${match.stack})",
                subtitle = "Match score: ${match.matchScore}% - Invite or start 1:1 chat",
                onViewClick = {
                    navController.navigate(
                        AppRoutes.collaboratorProfileRoute(
                            name = match.name,
                            stack = match.stack,
                            score = match.matchScore
                        )
                    )
                },
                onInviteClick = {
                    selectedCollaborator = match
                    showInviteDialog = true
                }
            )
        }
        item { SectionTitle("Active Projects") }
        items(content?.projects ?: emptyList()) { project ->
            ProjectProgressCard(
                project.title,
                project.description,
                project.progress,
                onViewClick = { navController.navigate(AppRoutes.PROJECTS) },
                onJoinClick = {
                    selectedProject = project.title
                    showJoinProjectDialog = true
                }
            )
        }
        item { SectionTitle("Realtime Activity") }
        items(content?.activity ?: emptyList()) { item ->
            ActivityCard(item) {
                navController.navigate(AppRoutes.NOTIFICATIONS)
            }
        }
        item { SectionTitle("Live Hackathons") }
        items(content?.events ?: emptyList()) { event ->
            EventCard(event) {
                navController.navigate(AppRoutes.EVENTS)
            }
        }
        item { SectionTitle("Tech News Feed") }
        items(content?.news ?: emptyList()) { news ->
            NewsCard(news) {
                navController.navigate(AppRoutes.SEARCH)
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
        shape = RoundedCornerShape(20.dp),
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
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
                Text(value, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall)
                Text(trend, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ProjectProgressCard(
    title: String,
    description: String,
    progress: Int,
    onViewClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = AppDesignTokens.cardShape
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Text("Progress: $progress%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onViewClick) { Text("View details") }
                TextButton(onClick = onJoinClick) { Text("Join team") }
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
    composerText: String,
    onComposerTextChange: (String) -> Unit,
    stack: String,
    onStackChange: (String) -> Unit,
    backendStackNeed: String,
    onBackendStackNeedChange: (String) -> Unit,
    spotsInput: String,
    onSpotsInputChange: (String) -> Unit,
    onPostProject: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = AppDesignTokens.cardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "What's on your mind?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Share your project ideas, post job openings, or find collaborators",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            OutlinedTextField(
                value = composerText,
                onValueChange = onComposerTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp),
                placeholder = { Text("Share project status, role requirements, or a collaboration idea...") },
                maxLines = 4
            )
            OutlinedTextField(
                value = stack,
                onValueChange = onStackChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Tech stack (optional)") },
                supportingText = { Text("Example: Kotlin + Firebase + Spring Boot") }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = backendStackNeed,
                    onValueChange = onBackendStackNeedChange,
                    modifier = Modifier.weight(1.7f),
                    singleLine = true,
                    label = { Text("Backend stack needed") },
                    supportingText = { Text("Example: Node.js + PostgreSQL") }
                )
                OutlinedTextField(
                    value = spotsInput,
                    onValueChange = onSpotsInputChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Spots") },
                    supportingText = { Text("1-20") }
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Button(
                    onClick = onPostProject,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppDesignTokens.compactButtonHeight + 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.width(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Post Project", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ProjectPostCard(post: ProjectPost) {
    var isExpanded by remember { mutableStateOf(false) }
    val baseLikes = remember(post.title) { (post.title.hashCode().absoluteValue % 120) + 4 }
    val baseComments = remember(post.title) { (post.owner.hashCode().absoluteValue % 24) + 1 }
    var hasLiked by remember { mutableStateOf(false) }
    var hasCommented by remember { mutableStateOf(false) }
    var hasJoined by remember { mutableStateOf(false) }
    var hasMessaged by remember { mutableStateOf(false) }
    var showCommentComposer by remember { mutableStateOf(false) }
    var commentDraft by rememberSaveable(post.title) { mutableStateOf("") }
    var submittedComment by rememberSaveable(post.title) { mutableStateOf("") }
    var showJoinForm by remember { mutableStateOf(false) }
    var showMessageForm by remember { mutableStateOf(false) }

    if (showJoinForm) {
        JoinProjectFormDialog(
            projectTitle = post.title,
            onDismissRequest = { showJoinForm = false },
            onSubmit = { hasJoined = true }
        )
    }

    if (showMessageForm) {
        MessageOwnerDialog(
            projectTitle = post.title,
            ownerName = post.owner,
            onDismissRequest = { showMessageForm = false },
            onSend = { hasMessaged = true }
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = AppDesignTokens.cardShape
    ) {
        Column(modifier = Modifier.padding(AppDesignTokens.cardInnerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        post.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        "by ${post.owner}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Surface(
                    color = VibrantOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "${post.spotsLeft} spots",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = VibrantOrange,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                post.stack,
                color = ElectricCyan,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                post.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Text("Open Roles:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        post.openRoles.forEach { role ->
                            Text("• $role", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            if (showCommentComposer && !hasCommented) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = commentDraft,
                    onValueChange = { commentDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Add your comment") },
                    maxLines = 3,
                    supportingText = { Text("Each user can comment once per post in this demo") }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        if (commentDraft.isNotBlank()) {
                            submittedComment = commentDraft
                            commentDraft = ""
                            hasCommented = true
                            showCommentComposer = false
                        }
                    },
                    enabled = commentDraft.isNotBlank(),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Post Comment")
                }
            }

            if (submittedComment.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Your comment: $submittedComment",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { hasLiked = true },
                        enabled = !hasLiked,
                        modifier = Modifier.height(AppDesignTokens.compactButtonHeight)
                    ) {
                        Text("👍 ${baseLikes + if (hasLiked) 1 else 0}", style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(
                        onClick = { showCommentComposer = true },
                        enabled = !hasCommented,
                        modifier = Modifier.height(AppDesignTokens.compactButtonHeight)
                    ) {
                        Text("💬 ${baseComments + if (hasCommented) 1 else 0}", style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.height(AppDesignTokens.compactButtonHeight)) {
                        Text(if (isExpanded) "Less" else "More", style = MaterialTheme.typography.labelMedium)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { showJoinForm = true },
                        enabled = !hasJoined,
                        modifier = Modifier.height(AppDesignTokens.compactButtonHeight)
                    ) {
                        Text(if (hasJoined) "Joined" else "Join", style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(
                        onClick = { showMessageForm = true },
                        enabled = !hasMessaged,
                        modifier = Modifier.height(AppDesignTokens.compactButtonHeight)
                    ) {
                        Text(if (hasMessaged) "Messaged" else "Message", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveGradientCard(
    title: String,
    subtitle: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
    val estimatedTasks = remember(title) { (title.hashCode().absoluteValue % 9) + 3 }
    val activeContributors = remember(subtitle) { (subtitle.hashCode().absoluteValue % 6) + 2 }
    val completion = remember(title, subtitle) { (title.length * 7 + subtitle.length * 3) % 55 + 35 }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = AppDesignTokens.cardLargeShape,
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.64f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.52f)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Outlined.Insights,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssistChip(
                        onClick = onPrimaryClick,
                        label = { Text("$activeContributors active") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                    )
                    AssistChip(
                        onClick = onSecondaryClick,
                        label = { Text("$estimatedTasks tasks") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.52f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Module completion", style = MaterialTheme.typography.labelMedium)
                        Text("$completion%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(
                    "Open this module to see dedicated tools, deeper analytics, and the live workflow for this area.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = onPrimaryClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(AppDesignTokens.compactButtonHeight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Explore", fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = onSecondaryClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(AppDesignTokens.compactButtonHeight)
                    ) {
                        Text("Learn More", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchCard(
    title: String,
    subtitle: String,
    onViewClick: () -> Unit,
    onInviteClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppDesignTokens.cardShape,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onViewClick) { Text("View profile") }
                TextButton(onClick = onInviteClick) { Text("Invite") }
            }
        }
    }
}

@Composable
private fun ActivityCard(item: ActivityItem, onViewClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppDesignTokens.cardShape,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(item.time, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = onViewClick) { Text("Open activity", style = MaterialTheme.typography.labelLarge) }
        }
    }
}

@Composable
private fun EventCard(event: EventHighlight, onViewClick: () -> Unit) {
    var showJoinEventDialog by remember { mutableStateOf(false) }

    if (showJoinEventDialog) {
        JoinEventDialog(
            eventTitle = event.title,
            onDismissRequest = { showJoinEventDialog = false }
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppDesignTokens.cardShape,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(event.meta, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onViewClick) { Text("View event") }
                TextButton(onClick = { showJoinEventDialog = true }) { Text("Join now") }
            }
        }
    }
}

@Composable
private fun NewsCard(news: NewsHighlight, onViewClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppDesignTokens.cardShape,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(news.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(news.source, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = onViewClick) { Text("Read more", style = MaterialTheme.typography.labelLarge) }
        }
    }
}

private fun modulePrimaryRoute(moduleTitle: String): String {
    val normalized = moduleTitle.lowercase()
    return when {
        "team" in normalized || "matching" in normalized -> AppRoutes.SEARCH
        "task" in normalized -> AppRoutes.TASKS
        "event" in normalized -> AppRoutes.EVENTS
        "portfolio" in normalized || "sync" in normalized -> AppRoutes.PROFILE
        else -> AppRoutes.PROJECTS
    }
}

private fun moduleSecondaryRoute(moduleTitle: String): String {
    val normalized = moduleTitle.lowercase()
    return when {
        "team" in normalized || "matching" in normalized -> AppRoutes.CHAT
        "task" in normalized -> AppRoutes.PROJECTS
        "event" in normalized -> AppRoutes.NOTIFICATIONS
        "portfolio" in normalized || "sync" in normalized -> AppRoutes.PROFILE
        else -> AppRoutes.DASHBOARD
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun JoinProjectFormDialog(
    projectTitle: String,
    onDismissRequest: () -> Unit,
    onSubmit: () -> Unit = {}
) {
    var yourName by rememberSaveable { mutableStateOf("") }
    var experience by rememberSaveable { mutableStateOf("") }
    val availableRoles = listOf("Frontend", "Backend", "Full Stack")
    var selectedRole by rememberSaveable { mutableStateOf(availableRoles.first()) }
    var roleExpanded by remember { mutableStateOf(false) }
    var portfolio by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Join Project", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Interested in: $projectTitle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = yourName,
                    onValueChange = { yourName = it },
                    label = { Text("Your name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("Years of experience *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("e.g., 3 years") }
                )
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role you want to join *") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        supportingText = { Text("Choose one: Frontend, Backend, Full Stack") }
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        availableRoles.forEach { roleOption ->
                            DropdownMenuItem(
                                text = { Text(roleOption) },
                                onClick = {
                                    selectedRole = roleOption
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = portfolio,
                    onValueChange = { portfolio = it },
                    label = { Text("Portfolio/GitHub link") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (yourName.isNotBlank() && experience.isNotBlank()) {
                    onSubmit()
                    onDismissRequest()
                }
            }) {
                Text("Submit Application")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MessageOwnerDialog(
    projectTitle: String,
    ownerName: String,
    onDismissRequest: () -> Unit,
    onSend: () -> Unit
) {
    var message by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Message $ownerName", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Project: $projectTitle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Your message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    supportingText = { Text("Share your interest or ask questions about the project") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (message.isNotBlank()) {
                    onSend()
                    onDismissRequest()
                }
            }) {
                Text("Send Message")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun InviteCollaboratorDialog(
    collaborator: CollaboratorMatch,
    onDismissRequest: () -> Unit
) {
    var inviteMessage by rememberSaveable { mutableStateOf("") }
    var selectedRole by rememberSaveable { mutableStateOf("Backend Developer") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Invite ${collaborator.name}", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Stack: ${collaborator.stack}", style = MaterialTheme.typography.bodyMedium, color = ElectricCyan)
                Text("Match Score: ${collaborator.matchScore}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ElectricGreen)
                OutlinedTextField(
                    value = selectedRole,
                    onValueChange = { selectedRole = it },
                    label = { Text("Role for invitation") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = inviteMessage,
                    onValueChange = { inviteMessage = it },
                    label = { Text("Invitation message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4,
                    supportingText = { Text("Tell them why you think they'd be a great fit") }
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text("Send Invite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun JoinProjectTeamDialog(
    projectTitle: String,
    onDismissRequest: () -> Unit,
    onSubmit: () -> Unit = {}
) {
    var yourName by rememberSaveable { mutableStateOf("") }
    var position by rememberSaveable { mutableStateOf("") }
    var experience by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Join Project Team", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Project: $projectTitle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = yourName,
                    onValueChange = { yourName = it },
                    label = { Text("Your name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Position you want to join *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("e.g., Frontend, Backend, DevOps") }
                )
                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("Relevant experience *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4,
                    supportingText = { Text("Tell us about your experience in this area") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (yourName.isNotBlank() && position.isNotBlank() && experience.isNotBlank()) {
                    onSubmit()
                    onDismissRequest()
                }
            }) {
                Text("Join Team")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun JoinEventDialog(
    eventTitle: String,
    onDismissRequest: () -> Unit
) {
    var teamName by rememberSaveable { mutableStateOf("") }
    var teamSize by rememberSaveable { mutableStateOf("") }
    var skillLevel by rememberSaveable { mutableStateOf("Intermediate") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Join Event", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Event: $eventTitle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Team name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = teamSize,
                    onValueChange = { teamSize = it },
                    label = { Text("Team size (number of people)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = skillLevel,
                    onValueChange = { skillLevel = it },
                    label = { Text("Team skill level") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("Beginner, Intermediate, Advanced") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (teamName.isNotBlank() && teamSize.isNotBlank()) {
                    onDismissRequest()
                }
            }) {
                Text("Register Team")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun InlineProjectCreationForm(
    postTitle: String,
    onTitleChange: (String) -> Unit,
    postStack: String,
    onStackChange: (String) -> Unit,
    postDescription: String,
    onDescriptionChange: (String) -> Unit,
    postLocation: String,
    onLocationChange: (String) -> Unit,
    postDeadline: String,
    onDeadlineChange: (String) -> Unit,
    onPublish: () -> Unit,
    onCancel: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Create a New Project", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = postTitle,
                onValueChange = onTitleChange,
                label = { Text("Project title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = postStack,
                onValueChange = onStackChange,
                label = { Text("Tech stack *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                supportingText = { Text("e.g., React, Kotlin, Firebase") }
            )
            OutlinedTextField(
                value = postDescription,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                maxLines = 3,
                textStyle = MaterialTheme.typography.bodySmall,
                supportingText = { Text("What are you building?") }
            )
            OutlinedTextField(
                value = postLocation,
                onValueChange = onLocationChange,
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = postDeadline,
                onValueChange = onDeadlineChange,
                label = { Text("Deadline") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onPublish,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Publish", fontSize = 12.sp)
                }
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text("Cancel", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun InlineBackendNeedForm(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = VibrantOrange.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("We're Hiring Backend Developers", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VibrantOrange)

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Job title or position *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                supportingText = { Text("e.g., Senior Backend Engineer, DevOps Lead") }
            )
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("About the role") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                maxLines = 3,
                textStyle = MaterialTheme.typography.bodySmall,
                supportingText = { Text("Describe the role, requirements, and benefits") }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = VibrantOrange)
                ) {
                    Text("Post Now", fontSize = 12.sp)
                }
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text("Cancel", fontSize = 12.sp)
                }
            }
        }
    }
}

