package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.developernetworkingapp.di.appViewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.domain.model.ProjectBoardContent
import com.example.developernetworkingapp.ui.components.CreateProjectDialog
import com.example.developernetworkingapp.ui.components.EmptyStateCard
import com.example.developernetworkingapp.ui.components.LoadingStateCard
import com.example.developernetworkingapp.ui.components.PremiumInfoCard
import com.example.developernetworkingapp.ui.components.NotificationBanner
import com.example.developernetworkingapp.ui.components.SectionTitle
import com.example.developernetworkingapp.ui.util.userFacingStatusMessage
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.state.ProjectsUiState
import com.example.developernetworkingapp.ui.components.CreateTaskDialog
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import com.example.developernetworkingapp.ui.viewmodel.ProjectsUiEvent
import com.example.developernetworkingapp.ui.viewmodel.ProjectsViewModel
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun ProjectBoardRoute(
    padding: PaddingValues,
    navController: NavController,
    selectedProjectName: String = ""
) {
    val viewModel: ProjectsViewModel = appViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(selectedProjectName) {
        viewModel.setSelectedProject(selectedProjectName)
    }
    ProjectBoardScreen(
        padding = padding,
        state = state,
        navController = navController,
        events = viewModel.events,
        onInviteDeveloper = viewModel::notifyInviteStarted,
        onCreateProject = viewModel::createProject,
        onCreateTask = viewModel::createTask,
        onDismissCreateProject = viewModel::clearCreateProjectError,
        onDismissCreateTask = viewModel::clearCreateTaskError,
    )
}

@Composable
fun ProjectBoardScreen(
    padding: PaddingValues,
    state: ProjectsUiState,
    navController: NavController,
    events: SharedFlow<ProjectsUiEvent>,
    onInviteDeveloper: () -> Unit,
    onCreateProject: (String, String, String) -> Unit,
    onCreateTask: (String, String, String, String?) -> Unit,
    onDismissCreateProject: () -> Unit,
    onDismissCreateTask: () -> Unit,
) {
    val content = state.displayContent
    var selectedTask by remember { mutableStateOf<String?>(null) }
    var activeNotification by rememberSaveable { mutableStateOf<String?>(null) }
    var showCreateProjectDialog by rememberSaveable { mutableStateOf(false) }
    var showCreateTaskDialog by rememberSaveable { mutableStateOf(false) }
    var showInviteDialog by rememberSaveable { mutableStateOf(false) }
    var inviteTarget by rememberSaveable { mutableStateOf("") }
    var inviteRole by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is ProjectsUiEvent.ShowNotification -> activeNotification = event.message
                ProjectsUiEvent.ProjectCreated -> showCreateProjectDialog = false
                ProjectsUiEvent.TaskCreated -> showCreateTaskDialog = false
            }
        }
    }

    activeNotification?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(AppDesignTokens.notificationAutoHideMs)
            activeNotification = null
        }
        NotificationBanner(message = message, onDismiss = { activeNotification = null })
    }

    selectedTask?.let { task ->
        AlertDialog(
            onDismissRequest = { selectedTask = null },
            title = { Text("Task Detail") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task, style = MaterialTheme.typography.titleMedium)
                    Text("Status, assignee, dependencies, and sprint estimate are tracked in this task.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = { navController.navigate(AppRoutes.TASKS) }, label = { Text("In Sprint") })
                        AssistChip(onClick = {
                            navController.navigate(
                                AppRoutes.detailRoute(
                                    title = task,
                                    subtitle = "Priority",
                                    description = "This task is marked high priority. Track blockers, reviewer status, and delivery deadline from the task board.",
                                    sourceRoute = AppRoutes.PROJECTS
                                )
                            )
                        }, label = { Text("Priority High") })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedTask = null
                    navController.navigate(AppRoutes.TASKS)
                }) { Text("Open board") }
            },
            dismissButton = { TextButton(onClick = { selectedTask = null }) { Text("Close") } }
        )
    }

    if (showCreateProjectDialog) {
        CreateProjectDialog(
            isSubmitting = state.isCreatingProject,
            errorMessage = state.createProjectError,
            onDismiss = {
                showCreateProjectDialog = false
                onDismissCreateProject()
            },
            onCreate = onCreateProject,
        )
    }

    if (showCreateTaskDialog) {
        val assignableMembers = if (content?.isOwner == true) {
            content.members.filter { it.role != "owner" }
        } else {
            emptyList()
        }
        CreateTaskDialog(
            isSubmitting = state.isCreatingTask,
            errorMessage = state.createTaskError,
            assigneeOptions = assignableMembers,
            onDismiss = {
                showCreateTaskDialog = false
                onDismissCreateTask()
            },
            onCreate = onCreateTask,
        )
    }

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = {
                showInviteDialog = false
                inviteTarget = ""
                inviteRole = ""
            },
            title = { Text("Invite Developer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Send an invitation to join this project workspace.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = inviteTarget,
                        onValueChange = { inviteTarget = it },
                        label = { Text("Email or username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = inviteRole,
                        onValueChange = { inviteRole = it },
                        label = { Text("Role (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onInviteDeveloper()
                        showInviteDialog = false
                        inviteTarget = ""
                        inviteRole = ""
                    },
                    enabled = inviteTarget.isNotBlank()
                ) { Text("Send invite") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showInviteDialog = false
                    inviteTarget = ""
                    inviteRole = ""
                }) { Text("Cancel") }
            }
        )
    }

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
            .padding(horizontal = AppDesignTokens.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppDesignTokens.screenVerticalSpacing),
        contentPadding = AppDesignTokens.screenContentPadding
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("Project Workspace")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (content?.isOwner != false) {
                        Button(onClick = { showCreateTaskDialog = true }) {
                            Text("Add task")
                        }
                    }
                    Button(onClick = { showCreateProjectDialog = true }) {
                        Text("Create project")
                    }
                }
            }
        }
        item {
            ElevatedCard(
                shape = AppDesignTokens.cardLargeShape,
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(AppDesignTokens.cardInnerPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(content?.teamName ?: "Loading team...", style = MaterialTheme.typography.titleLarge)
                    Text(content?.teamMeta ?: "Preparing board data", style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = { navController.navigate(AppRoutes.TASKS) }, label = { Text("Roadmap") }, colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)))
                        AssistChip(onClick = { navController.navigate(AppRoutes.SEARCH) }, label = { Text("Members") })
                        AssistChip(onClick = { navController.navigate(AppRoutes.NOTIFICATIONS) }, label = { Text("Milestones") })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { navController.navigate(AppRoutes.TASKS) }) { Text("Open Project") }
                        TextButton(onClick = { showInviteDialog = true }) { Text("Invite dev") }
                    }
                }
            }
        }
        if (state.isLoading && content == null) {
            item { LoadingStateCard(message = "Loading project board…") }
        }
        if (content != null &&
            content.todo.isEmpty() &&
            content.inProgress.isEmpty() &&
            content.done.isEmpty()
        ) {
            item {
                EmptyStateCard(
                    title = content.teamName,
                    subtitle = userFacingStatusMessage(content.teamMeta)
                        ?: "Tasks will appear here when your project workspace is active.",
                    actionLabel = if (content.teamName == "No projects yet") "Create project" else null,
                    onAction = if (content.teamName == "No projects yet") {
                        { showCreateProjectDialog = true }
                    } else {
                        null
                    },
                )
            }
        }
        item { SectionTitle("Kanban Board") }
        item {
            ClickableTaskColumn("To Do", content?.todo ?: emptyList()) { selectedTask = it }
        }
        item {
            ClickableTaskColumn("In Progress", content?.inProgress ?: emptyList()) { selectedTask = it }
        }
        item {
            ClickableTaskColumn("Done", content?.done ?: emptyList()) { selectedTask = it }
        }
        item {
            PremiumInfoCard(
                "Project actions",
                "Create sprint, review merge requests, assign members, and publish updates to followers."
            )
        }
    }
}

@Composable
private fun ClickableTaskColumn(
    title: String,
    tasks: List<String>,
    onTaskClick: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppDesignTokens.cardShape,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (tasks.isEmpty()) {
                Text(
                    "No tasks in this column",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            tasks.forEach { task ->
                ElevatedCard(
                    onClick = { onTaskClick(task) },
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(task, style = MaterialTheme.typography.bodyMedium)
                        Text("View", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
