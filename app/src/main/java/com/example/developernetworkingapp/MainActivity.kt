package com.example.developernetworkingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.navigation.MainAppScaffold
import com.example.developernetworkingapp.ui.screens.AdvancedLoginScreen
import com.example.developernetworkingapp.ui.screens.AdvancedSignupScreen
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
    }
}