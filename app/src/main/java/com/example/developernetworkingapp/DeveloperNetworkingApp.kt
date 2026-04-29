package com.example.developernetworkingapp

import android.app.Application
import com.example.developernetworkingapp.di.AppContainer

class DeveloperNetworkingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
    }
}
