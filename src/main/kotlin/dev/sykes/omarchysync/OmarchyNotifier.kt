package dev.sykes.omarchysync

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

/** Thin helper around the plugin's notification group. */
object OmarchyNotifier {

    private const val GROUP_ID = "Omarchy Theme Sync"

    fun info(project: Project?, title: String, content: String) =
        notify(project, title, content, NotificationType.INFORMATION)

    fun warn(project: Project?, title: String, content: String) =
        notify(project, title, content, NotificationType.WARNING)

    private fun notify(project: Project?, title: String, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(title, content, type)
            .notify(project)
    }
}
