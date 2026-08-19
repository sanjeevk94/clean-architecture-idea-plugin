package com.korina.cleanarch.idea.notifications

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

/**
 * Thin wrapper around [NotificationGroupManager] that posts balloon notifications
 * for generation success and failure events.
 *
 * The notification group `"CleanArch.Notifications"` is registered in `plugin.xml`.
 */
object CleanArchNotifier {

    private const val GROUP_ID = "CleanArch.Notifications"

    /**
     * Shows a balloon indicating successful code generation.
     *
     * @param project       The active IntelliJ project.
     * @param featureName   The feature name from the schema (e.g. "Product").
     * @param fileCount     Number of files written to disk.
     */
    fun notifySuccess(project: Project, featureName: String, fileCount: Int) {
        notify(
            project  = project,
            title    = "Clean Architecture Generator",
            content  = "✅ <b>$featureName</b> generated — $fileCount file(s) written.",
            type     = NotificationType.INFORMATION
        )
    }

    /**
     * Shows a balloon indicating that file generation failed.
     *
     * @param project       The active IntelliJ project.
     * @param featureName   The feature name attempted.
     */
    fun notifyFailure(project: Project, featureName: String) {
        notify(
            project  = project,
            title    = "Clean Architecture Generator",
            content  = "❌ Generation of <b>$featureName</b> failed. " +
                       "See the Clean Architecture tool window for details.",
            type     = NotificationType.ERROR
        )
    }

    /**
     * Shows an informational balloon for schema validation warnings or
     * other advisory messages.
     */
    fun notifyInfo(project: Project, message: String) {
        notify(
            project  = project,
            title    = "Clean Architecture Generator",
            content  = message,
            type     = NotificationType.WARNING
        )
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private fun notify(
        project: Project,
        title:   String,
        content: String,
        type:    NotificationType
    ) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(title, content, type)
            .notify(project)
    }
}
