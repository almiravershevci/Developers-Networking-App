package com.example.developernetworkingapp.di

import android.content.Context
import com.example.developernetworkingapp.data.datasource.remote.DashboardRemoteDataSource
import com.example.developernetworkingapp.data.datasource.remote.DevConnectApi
import com.example.developernetworkingapp.data.datasource.remote.DevConnectApiConfig
import com.example.developernetworkingapp.data.datasource.remote.FirebaseAuthInterceptor
import com.example.developernetworkingapp.data.datasource.remote.TechTrendsApi
import com.example.developernetworkingapp.data.repository.ApiTechTrendsRepository
import com.example.developernetworkingapp.data.repository.AdminRepository
import com.example.developernetworkingapp.data.repository.AuthRepository
import com.example.developernetworkingapp.data.repository.impl.AuthRepositoryFirebase
import com.example.developernetworkingapp.data.repository.impl.AdminRepositoryFirestore
import com.example.developernetworkingapp.data.local.ChatMuteStore
import com.example.developernetworkingapp.data.local.SettingsStore
import com.example.developernetworkingapp.data.repository.ChatRepository
import com.example.developernetworkingapp.data.repository.DashboardRepository
import com.example.developernetworkingapp.data.repository.EventsRepository
import com.example.developernetworkingapp.data.repository.impl.ChatRepositoryFirestore
import com.example.developernetworkingapp.data.repository.impl.DashboardRepositoryFirestore
import com.example.developernetworkingapp.data.repository.impl.EventsRepositoryFirestore
import com.example.developernetworkingapp.data.repository.impl.NotificationsRepositoryFirestore
import com.example.developernetworkingapp.data.repository.impl.ProfileRepositoryFirestore
import com.example.developernetworkingapp.data.repository.impl.SearchRepositoryFirestore
import com.example.developernetworkingapp.data.repository.impl.ProjectsRepositoryFirestore
import com.example.developernetworkingapp.data.repository.impl.MatchRepositoryFirestore
import com.example.developernetworkingapp.data.repository.impl.TasksRepositoryFirestore
import com.example.developernetworkingapp.data.repository.MatchRepository
import com.example.developernetworkingapp.data.repository.NotificationDispatcher
import com.example.developernetworkingapp.data.repository.NotificationsRepository
import com.example.developernetworkingapp.data.repository.ProfileRepository
import com.example.developernetworkingapp.data.repository.ProjectsRepository
import com.example.developernetworkingapp.data.repository.SearchRepository
import com.example.developernetworkingapp.data.repository.TasksRepository
import com.example.developernetworkingapp.data.repository.TechTrendsRepository
import com.example.developernetworkingapp.notifications.FcmTokenRegistrar
import com.example.developernetworkingapp.notifications.LocalNotificationDispatcher
import com.example.developernetworkingapp.notifications.NotificationChannels
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppContainer {
    private lateinit var authRepo: AuthRepository
    private lateinit var notificationDispatcherImpl: NotificationDispatcher
    private lateinit var chatMuteStoreImpl: ChatMuteStore
    private lateinit var settingsStoreImpl: SettingsStore
    private lateinit var fcmTokenRegistrar: FcmTokenRegistrar
    val authRepository: AuthRepository
        get() = authRepo
    val notificationDispatcher: NotificationDispatcher
        get() = notificationDispatcherImpl

    val dashboardRepository: DashboardRepository by lazy {
        DashboardRepositoryFirestore(
            authRepository = authRepository,
            remoteDataSource = dashboardRemoteDataSource,
        )
    }
    val projectsRepository: ProjectsRepository by lazy {
        ProjectsRepositoryFirestore(authRepository = authRepository)
    }
    val tasksRepository: TasksRepository by lazy { TasksRepositoryFirestore() }
    val matchRepository: MatchRepository by lazy { MatchRepositoryFirestore() }
    val eventsRepository: EventsRepository by lazy { EventsRepositoryFirestore() }
    val chatRepository: ChatRepository by lazy { ChatRepositoryFirestore() }
    val chatMuteStore: ChatMuteStore
        get() = chatMuteStoreImpl
    val settingsStore: SettingsStore
        get() = settingsStoreImpl
    val searchRepository: SearchRepository by lazy { SearchRepositoryFirestore() }
    val notificationsRepository: NotificationsRepository by lazy { NotificationsRepositoryFirestore() }
    val profileRepository: ProfileRepository by lazy {
        ProfileRepositoryFirestore(authRepository = authRepository)
    }
    val techTrendsRepository: TechTrendsRepository by lazy { ApiTechTrendsRepository(techTrendsApi) }
    val adminRepository: AdminRepository by lazy { AdminRepositoryFirestore() }

    private val devConnectApi: DevConnectApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(FirebaseAuthInterceptor())
            .build()
        Retrofit.Builder()
            .baseUrl(DevConnectApiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DevConnectApi::class.java)
    }

    private val dashboardRemoteDataSource: DashboardRemoteDataSource by lazy {
        DashboardRemoteDataSource(devConnectApi)
    }

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
            authRepo = AuthRepositoryFirebase(context)
            notificationDispatcherImpl = LocalNotificationDispatcher(context)
            chatMuteStoreImpl = ChatMuteStore(context)
            settingsStoreImpl = SettingsStore(context)
            NotificationChannels.ensureCreated(context)
            fcmTokenRegistrar = FcmTokenRegistrar(context)
            fcmTokenRegistrar.start()
        }
    }
}
