package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectTaskDoc
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Tasks microservice: collection group query with project-level fallback.
 */
class FirestoreTasksDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val projectsDataSource: FirestoreProjectsDataSource = FirestoreProjectsDataSource(),
) {
    suspend fun fetchRecentTasks(fallbackProjectId: String = DEFAULT_PROJECT_ID): List<ProjectTaskDoc> {
        val fromGroup = runCatching {
            db.collectionGroup(FirestorePaths.TASKS)
                .limit(40)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(ProjectTaskDoc::class.java)?.copy(id = doc.id)
                }
        }.getOrDefault(emptyList())

        if (fromGroup.isNotEmpty()) return fromGroup

        return runCatching {
            projectsDataSource.fetchProjectTasks(fallbackProjectId)
        }.getOrDefault(emptyList())
    }

    private companion object {
        const val DEFAULT_PROJECT_ID = "proj_devconnect_mobile"
    }
}
