package com.eton.notification_me

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.eton.notification_me.util.LogManager


class NotificationMonitorService : NotificationListenerService() {
    
    companion object {
        private const val TAG = "NotificationMonitorV3"
        private const val MY_PACKAGE_NAME = "com.eton.notification_me"
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "🔄 Service connected - Version 3.0 - Time: ${System.currentTimeMillis()}")
        LogManager.getInstance().addLog("📡 Service started v3.0")
    }

    
    /**
     * Get application name
     * @param packageName package name
     * @return application name
     */
    private fun getAppName(packageName: String): String {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            // Cannot find application with specified package name, return package name
            packageName
        }
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.apply {
            val logManager = LogManager.getInstance()
            val extras = notification.extras
            val packageName = packageName // Application identifier
            val appName = getAppName(packageName) // Get application name

            val title = extras.getString(Notification.EXTRA_TITLE) // Get notification title
            val text = extras.getString(Notification.EXTRA_TEXT) // Get notification text

            // Add detailed logs
            Log.d(TAG, "=== Received notification [VERSION 3.0] ===")
            Log.d(TAG, "App: $appName")
            Log.d(TAG, "Title: $title")
            Log.d(TAG, "Text: $text")
            Log.d(TAG, "Notification ID: ${sbn.id}")
            Log.d(TAG, "Post Time: ${sbn.postTime}")
            
            logManager.addLog("📱 $appName: $title")
            logManager.addNotificationLog("💬 $text")

            // Skip our own notifications to avoid infinite loop
            Log.d(TAG, "🔍 Checking application: '$appName'")
            if (packageName == MY_PACKAGE_NAME) {
                Log.d(TAG, "❌ Skip own notification to avoid infinite loop")
                logManager.addLog("Skip own notification to avoid infinite loop", "INFO")
                return
            }

            // Skip empty messages
            if (text.isNullOrBlank()) {
                Log.d(TAG, "❌ Message content is empty, skipping processing")
                logManager.addLog("⚠️ $appName message is empty")
                return
            }

            Log.d(TAG, "✅ Starting notification processing...")
            logManager.addLog("🚀 Starting processing $appName")

            // Get notification small icon
            val smallIcon = sbn.notification.smallIcon?.loadDrawable(this@NotificationMonitorService)
            
            // Get notification large icon
            val largeIcon =
                notification.getLargeIcon()?.loadDrawable(this@NotificationMonitorService)
                    ?.toBitmap()

            NotificationUtils().sendNotification(
                this@NotificationMonitorService,
                packageName,
                text,
                smallIcon
            )
        }
    }
}