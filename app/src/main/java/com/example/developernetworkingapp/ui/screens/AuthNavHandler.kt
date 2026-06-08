package com.example.developernetworkingapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.example.developernetworkingapp.di.appViewModel
import com.example.developernetworkingapp.ui.event.AuthNavEvent
import com.example.developernetworkingapp.ui.event.ProfileNavEvent
import com.example.developernetworkingapp.ui.navigation.AppRoutes
import com.example.developernetworkingapp.ui.viewmodel.LoginViewModel
import com.example.developernetworkingapp.ui.viewmodel.ProfileViewModel
import com.example.developernetworkingapp.ui.viewmodel.SignupViewModel
import com.example.developernetworkingapp.ui.viewmodel.VerificationViewModel
import kotlinx.coroutines.flow.merge

@Composable
fun CollectAuthNavEvents(navController: NavController) {
    val loginViewModel: LoginViewModel = appViewModel()
    val signupViewModel: SignupViewModel = appViewModel()
    val verificationViewModel: VerificationViewModel = appViewModel()

    LaunchedEffect(loginViewModel, signupViewModel, verificationViewModel, navController) {
        merge(
            loginViewModel.navigationEvents,
            signupViewModel.navigationEvents,
            verificationViewModel.navigationEvents,
        ).collect { event ->
            when (event) {
                AuthNavEvent.NavigateToDashboard ->
                    navController.navigate(AppRoutes.DASHBOARD) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                is AuthNavEvent.NavigateToVerifyEmail ->
                    navController.navigate(AppRoutes.verifyEmailRoute(event.email)) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                AuthNavEvent.NavigateToLogin ->
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
            }
        }
    }
}

@Composable
fun CollectProfileNavEvents(navController: NavController) {
    val profileViewModel: ProfileViewModel = appViewModel()

    LaunchedEffect(profileViewModel, navController) {
        profileViewModel.navigationEvents.collect { event ->
            when (event) {
                ProfileNavEvent.LoggedOut ->
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
            }
        }
    }
}
