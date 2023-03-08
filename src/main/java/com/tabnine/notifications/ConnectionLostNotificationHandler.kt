package com.tabnine.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.tabnine.binary.requests.config.CloudConnectionHealthStatus
import com.tabnine.binary.requests.config.StateResponse
import com.tabnine.config.Config
import com.tabnine.general.StaticConfig
import com.tabnine.general.Utils.getHoursDiff
import com.tabnine.lifecycle.BinaryStateChangeNotifier
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

private const val INTERVAL_BETWEEN_NOTIFICATIONS_HOURS = 5
private const val NOTIFICATION_CONTENT =
    "<b>Tabnine lost internet connection.</b>" +
        "<br/>" +
        "If your internet is connected and you're seeing this message, contact Tabnine support."

private const val ON_PREM_NOTIFICATION_CONTENT =
    "<b>Tabnine server connectivity issue.</b>" +
        "<br/>" +
        "Please check your network setup and access to your configured Tabnine Enterprise Host"

class ConnectionLostNotificationHandler {
    private var lastNotificationTime: Date? = null
    private var isRegistered = AtomicBoolean(false)

    fun startConnectionLostListener() {
        if (isRegistered.getAndSet(true)) {
            return
        }
        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(
                BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
                BinaryStateChangeNotifier { stateResponse ->
                    if (shouldShowNotification(stateResponse)) {
                        lastNotificationTime = Date()
                        showNotification()
                    }
                }
            )
    }

    private fun showNotification() {
        val notification = Notification(StaticConfig.TABNINE_NOTIFICATION_GROUP.displayId, StaticConfig.CONNECTION_LOST_NOTIFICATION_ICON, NotificationType.INFORMATION)
        notification
            .setContent(getNotificaitonContent())
            .addAction(object : AnAction("Dismiss") {
                override fun actionPerformed(e: AnActionEvent) {
                    notification.expire()
                }
            })
        Notifications.Bus.notify(notification)
    }

    private fun getNotificaitonContent() = if (Config.IS_ON_PREM) {
        ON_PREM_NOTIFICATION_CONTENT
    } else {
        NOTIFICATION_CONTENT
    }

    private fun shouldShowNotification(stateResponse: StateResponse): Boolean {
        return stateResponse.cloudConnectionHealthStatus == CloudConnectionHealthStatus.Failed &&
            (lastNotificationTime == null || getHoursDiff(lastNotificationTime, Date()) > INTERVAL_BETWEEN_NOTIFICATIONS_HOURS)
    }
}
