package com.example.developernetworkingapp.data.datasource.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Reads Firestore Timestamp fields or seed `*_iso` string fallbacks. */
fun DocumentSnapshot.readTimestamp(
    field: String,
    isoFallbackField: String = "${field}_iso",
): Timestamp? {
    getTimestamp(field)?.let { return it }
    val iso = getString(isoFallbackField) ?: return null
    return parseIsoTimestamp(iso)
}

fun parseIsoTimestamp(iso: String): Timestamp? {
    return runCatching {
        val millis = if (android.os.Build.VERSION.SDK_INT >= 26) {
            java.time.Instant.parse(iso).toEpochMilli()
        } else {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            format.parse(iso)?.time
        } ?: return null
        Timestamp(millis / 1000, ((millis % 1000) * 1_000_000).toInt())
    }.getOrNull()
}
