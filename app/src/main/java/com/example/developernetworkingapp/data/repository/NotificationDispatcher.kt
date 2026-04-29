package com.example.developernetworkingapp.data.repository

interface NotificationDispatcher {
    fun showLocalNotification(title: String, message: String)
}
