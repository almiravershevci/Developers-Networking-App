package com.example.developernetworkingapp.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.developernetworkingapp.data.repository.UserRole
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.screens.admin.AdminDashboardNavHost
import com.example.developernetworkingapp.ui.viewmodel.AdminViewModel
import com.example.developernetworkingapp.ui.viewmodel.SessionViewModel

@Composable
fun AdminDashboardRoute(padding: PaddingValues, navController: NavController) {
    val sessionViewModel: SessionViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()
    val currentUser by sessionViewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser?.role, currentUser?.email) {
        val u = currentUser ?: return@LaunchedEffect
        if (u.role != UserRole.ADMIN) {
            navController.navigate(AppRoutes.DASHBOARD) {
                popUpTo(AppRoutes.ADMIN_DASHBOARD) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    if (currentUser?.role == UserRole.ADMIN) {
        AdminDashboardNavHost(
            outerPadding = padding,
            outerNavController = navController,
            adminName = currentUser?.name.orEmpty(),
            viewModel = adminViewModel
        )
    }
}
