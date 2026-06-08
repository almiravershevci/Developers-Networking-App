package com.example.developernetworkingapp.data.datasource.remote

/**
 * Node analytics microservice base URL — shared config for the whole team.
 *
 * - Android emulator → `http://10.0.2.2:5000/` (host machine localhost)
 * - Physical device on same Wi‑Fi → `http://<your-lan-ip>:5000/`
 * - Deployed (Render/Railway) → set the team URL here once
 */
object DevConnectApiConfig {
    const val BASE_URL = "http://10.0.2.2:5000/"
    const val ENABLED = true
}
