package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectMemberDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectTaskDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectVisibility
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Projects microservice: project documents, members, and task subcollections.
 */
class FirestoreProjectsDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun fetchProject(projectId: String): ProjectDoc? {
        val snap = db.collection(FirestorePaths.PROJECTS).document(projectId).get().await()
        if (!snap.exists()) return null
        return snap.toObject(ProjectDoc::class.java)?.copy(id = snap.id)
    }

    suspend fun fetchPublicProjects(limit: Long = 12): List<ProjectDoc> {
        val snap = db.collection(FirestorePaths.PROJECTS)
            .whereEqualTo("visibility", ProjectVisibility.PUBLIC)
            .limit(limit)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(ProjectDoc::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun fetchOwnedProjects(ownerUserId: String): List<ProjectDoc> {
        val snap = db.collection(FirestorePaths.PROJECTS)
            .whereEqualTo("ownerUserId", ownerUserId)
            .limit(5)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(ProjectDoc::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun fetchProjectMembers(projectId: String): List<ProjectMemberDoc> {
        val snap = db.collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.MEMBERS)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(ProjectMemberDoc::class.java)?.copy(memberUserId = doc.id)
        }
    }

    suspend fun fetchProjectTasks(projectId: String): List<ProjectTaskDoc> {
        val snap = db.collection(FirestorePaths.PROJECTS)
            .document(projectId)
            .collection(FirestorePaths.TASKS)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(ProjectTaskDoc::class.java)?.copy(id = doc.id)
        }
    }
}
