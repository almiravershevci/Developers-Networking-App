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
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.developernetworkingapp.di.appViewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.domain.model.ActivityItem
import com.example.developernetworkingapp.domain.model.CollaboratorMatch
import com.example.developernetworkingapp.domain.model.MatchRequest
import com.example.developernetworkingapp.domain.model.EventHighlight
import com.example.developernetworkingapp.ui.state.FeedPostState
import com.example.developernetworkingapp.ui.components.EmptyStateCard
import com.example.developernetworkingapp.ui.components.ErrorStateCard
import com.example.developernetworkingapp.ui.components.GradientHeroCard
import com.example.developernetworkingapp.ui.components.LoadingStateCard
import com.example.developernetworkingapp.ui.components.NotificationBanner
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.data.ShortcutItem
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.DashboardUiState
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.DashboardUiEvent
import com.example.developernetworkingapp.ui.viewmodel.DashboardViewModel
import com.example.developernetworkingapp.ui.theme.VibrantOrange
import com.example.developernetworkingapp.ui.theme.ElectricCyan
import com.example.developernetworkingapp.ui.theme.ElectricGreen
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.absoluteValue

@Composable
fun DashboardRoute(
    padding: PaddingValues,
    navController: NavController,
    viewModel: DashboardViewModel = appViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        padding = padding,
        navController = navController,
        state = state,
        events = viewModel.events,
        onRefresh = viewModel::refreshFeed,
        onToggleLike = viewModel::togglePostLike,
        onTogglePostExpanded = viewModel::togglePostExpanded,
        onToggleComments = viewModel::toggleCommentsVisibility,
        onCommentDraftChange = viewModel::updateCommentDraft,
        onSubmitComment = viewModel::submitComment,
        onProjectApplicationSubmitted = viewModel::notifyProjectApplicationSubmitted,
        onSendMatchInvite = viewModel::sendMatchInvite,
        onAcceptMatchRequest = viewModel::acceptMatchRequest,
        onDeclineMatchRequest = viewModel::declineMatchRequest,
    )
}

@Composable
fun DashboardScreen(
    padding: PaddingValues,
    navController: NavController,
    state: DashboardUiState,
    events: SharedFlow<DashboardUiEvent>,
    onRefresh: () -> Unit,
    onToggleLike: (String) -> Unit,
    onTogglePostExpanded: (String) -> Unit,
    onToggleComments: (String) -> Unit,
    onCommentDraftChange: (String, String) -> Unit,
    onSubmitComment: (String) -> Unit,
    onProjectApplicationSubmitted: () -> Unit,
    onSendMatchInvite: (String, String?) -> Unit = { _, _ -> },
    onAcceptMatchRequest: (String) -> Unit = {},
    onDeclineMatchRequest: (String) -> Unit = {},
) {
    val content = state.content
    var showInviteDialog by rememberSaveable { mutableStateOf(false) }
    var selectedCollaborator by remember { mutableStateOf<CollaboratorMatch?>(null) }
    var showJoinProjectDialog by rememberSaveable { mutableStateOf(false) }
    var selectedProject by remember { mutableStateOf<String?>(null) }
    var activeNotification by rememberSaveable { mutableStateOf<String?>(null) }
    val shortcuts = listOf(
        ShortcutItem("Task Board", AppRoutes.TASKS, Icons.Outlined.Task),
        ShortcutItem("Live Events", AppRoutes.EVENTS, Icons.Outlined.Event),
        ShortcutItem("Chat Hub", AppRoutes.CHAT, Icons.Outlined.ChatBubbleOutline),
        ShortcutItem("Advanced Search", AppRoutes.SEARCH, Icons.Outlined.Task)
    )

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is DashboardUiEvent.ShowNotification -> activeNotification = event.message
            }
        }
    }

    // Notification Display
    activeNotification?.let { notificationMessage ->
        LaunchedEffect(notificationMessage) {
            kotlinx.coroutines.delay(AppDesignTokens.notificationAutoHideMs)
            activeNotification = null
        }
        NotificationBanner(
            message = notificationMessage,
            onDismiss = { activeNotification = null },
            containerColor = ElectricGreen.copy(alpha = 0.9f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    if (showInviteDialog && selectedCollaborator != null) {
        InviteCollaboratorDialog(
            collaborator = selectedCollaborator!!,
            isSending = state.matchActionInFlight == "send",
            onDismissRequest = { showInviteDialog = false; selectedCollaborator = null },
            onSendInvite = { message ->
                val collaborator = selectedCollaborator!!
                onSendMatchInvite(collaborator.suggestedUserId, message)
                showInviteDialog = false
                selectedCollaborator = null
            },
        )
    }

    if (showJoinProjectDialog && selectedProject != null) {
        JoinProjectTeamDialog(
            projectTitle = selectedProject!!,
            onDismissRequest = { showJoinProjectDialog = false; selectedProject = null },
            onSubmit = onProjectApplicationSubmitted
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
                subtitle = content?.heroSubtitle ?: if (state.isLoading) {
                    "Loading collaboration intelligence…"
                } else {
                    "Discover collaborators, projects, and events tailored to you"
                },
                progress = content?.analyticsSourceLine
                    ?: "Your creator feed, matching engine, and live project discovery are active",
            )
        }
        if (state.isLoading && content == null) {
            item { LoadingStateCard(message = "Loading your command center…") }
        }
        state.errorMessage?.let { error ->
            item { ErrorStateCard(error, onRetry = onRefresh) }
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
                    Button(onClick = onRefresh) {
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
        item { SectionTitle("Recruiting Projects") }
        items(state.feedPosts, key = { it.id }) { postState ->
            ProjectPostCard(
                postState = postState,
                onToggleLike = { onToggleLike(postState.id) },
                onToggleComments = { onToggleComments(postState.id) },
                onToggleExpanded = { onTogglePostExpanded(postState.id) },
                onCommentDraftChange = { onCommentDraftChange(postState.id, it) },
                onSubmitComment = { onSubmitComment(postState.id) }
            )
        }
        item { SectionTitle("Feature Modules") }
        items(content?.modules ?: emptyList()) { module ->
            InteractiveGradientCard(
                title = module.title,
                subtitle = module.subtitle,
                onLearnMoreClick = { navController.navigate(moduleLearnMoreRoute(module.title, module.subtitle)) }
            )
        }
        if (state.incomingMatchRequests.isNotEmpty()) {
            item { SectionTitle("Pending Match Requests") }
            items(state.incomingMatchRequests, key = { it.id }) { request ->
                PendingMatchRequestCard(
                    request = request,
                    isResolving = state.matchActionInFlight == request.id,
                    onAccept = { onAcceptMatchRequest(request.id) },
                    onDecline = { onDeclineMatchRequest(request.id) },
                )
            }
        }
        item { SectionTitle("Suggested Collaborators") }
        items(content?.matches ?: emptyList()) { match ->
            MatchCard(
                title = "${match.name} (${match.stack})",
                subtitle = "Match score: ${match.matchScore}% - Invite or start 1:1 chat",
                onViewClick = {
                    navController.navigate(
                        AppRoutes.collaboratorProfileRoute(match.suggestedUserId, match.matchScore),
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
                onViewClick = {
                    navController.navigate(
                        AppRoutes.detailRoute(
                            title = project.title,
                            subtitle = "Project Progress: ${project.progress}%",
                            description = buildProjectDetailDescription(project.description, project.progress),
                            sourceRoute = AppRoutes.PROJECTS
                        )
                    )
                },
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
private fun ProjectPostCard(
    postState: FeedPostState,
    onToggleLike: () -> Unit,
    onToggleComments: () -> Unit,
    onToggleExpanded: () -> Unit,
    onCommentDraftChange: (String) -> Unit,
    onSubmitComment: () -> Unit
) {
    val post = postState.post
    val baseLikes = remember(post.title) { (post.title.hashCode().absoluteValue % 120) + 4 }
    var hasJoined by remember { mutableStateOf(false) }
    var hasMessaged by remember { mutableStateOf(false) }
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

            if (postState.isExpanded) {
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

            if (postState.isCommentsVisible) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Comments", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        postState.comments.forEach { comment ->
                            Text(comment, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = postState.commentDraft,
                    onValueChange = onCommentDraftChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Add your comment") },
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onSubmitComment,
                    enabled = postState.commentDraft.isNotBlank(),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Post Comment")
                }
                if (postState.comments.any { it.startsWith("You:") }) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Your latest comments appear under the public thread.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
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
                        onClick = onToggleLike,
                        modifier = Modifier.height(AppDesignTokens.compactButtonHeight)
                    ) {
                        Text("👍 ${baseLikes + if (postState.hasLiked) 1 else 0}", style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(
                        onClick = onToggleComments,
                        modifier = Modifier.height(AppDesignTokens.compactButtonHeight)
                    ) {
                        Text(
                            if (postState.isCommentsVisible) {
                                "Hide comments (${postState.comments.size})"
                            } else {
                                "Comments (${postState.comments.size})"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    TextButton(onClick = onToggleExpanded, modifier = Modifier.height(AppDesignTokens.compactButtonHeight)) {
                        Text(if (postState.isExpanded) "Less" else "More", style = MaterialTheme.typography.labelMedium)
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
    onLearnMoreClick: () -> Unit
) {
    val estimatedTasks = remember(title) { (title.hashCode().absoluteValue % 9) + 3 }
    val activeContributors = remember(subtitle) { (subtitle.hashCode().absoluteValue % 6) + 2 }
    val completion = remember(title, subtitle) { (title.length * 7 + subtitle.length * 3) % 55 + 35 }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                        onClick = {},
                        label = { Text("$activeContributors active") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                    )
                    AssistChip(
                        onClick = {},
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
                TextButton(onClick = onLearnMoreClick, modifier = Modifier.padding(top = 2.dp)) {
                    Text("Learn More", style = MaterialTheme.typography.labelLarge, textDecoration = TextDecoration.Underline)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun PendingMatchRequestCard(
    request: MatchRequest,
    isResolving: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppDesignTokens.cardShape,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${request.fromDisplayName} wants to collaborate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            request.message?.takeIf { it.isNotBlank() }?.let { message ->
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = request.relativeTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAccept,
                    enabled = !isResolving,
                ) {
                    Text(if (isResolving) "Working…" else "Accept")
                }
                TextButton(
                    onClick = onDecline,
                    enabled = !isResolving,
                ) {
                    Text("Decline")
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
                TextButton(onClick = onViewClick) {
                    Text("View profile", textDecoration = TextDecoration.Underline)
                }
                Button(onClick = onInviteClick) { Text("Invite") }
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

private fun moduleLearnMoreRoute(moduleTitle: String, moduleSubtitle: String): String =
    AppRoutes.detailRoute(
        title = moduleTitle,
        subtitle = moduleSubtitle,
        description = moduleSubtitle,
        sourceRoute = AppRoutes.DASHBOARD,
    )

private fun buildProjectDetailDescription(summary: String, progress: Int): String {
    val body = summary.trim().ifBlank { "No project description available yet." }
    return "$body\n\nProgress: $progress%"
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
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true,
                            )
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
    isSending: Boolean,
    onDismissRequest: () -> Unit,
    onSendInvite: (String?) -> Unit,
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
            Button(
                onClick = {
                    val message = buildString {
                        append("Role: $selectedRole")
                        if (inviteMessage.isNotBlank()) {
                            append("\n")
                            append(inviteMessage.trim())
                        }
                    }.takeIf { it.isNotBlank() }
                    onSendInvite(message)
                },
                enabled = !isSending && collaborator.suggestedUserId.isNotBlank(),
            ) {
                Text(if (isSending) "Sending…" else "Send Invite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, enabled = !isSending) {
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

