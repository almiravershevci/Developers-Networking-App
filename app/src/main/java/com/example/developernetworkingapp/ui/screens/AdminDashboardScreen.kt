package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.developernetworkingapp.data.repository.UserRole
import com.example.developernetworkingapp.di.appViewModel
import com.example.developernetworkingapp.ui.components.EmptyStateCard
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.screens.admin.AdminDashboardNavHost
import com.example.developernetworkingapp.ui.theme.AppDesignTokens
import androidx.compose.ui.unit.dp
import com.example.developernetworkingapp.ui.viewmodel.AdminViewModel
import com.example.developernetworkingapp.ui.viewmodel.SessionViewModel

@Composable
fun AdminDashboardRoute(padding: PaddingValues, navController: NavController) {
    val sessionViewModel: SessionViewModel = appViewModel()
    val adminViewModel: AdminViewModel = appViewModel()
    val currentUser by sessionViewModel.currentUser.collectAsStateWithLifecycle()
    val isAdmin = currentUser?.role == UserRole.ADMIN

    when {
        currentUser == null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(AppDesignTokens.screenHorizontalPadding),
                verticalArrangement = Arrangement.Center,
            ) {
                LoadingAccessCard("Checking admin access…")
            }
        }
        isAdmin -> {
            AdminDashboardNavHost(
                outerPadding = padding,
                outerNavController = navController,
                adminName = currentUser?.name.orEmpty(),
                viewModel = adminViewModel,
            )
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(AppDesignTokens.screenHorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                EmptyStateCard(
                    title = "Admin access required",
                    subtitle = "Sign up or log in with admin@devconnect.app to unlock the admin console. " +
                        "In Firebase Console, set users/{your-uid}.accountRole to \"admin\" for an existing account.",
                )
                Button(onClick = { navController.navigate(AppRoutes.DASHBOARD) }) {
                    Text("Back to dashboard")
                }
            }
        }
    }
}

@Composable
private fun LoadingAccessCard(message: String) {
    EmptyStateCard(title = message, subtitle = "Please wait a moment.")
}
