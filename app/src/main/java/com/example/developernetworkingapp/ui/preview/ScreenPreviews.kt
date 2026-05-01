package com.example.developernetworkingapp.ui.preview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.developernetworkingapp.domain.model.ChatContent
import com.example.developernetworkingapp.domain.model.EventContent
import com.example.developernetworkingapp.domain.model.NotificationContent
import com.example.developernetworkingapp.domain.model.ProfileContent
import com.example.developernetworkingapp.domain.model.ProjectBoardContent
import com.example.developernetworkingapp.domain.model.SearchContent
import com.example.developernetworkingapp.domain.model.SearchResult
import com.example.developernetworkingapp.domain.model.TaskContent
import com.example.developernetworkingapp.ui.screens.AdvancedLoginScreen
import com.example.developernetworkingapp.ui.screens.AdvancedSignupScreen
import com.example.developernetworkingapp.ui.screens.ChatScreen
import com.example.developernetworkingapp.ui.screens.CollaboratorProfileScreen
import com.example.developernetworkingapp.ui.screens.DashboardScreen
import com.example.developernetworkingapp.ui.screens.EventFeedScreen
import com.example.developernetworkingapp.ui.screens.GenericDetailScreen
import com.example.developernetworkingapp.ui.screens.NotificationScreen
import com.example.developernetworkingapp.ui.screens.ProfileScreen
import com.example.developernetworkingapp.ui.screens.ProjectBoardScreen
import com.example.developernetworkingapp.ui.screens.SearchScreen
import com.example.developernetworkingapp.ui.screens.TaskManagementScreen
import com.example.developernetworkingapp.ui.state.ChatUiState
import com.example.developernetworkingapp.ui.state.DashboardUiState
import com.example.developernetworkingapp.ui.state.EventsUiState
import com.example.developernetworkingapp.ui.state.NotificationsUiState
import com.example.developernetworkingapp.ui.state.ProfileUiState
import com.example.developernetworkingapp.ui.state.ProjectsUiState
import com.example.developernetworkingapp.ui.state.SearchUiState
import com.example.developernetworkingapp.ui.state.TasksUiState
import com.example.developernetworkingapp.ui.theme.DeveloperNetworkingAppTheme
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Created by Eljesa on 28-Apr-26.
 */

// ============================================================================
// AUTH SCREENS PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
fun AdvancedLoginScreenPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            AdvancedLoginScreen(navController = rememberNavController())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdvancedSignupScreenPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            AdvancedSignupScreen(navController = rememberNavController())
        }
    }
}

// ============================================================================
// CHAT SCREEN PREVIEW
// ============================================================================

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            ChatScreen(
                padding = PaddingValues(16.dp),
                state = ChatUiState(
                    content = ChatContent(
                        conversations = listOf(
                            "Project Planning",
                            "Design Feedback",
                            "API Integration",
                            "Testing Updates"
                        ),
                        composerHint = "Type a message..."
                    )
                ),
                navController = rememberNavController()
            )
        }
    }
}

// ============================================================================
// DASHBOARD SCREEN PREVIEW
// ============================================================================

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            DashboardScreen(
                padding = PaddingValues(16.dp),
                navController = rememberNavController(),
                state = DashboardUiState(),
                events = MutableSharedFlow(),
                onRefresh = {},
                onCreatePost = {},
                onToggleLike = {},
                onTogglePostExpanded = {},
                onToggleComments = {},
                onCommentDraftChange = { _, _ -> },
                onSubmitComment = {},
                onProjectApplicationSubmitted = {},
                onComposerTextChange = {},
                onComposerStackChange = {},
                onComposerBackendNeedChange = {},
                onComposerSpotsInputChange = {}
            )
        }
    }
}

// ============================================================================
// DETAIL SCREENS PREVIEW
// ============================================================================

@Preview(showBackground = true)
@Composable
fun GenericDetailScreenPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            GenericDetailScreen(
                padding = PaddingValues(16.dp),
                navController = rememberNavController(),
                title = "Chat Room: Project Planning",
                subtitle = "Conversation Details",
                description = "Message timeline, members online, shared files, linked tasks, and project references for this room.",
                sourceRoute = "chat"
            )
        }
    }
}

// ============================================================================
// EVENTS SCREEN PREVIEW
// ============================================================================

@Preview(showBackground = true)
@Composable
fun EventFeedScreenPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            EventFeedScreen(
                padding = PaddingValues(16.dp),
                state = EventsUiState(
                    content = EventContent(
                        items = listOf(
                            "Local Hackathon 2026",
                            "Spring Mobile Dev Sprint",
                            "DroidCon 2026",
                            "AI Bootcamp Series"
                        )
                    )
                ),
                navController = rememberNavController(),
                events = MutableSharedFlow(),
                onJoinEvent = {}
            )
        }
    }
}

// ============================================================================
// NOTIFICATIONS SCREEN PREVIEW
// ============================================================================

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            NotificationScreen(
                padding = PaddingValues(16.dp),
                state = NotificationsUiState(
                    content = NotificationContent(
                        items = listOf(
                            "You were invited to Mobile App Project",
                            "Eljesa liked your project post",
                            "New message from Eljesa in Chat Hub",
                            "Reminder: Project deadline in 2 days"
                        )
                    )
                ),
                navController = rememberNavController()
            )
        }
    }
}

// ============================================================================
// PROFILE SCREEN PREVIEW
// ============================================================================

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            ProfileScreen(
                padding = PaddingValues(16.dp),
                state = ProfileUiState(
                    content = ProfileContent(
                        name = "Eljesa Azemi",
                        role = "Software Engineer",
                        bio = "Passionate about building beautiful apps and connecting with fellow creators.",
                        portfolio = "github.com/eljesaazz | linkedin.com/in/eljesaazz | eljesaazz.com",
                        insights = "Commits: 1,247 | PRs: 89 | Stars: 156",
                        stacks = listOf("Kotlin", "Android", "React", "Node.js", "Java")
                    )
                ),
                events = MutableSharedFlow(),
                navController = rememberNavController(),
                onProfileSaved = {},
                onSyncStarted = {},
                onLogout = {}
            )
        }
    }
}

// ============================================================================
// PROJECTS SCREEN PREVIEW
// ============================================================================

@Preview(showBackground = true)
@Composable
fun ProjectBoardScreenPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            ProjectBoardScreen(
                padding = PaddingValues(16.dp),
                state = ProjectsUiState(
                    content = ProjectBoardContent(
                        teamName = "Mobile App Redesign Team",
                        teamMeta = "5 active members • Sprint 12 • On track",
                        todo = listOf(
                            "Design login flow",
                            "Set up authentication",
                            "Create API models"
                        ),
                        inProgress = listOf(
                            "Implement dashboard",
                            "Write unit tests"
                        ),
                        done = listOf(
                            "Project setup",
                            "Database schema"
                        )
                    )
                ),
                navController = rememberNavController(),
                selectedProjectName = "DevConnect Mobile",
                events = MutableSharedFlow(),
                onInviteDeveloper = {}
            )
        }
    }
}

// ============================================================================
// SEARCH SCREEN PREVIEW
// ============================================================================

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            SearchScreen(
                padding = PaddingValues(16.dp),
                state = SearchUiState(
                    query = "",
                    content = SearchContent(
                        filters = listOf("React", "Node.js", "Kotlin", "Python", "Remote"),
                        results = listOf(
                            SearchResult(
                                title = "E-commerce Platform Rebuild",
                                subtitle = "Building a scalable marketplace",
                                owner = "TechTeam Solutions",
                                location = "San Francisco, CA",
                                stack = "React + Node.js + PostgreSQL",
                                membersCount = 8,
                                description = "We're rebuilding our platform with modern tech stack.",
                                rolesNeeded = listOf("Frontend", "Backend", "DevOps")
                            )
                        )
                    )
                ),
                onQueryChange = {},
                onReloadTrends = {},
                navController = rememberNavController()
            )
        }
    }
}

// ============================================================================
// TASKS SCREEN PREVIEW
// ============================================================================

@Preview(showBackground = true)
@Composable
fun TaskManagementScreenPreview() {
    DeveloperNetworkingAppTheme {
        Surface {
            TaskManagementScreen(
                padding = PaddingValues(16.dp),
                state = TasksUiState(
                    content = TaskContent(
                        items = listOf(
                            "Implement authentication module",
                            "Write database migration scripts",
                            "Create API documentation",
                            "Design UI components library",
                            "Set up CI/CD pipeline"
                        )
                    )
                ),
                navController = rememberNavController()
            )
        }
    }
}