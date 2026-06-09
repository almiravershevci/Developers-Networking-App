package com.example.developernetworkingapp.data.repository.impl

import com.example.developernetworkingapp.data.repository.SearchRepository
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreSearchDataSource
import com.example.developernetworkingapp.data.datasource.firebase.FirestoreUserDataSource
import com.example.developernetworkingapp.data.datasource.firebase.authStateChanges
import com.example.developernetworkingapp.data.datasource.firebase.schema.LocationKind
import com.example.developernetworkingapp.data.datasource.firebase.schema.ProjectDoc
import com.example.developernetworkingapp.domain.model.SearchContent
import com.example.developernetworkingapp.domain.model.SearchResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Talent search: public Firestore projects indexed for client-side filtering.
 */
class SearchRepositoryFirestore(
    private val searchDataSource: FirestoreSearchDataSource = FirestoreSearchDataSource(),
    private val userDataSource: FirestoreUserDataSource = FirestoreUserDataSource(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : SearchRepository {

    override fun observeSearch(): Flow<SearchContent> =
        firebaseAuth.authStateChanges().flatMapLatest { firebaseUser ->
            when {
                firebaseUser == null -> flow {
                    emit(SearchContent(statusMessage = "Sign in to search projects and collaborators."))
                }
                !firebaseUser.isEmailVerified -> flow {
                    emit(SearchContent(statusMessage = "Verify your email to use talent search."))
                }
                else -> searchDataSource.observePublicProjects()
                    .flatMapLatest { projects ->
                        flow { emit(buildSearchContent(projects)) }
                    }
                    .catch { error ->
                        emit(
                            SearchContent(
                                statusMessage = "Couldn't load search catalog. Check rules and seed projects. (${error.message})",
                            ),
                        )
                    }
            }
        }.flowOn(Dispatchers.IO)

    private suspend fun buildSearchContent(projects: List<ProjectDoc>): SearchContent {
        if (projects.isEmpty()) {
            return SearchContent(
                statusMessage = "No public projects in Firestore. Seed the projects collection or publish visibility=public.",
            )
        }

        val ownerIds = projects.map { it.ownerUserId }.distinct()
        val owners = runCatching { userDataSource.fetchUserProfiles(ownerIds) }.getOrDefault(emptyMap())

        val results = projects.map { project ->
            val ownerName = owners[project.ownerUserId]?.displayName?.takeIf { it.isNotBlank() }
                ?: "Project owner"
            project.toSearchResult(ownerName)
        }

        return SearchContent(
            filters = buildQuickFilters(projects),
            results = results,
        )
    }

    private fun buildQuickFilters(projects: List<ProjectDoc>): List<String> {
        val fromTags = projects.flatMap { it.stackTags }.map { tag ->
            tag.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        val fromRoles = projects.flatMap { it.openRoleLabels }
        val fromLocation = projects.map { project ->
            when (project.locationKind) {
                LocationKind.REMOTE -> "Remote"
                LocationKind.HYBRID -> "Hybrid"
                LocationKind.ONSITE -> project.cityName?.takeIf { it.isNotBlank() } ?: "Onsite"
                else -> project.locationKind
            }
        }
        return (fromTags + fromRoles + fromLocation)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(12)
    }

    private fun ProjectDoc.toSearchResult(ownerName: String): SearchResult = SearchResult(
        projectId = id,
        title = title,
        subtitle = subtitle.ifBlank { description.take(80) },
        stack = primaryStackLabel.ifBlank { stackTags.joinToString(" · ") },
        owner = ownerName,
        location = formatLocation(),
        rolesNeeded = openRoleLabels,
        membersCount = memberCount.coerceAtLeast(1),
        description = description,
    )

    private fun ProjectDoc.formatLocation(): String = when (locationKind) {
        LocationKind.REMOTE -> "Remote"
        LocationKind.HYBRID -> "Hybrid"
        LocationKind.ONSITE -> cityName?.takeIf { it.isNotBlank() } ?: "Onsite"
        else -> locationKind
    }

}
