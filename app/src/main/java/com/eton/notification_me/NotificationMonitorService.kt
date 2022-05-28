package com.eton.notification_me

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.graphics.drawable.toBitmap


class NotificationMonitorService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.apply {
            val extras = notification.extras
            val packageName = packageName // 取得應用程式包名

            val title = extras.getString(Notification.EXTRA_TITLE) // 取得通知欄標題

            val text = extras.getString(Notification.EXTRA_TEXT) // 取得通知欄文字

            // 取得通知欄的小圖示
            val smallIcon = notification.smallIcon?.loadDrawable(this@NotificationMonitorService)
            // 取得通知欄的大圖示
            val largeIcon =
                notification.getLargeIcon()?.loadDrawable(this@NotificationMonitorService)
                    ?.toBitmap()
//            MainActivity.show(packageName, title, text, smallIcon, largeIcon)
            NotificationUtils().sendNotification(
                this@NotificationMonitorService,
                packageName,
                text ?: "",
                smallIcon
            )
        }
    }
}