package com.example.developernetworkingapp.ui.util

/**
 * Rewrites developer-only Firestore/seed hints into user-facing copy for demos.
 */
fun userFacingStatusMessage(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val lower = raw.lowercase()
    return when {
        "firestore" in lower || "seed" in lower || "recipientuserid" in lower ||
            "subcollection" in lower || "proj_" in lower || "rules" in lower ->
            "Your live data will appear here once your account is connected to a project workspace."
        else -> raw
    }
}
