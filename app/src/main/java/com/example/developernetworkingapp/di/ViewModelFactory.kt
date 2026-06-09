package com.example.developernetworkingapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.developernetworkingapp.ui.navigation.AppNavigationViewModel
import com.example.developernetworkingapp.ui.viewmodel.AdminViewModel
import com.example.developernetworkingapp.ui.viewmodel.CollaboratorProfileViewModel
import com.example.developernetworkingapp.ui.viewmodel.ChatViewModel
import com.example.developernetworkingapp.ui.viewmodel.ConversationViewModel
import com.example.developernetworkingapp.ui.viewmodel.DashboardViewModel
import com.example.developernetworkingapp.ui.viewmodel.EventsViewModel
import com.example.developernetworkingapp.ui.viewmodel.LoginViewModel
import com.example.developernetworkingapp.ui.viewmodel.MainShellViewModel
import com.example.developernetworkingapp.ui.viewmodel.NotificationsViewModel
import com.example.developernetworkingapp.ui.viewmodel.ProfileViewModel
import com.example.developernetworkingapp.ui.viewmodel.ProjectsViewModel
import com.example.developernetworkingapp.ui.viewmodel.SearchViewModel
import com.example.developernetworkingapp.ui.viewmodel.SessionViewModel
import com.example.developernetworkingapp.ui.viewmodel.SettingsViewModel
import com.example.developernetworkingapp.ui.viewmodel.SignupViewModel
import com.example.developernetworkingapp.ui.viewmodel.TasksViewModel
import com.example.developernetworkingapp.ui.viewmodel.VerificationViewModel

class AppViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SessionViewModel::class.java) ->
                SessionViewModel(AppContainer.authRepository) as T
            modelClass.isAssignableFrom(AppNavigationViewModel::class.java) ->
                AppNavigationViewModel(AppContainer.authRepository) as T
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(AppContainer.authRepository) as T
            modelClass.isAssignableFrom(SignupViewModel::class.java) ->
                SignupViewModel(AppContainer.authRepository) as T
            modelClass.isAssignableFrom(VerificationViewModel::class.java) ->
                VerificationViewModel(AppContainer.authRepository) as T
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(
                    AppContainer.dashboardRepository,
                    AppContainer.matchRepository,
                    AppContainer.projectJoinRepository,
                    AppContainer.projectsRepository,
                ) as T
            modelClass.isAssignableFrom(ProjectsViewModel::class.java) ->
                ProjectsViewModel(
                    AppContainer.projectsRepository,
                    AppContainer.dashboardRepository,
                    AppContainer.tasksRepository,
                    AppContainer.projectJoinRepository,
                ) as T
            modelClass.isAssignableFrom(ChatViewModel::class.java) ->
                ChatViewModel(
                    AppContainer.chatRepository,
                    AppContainer.chatMuteStore,
                ) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(
                    AppContainer.searchRepository,
                    AppContainer.techTrendsRepository,
                ) as T
            modelClass.isAssignableFrom(NotificationsViewModel::class.java) ->
                NotificationsViewModel(AppContainer.notificationsRepository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(
                    AppContainer.profileRepository,
                    AppContainer.authRepository,
                ) as T
            modelClass.isAssignableFrom(TasksViewModel::class.java) ->
                TasksViewModel(
                    AppContainer.tasksRepository,
                    AppContainer.projectsRepository,
                    AppContainer.notificationDispatcher,
                ) as T
            modelClass.isAssignableFrom(EventsViewModel::class.java) ->
                EventsViewModel(AppContainer.eventsRepository) as T
            modelClass.isAssignableFrom(AdminViewModel::class.java) ->
                AdminViewModel(AppContainer.adminRepository) as T
            modelClass.isAssignableFrom(MainShellViewModel::class.java) ->
                MainShellViewModel(AppContainer.notificationsRepository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(
                    AppContainer.settingsStore,
                    AppContainer.authRepository,
                ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

class CollaboratorProfileViewModelFactory(
    private val userId: String,
    private val matchScore: Int,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CollaboratorProfileViewModel::class.java)) {
            return CollaboratorProfileViewModel(
                userId = userId,
                matchScore = matchScore,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class ConversationViewModelFactory(
    private val conversationId: String,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
            return ConversationViewModel(
                conversationId = conversationId,
                repository = AppContainer.chatRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
