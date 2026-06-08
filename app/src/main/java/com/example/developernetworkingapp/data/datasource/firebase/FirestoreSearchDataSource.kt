package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectVisibility
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Search microservice: discoverable public projects (and profile lookup helpers).
 */
class FirestoreSearchDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val projectsDataSource: FirestoreProjectsDataSource = FirestoreProjectsDataSource(),
) {
    fun observePublicProjects(): Flow<List<ProjectDoc>> = callbackFlow {
        val registration = db.collection(FirestorePaths.PROJECTS)
            .whereEqualTo("visibility", ProjectVisibility.PUBLIC)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val projects = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(ProjectDoc::class.java)?.copy(
                            id = doc.id,
                            updatedAt = doc.readTimestamp("updatedAt"),
                        )
                    }.orEmpty()
                    .sortedByDescending { it.updatedAt?.toDate()?.time ?: 0L }
                trySend(projects)
            }
        awaitClose { registration.remove() }
    }

    suspend fun fetchPublicProjectsOnce(): List<ProjectDoc> =
        projectsDataSource.fetchPublicProjects(limit = 24)
}
