package com.example.developernetworkingapp.data.datasource.firebase

import com.example.developernetworkingapp.data.datasource.firebase.schema.FirestorePaths
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectMemberDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectTaskDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectVisibility
import com.example.developernetworkingapp.data.datasource.firebase.schema.TaskBoardColumn
import com.example.developernetworkingapp.data.datasource.firebase.schema.TaskPriority
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Projects microservice: project documents, members, and task subcollections.
 *
 * Read paths use one-shot [get]; task boards use [observeProjectTasks] for realtime Kanban sync.
 * Writes follow the same timestamp + batch conventions as [FirestoreChatDataSource].
 */
class FirestoreProjectsDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    // region reads

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
        val snap = tasksCollection(projectId).get().await()
        return snap.documents.mapNotNull { doc -> doc.toProjectTaskDoc() }
    }

    fun observeProjectTasks(projectId: String): Flow<List<ProjectTaskDoc>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = tasksCollection(projectId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents
                    ?.mapNotNull { doc -> doc.toProjectTaskDoc() }
                    .orEmpty()
                    .sortedByDescending { it.updatedAt?.toDate()?.time ?: 0L }
                trySend(tasks)
            }
        awaitClose { registration?.remove() }
    }

    // endregion

    // region task writes

    /**
     * Creates a Kanban task under [FirestorePaths.projectTasks].
     * Writes only the task doc — contributors may not update the parent project document (rules).
     *
     * @return New task document id.
     */
    suspend fun createProjectTask(
        projectId: String,
        createdByUserId: String,
        title: String,
        priority: String = TaskPriority.MEDIUM,
        assigneeUserId: String? = null,
        boardColumn: String = TaskBoardColumn.TODO,
    ): String {
        val trimmedTitle = title.trim()
        require(trimmedTitle.isNotEmpty()) { "Task title cannot be empty" }
        require(createdByUserId.isNotBlank()) { "createdByUserId is required" }

        val taskRef = tasksCollection(projectId).document()
        val taskPayload = mapOf(
            "schemaVersion" to ProjectTaskDoc().schemaVersion,
            "title" to trimmedTitle,
            "boardColumn" to boardColumn,
            "priority" to priority,
            "assigneeUserId" to assigneeUserId,
            "createdByUserId" to createdByUserId,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        taskRef.set(taskPayload).await()
        return taskRef.id
    }

    suspend fun updateTaskBoardColumn(
        projectId: String,
        taskId: String,
        boardColumn: String,
    ) {
        require(boardColumn.isNotBlank()) { "boardColumn is required" }
        updateProjectTask(
            projectId = projectId,
            taskId = taskId,
            boardColumn = boardColumn,
        )
    }

    /**
     * Partial update — only non-null arguments are written.
     * Pass [clearAssignee] = true to remove [ProjectTaskDoc.assigneeUserId].
     */
    suspend fun updateProjectTask(
        projectId: String,
        taskId: String,
        title: String? = null,
        priority: String? = null,
        assigneeUserId: String? = null,
        boardColumn: String? = null,
        clearAssignee: Boolean = false,
    ) {
        val patch = buildMap<String, Any> {
            title?.let {
                val trimmed = it.trim()
                require(trimmed.isNotEmpty()) { "Task title cannot be blank" }
                put("title", trimmed)
            }
            priority?.let { put("priority", it) }
            boardColumn?.let { put("boardColumn", it) }
            when {
                clearAssignee -> put("assigneeUserId", FieldValue.delete())
                assigneeUserId != null -> put("assigneeUserId", assigneeUserId)
            }
            put("updatedAt", FieldValue.serverTimestamp())
        }

        taskRef(projectId, taskId).update(patch).await()
    }

    suspend fun deleteProjectTask(projectId: String, taskId: String) {
        taskRef(projectId, taskId).delete().await()
    }

    // endregion

    // region path helpers

    private fun projectRef(projectId: String): DocumentReference =
        db.collection(FirestorePaths.PROJECTS).document(projectId)

    private fun tasksCollection(projectId: String) =
        projectRef(projectId).collection(FirestorePaths.TASKS)

    private fun taskRef(projectId: String, taskId: String): DocumentReference =
        tasksCollection(projectId).document(taskId)

    private fun com.google.firebase.firestore.DocumentSnapshot.toProjectTaskDoc(): ProjectTaskDoc? =
        toObject(ProjectTaskDoc::class.java)?.copy(
            id = id,
            createdAt = readTimestamp("createdAt"),
            updatedAt = readTimestamp("updatedAt"),
        )

    // endregion
}
