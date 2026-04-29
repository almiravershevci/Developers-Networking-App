package com.example.developernetworkingapp.di

import android.content.Context
import com.example.developernetworkingapp.data.remote.TechTrendsApi
import com.example.developernetworkingapp.data.repository.ApiTechTrendsRepository
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
import com.example.developernetworkingapp.data.repository.NotificationDispatcher
import com.example.developernetworkingapp.data.repository.NotificationsRepository
import com.example.developernetworkingapp.data.repository.ProfileRepository
import com.example.developernetworkingapp.data.repository.ProjectsRepository
import com.example.developernetworkingapp.data.repository.SearchRepository
import com.example.developernetworkingapp.data.repository.TasksRepository
import com.example.developernetworkingapp.data.repository.TechTrendsRepository
import com.example.developernetworkingapp.notifications.LocalNotificationDispatcher
import com.example.developernetworkingapp.notifications.NotificationChannels
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppContainer {
    private lateinit var authRepo: AuthRepository
    private lateinit var notificationDispatcherImpl: NotificationDispatcher
    val authRepository: AuthRepository
        get() = authRepo
    val notificationDispatcher: NotificationDispatcher
        get() = notificationDispatcherImpl

    val dashboardRepository: DashboardRepository by lazy { FakeDashboardRepository() }
    val projectsRepository: ProjectsRepository by lazy { FakeProjectsRepository() }
    val tasksRepository: TasksRepository by lazy { FakeTasksRepository() }
    val eventsRepository: EventsRepository by lazy { FakeEventsRepository() }
    val chatRepository: ChatRepository by lazy { FakeChatRepository() }
    val searchRepository: SearchRepository by lazy { FakeSearchRepository() }
    val notificationsRepository: NotificationsRepository by lazy { FakeNotificationsRepository() }
    val profileRepository: ProfileRepository by lazy { FakeProfileRepository() }
    val techTrendsRepository: TechTrendsRepository by lazy { ApiTechTrendsRepository(techTrendsApi) }

    private val techTrendsApi: TechTrendsApi by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        Retrofit.Builder()
            .baseUrl("https://hn.algolia.com/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TechTrendsApi::class.java)
    }

    fun initialize(context: Context) {
        if (!::authRepo.isInitialized) {
            authRepo = AuthRepositoryImpl(context)
            notificationDispatcherImpl = LocalNotificationDispatcher(context)
            NotificationChannels.ensureCreated(context)
        }
    }
}
