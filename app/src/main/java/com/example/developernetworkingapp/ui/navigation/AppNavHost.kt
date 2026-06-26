package com.example.developernetworkingapp.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.developernetworkingapp.di.appViewModel
import com.example.developernetworkingapp.ui.event.AppNavEvent
import com.example.developernetworkingapp.ui.event.AuthNavEvent
import com.example.developernetworkingapp.ui.screens.AdvancedLoginScreen
import com.example.developernetworkingapp.ui.screens.AdvancedSignupScreen
import com.example.developernetworkingapp.ui.screens.ConversationRoute
import com.example.developernetworkingapp.ui.screens.EmailVerificationRoute
import com.example.developernetworkingapp.ui.screens.CollaboratorProfileRoute
import com.example.developernetworkingapp.ui.screens.GenericDetailScreen
import com.example.developernetworkingapp.ui.screens.CollectAuthNavEvents

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val appNavigationViewModel: AppNavigationViewModel = appViewModel()
    val currentUser by appNavigationViewModel.currentUser.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(currentUser, currentRoute) {
        appNavigationViewModel.onRouteChanged(currentRoute, currentUser != null)
    }

    LaunchedEffect(Unit) {
        appNavigationViewModel.navEvents.collect { event ->
            when (event) {
                AppNavEvent.NavigateToLogin ->
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                AppNavEvent.NavigateToDashboard ->
                    navController.navigate(AppRoutes.DASHBOARD) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
            }
        }
    }

    CollectAuthNavEvents(navController)

    NavHost(navController = navController, startDestination = AppRoutes.LOGIN) {
        composable(AppRoutes.LOGIN) { AdvancedLoginScreen() }
        composable(AppRoutes.SIGNUP) { AdvancedSignupScreen() }
        composable(
            route = AppRoutes.VERIFY_EMAIL,
            arguments = listOf(navArgument("email") { type = NavType.StringType }),
        ) { entry ->
            EmailVerificationRoute(email = entry.arguments?.getString("email").orEmpty())
        }
        composable(AppRoutes.DASHBOARD) { MainAppScaffold(navController) }
        composable(
            route = AppRoutes.PROJECTS_WITH_PROJECT,
            arguments = listOf(
                navArgument("project") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { MainAppScaffold(navController) }
        composable(AppRoutes.CHAT) { MainAppScaffold(navController) }
        composable(AppRoutes.SEARCH) { MainAppScaffold(navController) }
        composable(AppRoutes.NOTIFICATIONS) { MainAppScaffold(navController) }
        composable(AppRoutes.PROFILE) { MainAppScaffold(navController) }
        composable(AppRoutes.SETTINGS) { MainAppScaffold(navController) }
        composable(AppRoutes.ADMIN_DASHBOARD) { MainAppScaffold(navController) }
        composable(AppRoutes.TASKS) { MainAppScaffold(navController) }
        composable(AppRoutes.EVENTS) { MainAppScaffold(navController) }
        composable(
            route = AppRoutes.CONVERSATION,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType }),
        ) { entry ->
            ConversationRoute(
                padding = PaddingValues(),
                navController = navController,
                conversationId = entry.arguments?.getString("conversationId").orEmpty(),
            )
        }
        composable(
            route = AppRoutes.COLLABORATOR_PROFILE,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("score") { type = NavType.IntType },
            ),
        ) { entry ->
            CollaboratorProfileRoute(
                padding = PaddingValues(),
                navController = navController,
                collaboratorId = entry.arguments?.getString("id").orEmpty(),
                score = entry.arguments?.getInt("score") ?: 0,
            )
        }
        composable(
            route = AppRoutes.DETAIL,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("subtitle") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType },
                navArgument("sourceRoute") { type = NavType.StringType },
            ),
        ) { entry ->
            GenericDetailScreen(
                padding = PaddingValues(),
                navController = navController,
                title = entry.arguments?.getString("title").orEmpty(),
                subtitle = entry.arguments?.getString("subtitle").orEmpty(),
                description = entry.arguments?.getString("description").orEmpty(),
                sourceRoute = entry.arguments?.getString("sourceRoute") ?: AppRoutes.DASHBOARD,
            )
        }
    }
}
