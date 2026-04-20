package com.example.developernetworkingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.navigation.MainAppScaffold
import com.example.developernetworkingapp.ui.screens.AdvancedLoginScreen
import com.example.developernetworkingapp.ui.screens.AdvancedSignupScreen
import com.example.developernetworkingapp.ui.screens.CollaboratorProfileScreen
import com.example.developernetworkingapp.ui.screens.GenericDetailScreen
import com.example.developernetworkingapp.ui.theme.DeveloperNetworkingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeveloperNetworkingAppTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = AppRoutes.DASHBOARD) {
                    composable(AppRoutes.LOGIN) { AdvancedLoginScreen(navController) }
                    composable(AppRoutes.SIGNUP) { AdvancedSignupScreen(navController) }
                    composable(AppRoutes.DASHBOARD) { MainAppScaffold(navController) }
                    composable(AppRoutes.PROJECTS) { MainAppScaffold(navController) }
                    composable(AppRoutes.CHAT) { MainAppScaffold(navController) }
                    composable(AppRoutes.SEARCH) { MainAppScaffold(navController) }
                    composable(AppRoutes.NOTIFICATIONS) { MainAppScaffold(navController) }
                    composable(AppRoutes.PROFILE) { MainAppScaffold(navController) }
                    composable(AppRoutes.TASKS) { MainAppScaffold(navController) }
                    composable(AppRoutes.EVENTS) { MainAppScaffold(navController) }
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
                    composable(
                        route = AppRoutes.COLLABORATOR_PROFILE,
                        arguments = listOf(
                            navArgument("name") { type = NavType.StringType },
                            navArgument("stack") { type = NavType.StringType },
                            navArgument("score") { type = NavType.IntType }
                        )
                    ) { entry ->
                        CollaboratorProfileScreen(
                            padding = androidx.compose.foundation.layout.PaddingValues(),
                            navController = navController,
                            name = entry.arguments?.getString("name").orEmpty(),
                            stack = entry.arguments?.getString("stack").orEmpty(),
                            score = entry.arguments?.getInt("score") ?: 0
                        )
                    }
                }
            }
        }
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
    NavHost(navController = navController, startDestination = AppRoutes.DASHBOARD) {
        composable(AppRoutes.LOGIN) { AdvancedLoginScreen(navController) }
        composable(AppRoutes.SIGNUP) { AdvancedSignupScreen(navController) }
        composable(AppRoutes.DASHBOARD) { MainAppScaffold(navController) }
        composable(AppRoutes.PROJECTS) { MainAppScaffold(navController) }
        composable(AppRoutes.CHAT) { MainAppScaffold(navController) }
        composable(AppRoutes.SEARCH) { MainAppScaffold(navController) }
        composable(AppRoutes.NOTIFICATIONS) { MainAppScaffold(navController) }
        composable(AppRoutes.PROFILE) { MainAppScaffold(navController) }
        composable(AppRoutes.TASKS) { MainAppScaffold(navController) }
        composable(AppRoutes.EVENTS) { MainAppScaffold(navController) }
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
        composable(
            route = AppRoutes.COLLABORATOR_PROFILE,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("stack") { type = NavType.StringType },
                navArgument("score") { type = NavType.IntType }
            )
        ) { entry ->
            CollaboratorProfileScreen(
                padding = androidx.compose.foundation.layout.PaddingValues(),
                navController = navController,
                name = entry.arguments?.getString("name").orEmpty(),
                stack = entry.arguments?.getString("stack").orEmpty(),
                score = entry.arguments?.getInt("score") ?: 0
            )
        }
    }
}