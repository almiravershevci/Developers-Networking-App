package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.AdminRepository
import com.example.developernetworkingapp.di.AppContainer
import com.example.developernetworkingapp.domain.model.AdminPermissionPreset
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: AdminRepository = AppContainer.adminRepository
) : ViewModel() {

    val dashboard = repository.snapshot.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = repository.snapshot.value
    )

    private val _userMessages = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val userMessages = _userMessages.asSharedFlow()

    private fun message(text: String) {
        viewModelScope.launch { _userMessages.emit(text) }
    }

    fun deactivateUser(id: String) {
        repository.deactivateUser(id)
        message("User deactivated.")
    }

    fun activateUser(id: String) {
        repository.activateUser(id)
        message("User activated.")
    }

    fun banUser(id: String) {
        repository.banUser(id)
        message("User banned.")
    }

    fun updateUserProfile(id: String, techStack: String, bio: String) {
        repository.updateUserProfile(id, techStack, bio)
        message("Profile updated.")
    }

    fun approveProject(id: String) {
        repository.approveProject(id)
        message("Project approved.")
    }

    fun rejectProject(id: String) {
        repository.rejectProject(id)
        message("Project rejected.")
    }

    fun archiveProject(id: String) {
        repository.archiveProject(id)
        message("Project archived.")
    }

    fun updateProject(id: String, name: String, techSummary: String) {
        repository.updateProject(id, name, techSummary)
        message("Project saved.")
    }

    fun approveAllQueuedContent() {
        repository.approveAllQueuedContent()
        message("Queued posts approved.")
    }

    fun holdQueuedContentForReview() {
        repository.holdQueuedContentForReview()
        message("Queue flagged for extended review.")
    }

    fun removeReportedContent(reportId: String) {
        repository.removeReportedContent(reportId)
        message("Content removed.")
    }

    fun dismissReport(reportId: String) {
        repository.dismissReport(reportId)
        message("Report dismissed.")
    }

    fun sendNotification(title: String, body: String, audience: String) {
        repository.sendNotification(title, body, audience)
        message("Notification sent.")
    }

    fun schedulePush() {
        repository.scheduleNextPushSlot()
        message("Push slot scheduled.")
    }

    fun exportAnalyticsCsv(): String {
        val csv = repository.exportAnalyticsCsv()
        message("Analytics export generated (${csv.lineSequence().count()} lines).")
        return csv
    }

    fun bulkExportUsersCsv(): String {
        val csv = repository.bulkExportUsersCsv()
        message("User export generated (${csv.lineSequence().count()} lines).")
        return csv
    }

    fun setDefaultNotifications(enabled: Boolean) {
        repository.setDefaultNotifications(enabled)
        message("Notification defaults saved.")
    }

    fun setStrictEncryption(enabled: Boolean) {
        repository.setStrictEncryption(enabled)
        message("Encryption policy saved.")
    }

    fun setAnalyticsSharing(enabled: Boolean) {
        repository.setAnalyticsSharing(enabled)
        message("Analytics preference saved.")
    }

    fun rotateApiKeys() {
        repository.rotateIntegrationKeys()
        message("Keys rotated (mock).")
    }

    fun saveThemeDraft() {
        repository.saveThemeDraft()
        message("Theme draft saved.")
    }

    fun assignTicket(id: String) {
        repository.assignTicket(id)
        message("Ticket assigned.")
    }

    fun closeTicket(id: String) {
        repository.closeTicket(id)
        message("Ticket closed.")
    }

    fun addHelpArticle() {
        repository.addHelpArticleStub()
        message("Help article draft created.")
    }

    fun updateAdminPreset(adminId: String, preset: AdminPermissionPreset) {
        repository.updateAdminPreset(adminId, preset)
        message("Permissions updated.")
    }
}
