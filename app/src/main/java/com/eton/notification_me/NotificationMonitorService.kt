package com.eton.notification_me

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.graphics.drawable.toBitmap


class NotificationMonitorService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val extras = sbn!!.notification.extras
        val packageName = sbn!!.packageName // 取得應用程式包名

        val title = extras.getString(Notification.EXTRA_TITLE) // 取得通知欄標題

        val text = extras.getString(Notification.EXTRA_TEXT) // 取得通知欄文字

        // 取得通知欄的小圖示
        var smallIcon = sbn.notification.smallIcon.loadDrawable(this)
        // 取得通知欄的大圖示
        val largeIcon = sbn.notification.getLargeIcon().loadDrawable(this).toBitmap()
        if (packageName != applicationContext.packageName) {
            MainActivity.show(packageName, title, text, smallIcon, largeIcon)
        }
    }
}