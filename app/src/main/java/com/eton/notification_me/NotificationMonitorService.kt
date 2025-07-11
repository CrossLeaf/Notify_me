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
        Log.d(TAG, "🔄 服務已連接 - 版本 3.0 - 時間: ${System.currentTimeMillis()}")
        LogManager.getInstance().addLog("通知監聽服務已連接 - 版本 3.0", "INFO")
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.apply {
            val logManager = LogManager.getInstance()
            val extras = notification.extras
            val packageName = packageName // 取得應用程式包名

            val title = extras.getString(Notification.EXTRA_TITLE) // 取得通知欄標題
            val text = extras.getString(Notification.EXTRA_TEXT) // 取得通知欄文字

            // 添加詳細日誌
            Log.d(TAG, "=== 收到通知 [VERSION 3.0] ===")
            Log.d(TAG, "Package: $packageName")
            Log.d(TAG, "Title: $title")
            Log.d(TAG, "Text: $text")
            Log.d(TAG, "Notification ID: ${sbn.id}")
            Log.d(TAG, "Post Time: ${sbn.postTime}")
            
            logManager.addLog("收到通知 - 包名: $packageName, 標題: $title", "DEBUG")
            logManager.addNotificationLog("通知內容: $text", "DEBUG")

            // 跳過我們自己的通知，避免無限循環
            Log.d(TAG, "🔍 檢查包名: '$packageName' vs '$MY_PACKAGE_NAME'")
            if (packageName == MY_PACKAGE_NAME) {
                Log.d(TAG, "❌ 跳過自己的通知，避免無限循環")
                logManager.addLog("跳過自己的通知，避免無限循環", "INFO")
                return
            }

            // 跳過空訊息
            if (text.isNullOrBlank()) {
                Log.d(TAG, "❌ 訊息內容為空，跳過處理")
                logManager.addLog("訊息內容為空，跳過處理 - 包名: $packageName", "INFO")
                return
            }

            Log.d(TAG, "✅ 開始處理通知...")
            logManager.addLog("開始處理通知: $packageName", "INFO")

            // 取得通知欄的小圖示
            val smallIcon = notification.smallIcon?.loadDrawable(this@NotificationMonitorService)
            // 取得通知欄的大圖示
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