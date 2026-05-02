package com.example.developernetworkingapp.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.developernetworkingapp.ui.screens.AdvancedLoginScreen
import com.example.developernetworkingapp.ui.screens.AdvancedSignupScreen
import com.example.developernetworkingapp.ui.screens.CollaboratorProfileScreen
import com.example.developernetworkingapp.ui.screens.EmailVerificationRoute
import com.example.developernetworkingapp.ui.screens.GenericDetailScreen
import android.net.Uri

@Composable
fun DeveloperNetworkingNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN,
    ) {
        composable(AppRoutes.LOGIN) {
            AdvancedLoginScreen(navController)
        }
        composable(AppRoutes.SIGNUP) {
            AdvancedSignupScreen(navController)
        }
        composable(
            route = AppRoutes.VERIFY_EMAIL,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
            ),
        ) { entry ->
            val raw = entry.arguments?.getString("email").orEmpty()
            val email = Uri.decode(raw)
            EmailVerificationRoute(navController, email)
        }

        // Register each tab route on NavGraphBuilder explicitly (forEach can skip DSL receiver).
        composable(AppRoutes.DASHBOARD) { MainAppScaffold(navController) }
        composable(AppRoutes.PROJECTS) { MainAppScaffold(navController) }
        composable(AppRoutes.CHAT) { MainAppScaffold(navController) }
        composable(AppRoutes.SEARCH) { MainAppScaffold(navController) }
        composable(AppRoutes.NOTIFICATIONS) { MainAppScaffold(navController) }
        composable(AppRoutes.PROFILE) { MainAppScaffold(navController) }
        composable(AppRoutes.TASKS) { MainAppScaffold(navController) }
        composable(AppRoutes.EVENTS) { MainAppScaffold(navController) }

        composable(
            route = AppRoutes.PROJECTS_WITH_PROJECT,
            arguments = listOf(
                navArgument("project") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) {
            MainAppScaffold(navController)
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
            val a = entry.arguments!!
            GenericDetailScreen(
                padding = PaddingValues(16.dp),
                navController = navController,
                title = Uri.decode(a.getString("title").orEmpty()),
                subtitle = Uri.decode(a.getString("subtitle").orEmpty()),
                description = Uri.decode(a.getString("description").orEmpty()),
                sourceRoute = Uri.decode(a.getString("sourceRoute").orEmpty()),
            )
        }

        composable(
            route = AppRoutes.COLLABORATOR_PROFILE,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("score") { type = NavType.IntType },
            ),
        ) { entry ->
            val a = entry.arguments!!
            CollaboratorProfileScreen(
                padding = PaddingValues(16.dp),
                navController = navController,
                collaboratorId = a.getString("id").orEmpty(),
                score = a.getInt("score"),
            )
        }
    }
}
