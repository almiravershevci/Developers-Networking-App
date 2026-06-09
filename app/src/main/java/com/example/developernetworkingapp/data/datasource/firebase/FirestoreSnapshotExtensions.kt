package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.UserProfileDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.UserStatsDoc
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.toUserProfileDocSafe(): UserProfileDoc? {
    if (!exists()) return null
    val base = toObject(UserProfileDoc::class.java) ?: return null
    return base.copy(
        id = id,
        skillTags = readStringList("skillTags"),
        fcmTokens = readStringList("fcmTokens"),
    )
}

internal fun DocumentSnapshot.toUserStatsDocSafe(): UserStatsDoc? {
    if (!exists()) return null
    val base = toObject(UserStatsDoc::class.java) ?: return null
    return base.copy(
        userId = id,
        updatedAt = readTimestamp("updatedAt"),
    )
}

internal fun DocumentSnapshot.toProjectDocSafe(): ProjectDoc? {
    if (!exists()) return null
    val base = toObject(ProjectDoc::class.java) ?: return null
    return base.copy(
        id = id,
        stackTags = readStringList("stackTags"),
        openRoleLabels = readStringList("openRoleLabels"),
        searchKeywords = readStringList("searchKeywords"),
        createdAt = readTimestamp("createdAt"),
        updatedAt = readTimestamp("updatedAt"),
    )
}

private fun DocumentSnapshot.readStringList(field: String): List<String> {
    val raw = get(field) as? List<*> ?: return emptyList()
    return raw.filterIsInstance<String>()
}
