package com.example.developernetworkingapp.data.datasource.remote

import com.example.developernetworkingapp.BuildConfig

/**
 * Node analytics microservice base URL — shared config for the whole team.
 *
 * - Debug / emulator → `BuildConfig.API_BASE_URL` (default `http://10.0.2.2:5000/`)
 * - Release → HTTPS URL from Render/Railway (see docs/PRODUCTION_DEPLOYMENT.md)
 * - Physical device on same Wi‑Fi → temporarily change debug `API_BASE_URL` in build.gradle.kts
 */
object DevConnectApiConfig {
    val BASE_URL: String = BuildConfig.API_BASE_URL
    const val ENABLED = true
}
