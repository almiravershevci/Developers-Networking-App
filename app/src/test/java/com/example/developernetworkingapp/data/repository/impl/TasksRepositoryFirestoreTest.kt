package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.datasource.firebase.FirestoreProjectsDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectTaskDoc
import com.example.developernetworkingapp.data.datasource.firebase.schema.TaskBoardColumn
import com.example.developernetworkingapp.data.datasource.firebase.schema.TaskPriority
import com.example.developernetworkingapp.data.repository.mapping.TaskItemMapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TasksRepositoryFirestoreTest {

    private val projectsDataSource: FirestoreProjectsDataSource = mock()
    private val userDataSource: FirestoreUserDataSource = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseUser: FirebaseUser = mock()

    private lateinit var repository: TasksRepositoryFirestore

    @Before
    fun setUp() {
        repository = TasksRepositoryFirestore(
            projectsDataSource = projectsDataSource,
            userDataSource = userDataSource,
            firebaseAuth = firebaseAuth,
        )
    }

    @Test
    fun moveTask_callsDataSourceWithCorrectBoardColumn() = runTest {
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("user_1")
        whenever(firebaseUser.isEmailVerified).thenReturn(true)
        whenever(projectsDataSource.fetchOwnedProjects("user_1"))
            .thenReturn(listOf(ProjectDoc(id = "proj_owned")))

        val result = repository.moveTask("task_42", TaskBoardColumn.IN_PROGRESS)

        assertTrue(result.isSuccess)
        verify(projectsDataSource).updateTaskBoardColumn(
            projectId = "proj_owned",
            taskId = "task_42",
            boardColumn = TaskBoardColumn.IN_PROGRESS,
        )
    }

    @Test
    fun moveTask_returnsFailureWhenSignedOut() = runTest {
        whenever(firebaseAuth.currentUser).thenReturn(null)

        val result = repository.moveTask("task_42", TaskBoardColumn.DONE)

        assertTrue(result.isFailure)
        assertEquals(
            "Sign in with a verified email to manage tasks.",
            result.exceptionOrNull()?.message,
        )
    }

    @Test
    fun moveTask_returnsFailureWhenEmailNotVerified() = runTest {
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.isEmailVerified).thenReturn(false)

        val result = repository.moveTask("task_42", TaskBoardColumn.DONE)

        assertTrue(result.isFailure)
        assertEquals(
            "Sign in with a verified email to manage tasks.",
            result.exceptionOrNull()?.message,
        )
    }

    @Test
    fun taskItemMapper_mapsProjectTaskDocCorrectly() {
        val task = ProjectTaskDoc(
            id = "task_1",
            title = "Wire chat notifications",
            boardColumn = TaskBoardColumn.IN_PROGRESS,
            priority = TaskPriority.HIGH,
            assigneeUserId = "user_1",
        )

        val item = TaskItemMapper.fromDoc(
            task = task,
            currentUserId = "user_1",
            assigneeName = "Jane Dev",
        )

        assertEquals("task_1", item.id)
        assertEquals("Wire chat notifications", item.title)
        assertEquals(TaskPriority.HIGH, item.priority)
        assertEquals("You", item.assigneeLabel)
        assertEquals("In Progress", item.statusLabel)
        assertEquals(
            "Wire chat notifications - High - Assignee: You - In Progress",
            item.displayLine,
        )
    }

    @Test
    fun taskItemMapper_marksUnassignedTasksCorrectly() {
        val task = ProjectTaskDoc(
            id = "task_2",
            title = "Draft API contract",
            boardColumn = TaskBoardColumn.TODO,
            priority = TaskPriority.MEDIUM,
            assigneeUserId = null,
        )

        val item = TaskItemMapper.fromDoc(
            task = task,
            currentUserId = "user_1",
            assigneeName = null,
        )

        assertEquals("Unassigned", item.assigneeLabel)
        assertEquals("To Do", item.statusLabel)
        assertFalse(item.displayLine.contains("Teammate"))
    }
}
