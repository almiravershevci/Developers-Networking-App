package com.example.developernetworkingapp.di

import android.content.Context
import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.AuthRepositoryImpl
import com.example.developernetworkingapp.data.repository.ChatRepository
import com.example.developernetworkingapp.data.repository.DashboardRepository
import com.example.developernetworkingapp.data.repository.EventsRepository
import com.example.developernetworkingapp.data.repository.FakeChatRepository
import com.example.developernetworkingapp.data.repository.FakeDashboardRepository
import com.example.developernetworkingapp.data.repository.FakeEventsRepository
import com.example.developernetworkingapp.data.repository.FakeNotificationsRepository
import com.example.developernetworkingapp.data.repository.FakeProfileRepository
import com.example.developernetworkingapp.data.repository.FakeProjectsRepository
import com.example.developernetworkingapp.data.repository.FakeSearchRepository
import com.example.developernetworkingapp.data.repository.FakeTasksRepository
import com.example.developernetworkingapp.data.repository.NotificationsRepository
import com.example.developernetworkingapp.data.repository.ProfileRepository
import com.example.developernetworkingapp.data.repository.ProjectsRepository
import com.example.developernetworkingapp.data.repository.SearchRepository
import com.example.developernetworkingapp.data.repository.TasksRepository

object AppContainer {
    private lateinit var authRepo: AuthRepository
    val authRepository: AuthRepository
        get() = authRepo

    val dashboardRepository: DashboardRepository by lazy { FakeDashboardRepository() }
    val projectsRepository: ProjectsRepository by lazy { FakeProjectsRepository() }
    val tasksRepository: TasksRepository by lazy { FakeTasksRepository() }
    val eventsRepository: EventsRepository by lazy { FakeEventsRepository() }
    val chatRepository: ChatRepository by lazy { FakeChatRepository() }
    val searchRepository: SearchRepository by lazy { FakeSearchRepository() }
    val notificationsRepository: NotificationsRepository by lazy { FakeNotificationsRepository() }
    val profileRepository: ProfileRepository by lazy { FakeProfileRepository() }

    fun initialize(context: Context) {
        if (!::authRepo.isInitialized) {
            authRepo = AuthRepositoryImpl(context)
        }
    }
}
