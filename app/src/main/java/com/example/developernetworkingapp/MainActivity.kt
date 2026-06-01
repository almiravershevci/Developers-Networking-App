package com.example.developernetworkingapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.navigation.MainAppScaffold
import com.example.developernetworkingapp.ui.screens.AdvancedLoginScreen
import com.example.developernetworkingapp.ui.screens.AdvancedSignupScreen
import com.example.developernetworkingapp.ui.screens.EmailVerificationRoute
import com.example.developernetworkingapp.ui.screens.ConversationScreen
import com.example.developernetworkingapp.ui.screens.GenericDetailScreen
import com.example.developernetworkingapp.ui.theme.DeveloperNetworkingAppTheme
import com.example.developernetworkingapp.ui.viewmodel.SessionViewModel

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        setContent {
            DeveloperNetworkingAppTheme {
                val sessionViewModel: SessionViewModel = viewModel()
                val navController = rememberNavController()
                val currentUser by sessionViewModel.currentUser.collectAsState()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                val unauthenticatedRoutes = setOf(AppRoutes.LOGIN, AppRoutes.SIGNUP)
                val isVerificationRoute = currentRoute?.startsWith("verify/") == true || currentRoute == AppRoutes.VERIFY_EMAIL

                LaunchedEffect(currentUser, currentRoute) {
                    if (currentUser == null && currentRoute != null && currentRoute !in unauthenticatedRoutes && !isVerificationRoute) {
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                    if (currentUser != null && currentRoute != null && currentRoute in unauthenticatedRoutes) {
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                NavHost(navController = navController, startDestination = AppRoutes.LOGIN) {
                    composable(AppRoutes.LOGIN) { AdvancedLoginScreen(navController) }
                    composable(AppRoutes.SIGNUP) { AdvancedSignupScreen(navController) }
                    composable(
                        route = AppRoutes.VERIFY_EMAIL,
                        arguments = listOf(navArgument("email") { type = NavType.StringType })
                    ) { entry ->
                        EmailVerificationRoute(
                            navController = navController,
                            email = entry.arguments?.getString("email").orEmpty()
                        )
                    }
                    composable(AppRoutes.DASHBOARD) { MainAppScaffold(navController) }
                    composable(
                        route = AppRoutes.PROJECTS_WITH_PROJECT,
                        arguments = listOf(
                            navArgument("project") {
                                type = NavType.StringType
                                defaultValue = ""
                            }
                        )
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
                        arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
                    ) { entry ->
                        ConversationScreen(
                            padding = androidx.compose.foundation.layout.PaddingValues(),
                            navController = navController,
                            conversationId = entry.arguments?.getString("conversationId").orEmpty()
                        )
                    }
                    composable(
                        route = AppRoutes.DETAIL,
                        arguments = listOf(
                            navArgument("title") { type = NavType.StringType },
                            navArgument("subtitle") { type = NavType.StringType },
                            navArgument("description") { type = NavType.StringType },
                            navArgument("sourceRoute") { type = NavType.StringType }
                        )
                    ) { entry ->
                        GenericDetailScreen(
                            padding = androidx.compose.foundation.layout.PaddingValues(),
                            navController = navController,
                            title = entry.arguments?.getString("title").orEmpty(),
                            subtitle = entry.arguments?.getString("subtitle").orEmpty(),
                            description = entry.arguments?.getString("description").orEmpty(),
                            sourceRoute = entry.arguments?.getString("sourceRoute") ?: AppRoutes.DASHBOARD
                        )
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    DeveloperNetworkingAppTheme {
        AppEntry()
    }
}

@Composable
private fun AppEntry() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppRoutes.LOGIN) {
        composable(AppRoutes.LOGIN) { AdvancedLoginScreen(navController) }
        composable(AppRoutes.SIGNUP) { AdvancedSignupScreen(navController) }
        composable(
            route = AppRoutes.VERIFY_EMAIL,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { entry ->
            EmailVerificationRoute(
                navController = navController,
                email = entry.arguments?.getString("email").orEmpty()
            )
        }
        composable(AppRoutes.DASHBOARD) { MainAppScaffold(navController) }
        composable(
            route = AppRoutes.PROJECTS_WITH_PROJECT,
            arguments = listOf(
                navArgument("project") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
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
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { entry ->
            ConversationScreen(
                padding = androidx.compose.foundation.layout.PaddingValues(),
                navController = navController,
                conversationId = entry.arguments?.getString("conversationId").orEmpty()
            )
        }
        composable(
            route = AppRoutes.DETAIL,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("subtitle") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType },
                navArgument("sourceRoute") { type = NavType.StringType }
            )
        ) { entry ->
            GenericDetailScreen(
                padding = androidx.compose.foundation.layout.PaddingValues(),
                navController = navController,
                title = entry.arguments?.getString("title").orEmpty(),
                subtitle = entry.arguments?.getString("subtitle").orEmpty(),
                description = entry.arguments?.getString("description").orEmpty(),
                sourceRoute = entry.arguments?.getString("sourceRoute") ?: AppRoutes.DASHBOARD
            )
        }
    }
}