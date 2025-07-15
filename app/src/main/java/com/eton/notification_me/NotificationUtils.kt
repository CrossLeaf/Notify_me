package com.eton.notification_me

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import java.util.*
import com.eton.notification_me.util.LogManager


open class NotificationUtils {
    companion object {
        const val CHANNEL_ID_BASE = "work"
        private var lastSoundUri: String? = null
        private var currentChannelId: String = CHANNEL_ID_BASE
//        var condition = arrayListOf<String>()
    }

    private var notificationManager: NotificationManager? = null

    fun sendNotification(
        context: Context,
        packageName: String,
        messageBody: String,
        smallIcon: Drawable?
    ) {
        val logManager = LogManager.getInstance()
        
        Log.d("NotificationUtils", "=== 開始處理通知 ===")
        Log.d("NotificationUtils", "Package: $packageName")
        Log.d("NotificationUtils", "Message: $messageBody")
        
        logManager.addLog("開始處理通知 - 包名: $packageName", "INFO")
        logManager.addNotificationLog("訊息內容: $messageBody", "DEBUG")
        
        val spUtil = SpUtil(context)
        
        // 獲取自訂音效設定
        val customSoundUriString = spUtil.getNotificationSoundUri()
        val customSoundUri = if (customSoundUriString != null) {
            Uri.parse(customSoundUriString)
        } else {
            // 使用專案內的預設音效
            Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.warning}")
        }
        
        Log.d("NotificationUtils", "🎵 使用音效: ${spUtil.getNotificationSoundName()}")
        Log.d("NotificationUtils", "🎵 音效URI: $customSoundUri")
        
        // 檢查音效是否改變，如果改變則創建新的通知頻道
        if (lastSoundUri != customSoundUri.toString()) {
            Log.d("NotificationUtils", "🔄 音效已改變，創建新的通知頻道")
            currentChannelId = "${CHANNEL_ID_BASE}_${System.currentTimeMillis()}"
            createNotificationChannelWithSound(context, customSoundUri)
            lastSoundUri = customSoundUri.toString()
        }
        
        // 僅送出被選中的 app
        if (!spUtil.getPackageName().contains(packageName)) {
            Log.d("NotificationUtils", "應用程式未被選中，跳過處理")
            logManager.addLog("應用程式未被選中，跳過處理: $packageName", "INFO")
            return
        }
        
        // 過濾掉 Telegram 的摘要通知
        if (packageName == "org.telegram.messenger") {
            if (isTelegramSummaryNotification(messageBody)) {
                Log.d("NotificationUtils", "Telegram 摘要通知，跳過處理")
                logManager.addNotificationLog("Telegram 摘要通知，跳過處理: $messageBody", "INFO")
                return
            }
        }
        spUtil.getCondition()?.let { conditionSet ->
            conditionSet.any {
                messageBody.contains(it, true)
            }.also { isMatch ->
                Log.d("TAG", "sendNotification: match? $isMatch")
                
                // 如果條件不符合，直接返回
                if (!isMatch) {
                    Log.d("TAG", "sendNotification: 條件不符合，跳過處理")
                    logManager.addNotificationLog("條件不符合，跳過處理: $messageBody", "INFO")
                    return
                }
                
                // 檢查是否為重複通知 (防止短時間內重複觸發)
                val currentTime = System.currentTimeMillis()
                val lastNotificationTime = spUtil.getLastNotificationTime()
                val timeDiff = currentTime - lastNotificationTime
                
                // 如果距離上次通知時間少於2秒，且訊息內容相同，則跳過
                if (timeDiff < 2000 && messageBody == spUtil.getMessageBody()) {
                    Log.d("NotificationUtils", "⚠️ 重複通知，跳過處理 (時間差: ${timeDiff}ms)")
                    logManager.addNotificationLog("重複通知，跳過處理: $messageBody", "INFO")
                    return
                }
                
                // 記錄訊息
                Log.d("NotificationUtils", "處理訊息: $messageBody")
                logManager.addNotificationLog("關鍵字匹配，準備發送通知: $messageBody", "INFO")
                
                // 更新最後處理的訊息內容
                spUtil.editMessageBody(messageBody)
                
                // 確保每次通知都有唯一的識別
                val uniqueId = currentTime.toInt()
                val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(currentTime))
                
                val builder = NotificationCompat.Builder(context, currentChannelId)
                    .setLargeIcon(
                        ContextCompat.getDrawable(context, R.drawable.spy_notify)?.toBitmap()
                    )
                    .setContentTitle("你被 tag 了 - ${getLabel(context, packageName)}")
                    .setContentText("$messageBody [$timestamp]")
                    .setColor(ContextCompat.getColor(context, R.color.black))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_LIGHTS)
                    .setSound(customSoundUri)
                    .setVibrate(longArrayOf(0, 300, 200, 300))
                    .setWhen(currentTime)
                    .setShowWhen(true)
                    .setOnlyAlertOnce(false)
                    .setChannelId(currentChannelId)
                    .setGroup("chat_messages")
                    .setGroupSummary(false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.setSmallIcon(android.R.drawable.stat_notify_error)
                        .color = ContextCompat.getColor(context, android.R.color.holo_red_light)
                } else {
                    builder.setSmallIcon(android.R.drawable.stat_notify_error)
                }
                
                // 發送通知
                with(NotificationManagerCompat.from(context)) {
                    if (areNotificationsEnabled()) {
                        @Suppress("MissingPermission")
                        notify(uniqueId, builder.build())
                    } else {
                        Log.w("NotificationUtils", "通知權限未授予，無法發送通知")
                        logManager.addLog("通知權限未授予，無法發送通知", "WARNING")
                        return
                    }
                    
                    // 通知音效已由系統通知處理，不需要手動播放
                    Log.d("NotificationUtils", "🔊 通知音效由系統處理: ${spUtil.getNotificationSoundName()}")
                    
                    // 更新最後通知時間
                    spUtil.setLastNotificationTime(currentTime)
                    
                    Log.d("NotificationUtils", "🔔 通知已發送，ID: $uniqueId, 時間: $timestamp")
                    logManager.addLog("✅ 通知已發送 - ID: $uniqueId, 時間: $timestamp", "INFO")
                }
            }
        }
    }

    fun createNotificationChannelWithSound(context: Context, soundUri: Uri) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(currentChannelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                enableLights(true)
                lightColor = android.graphics.Color.RED
                setBypassDnd(true)
                setShowBadge(true)
            }
            
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()
            channel.setSound(soundUri, audioAttributes)
            
            Log.d("NotificationUtils", "🎵 創建通知頻道: $currentChannelId，音效: $soundUri")

            // Register the channel with the system
            notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID_BASE, name, importance).apply {
                description = descriptionText
                // 確保每次都有聲音
                setSound(null, null) // 先清除預設聲音
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                enableLights(true)
                lightColor = android.graphics.Color.RED
                setBypassDnd(true)
                setShowBadge(true)
            }
            
            // 獲取自訂音效設定
            val spUtil = SpUtil(context)
            val customSoundUriString = spUtil.getNotificationSoundUri()
            val soundUri = if (customSoundUriString != null) {
                Uri.parse(customSoundUriString)
            } else {
                // 使用專案內的預設音效
                Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.warning}")
            }
            
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()
            channel.setSound(soundUri, audioAttributes)

            // Register the channel with the system
            notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)

        }
    }

    /**
     * 判斷是否為 Telegram 摘要通知
     * @param messageBody 訊息內容
     * @return 是否為摘要通知
     */
    private fun isTelegramSummaryNotification(messageBody: String): Boolean {
        // 常見的 Telegram 摘要通知格式
        val summaryPatterns = listOf(
            ".*・\\d+\\s*new\\s*messages?\\s*from\\s*\\d+\\s*chats?.*", // Max・15 new messages from 4 chats
            ".*\\d+\\s*new\\s*messages?\\s*from\\s*.*", // 其他格式的摘要
            ".*unread\\s*messages?.*", // 未讀訊息摘要
            ".*messages?\\s*from\\s*\\d+\\s*chats?.*" // 來自多個聊天室的訊息
        )
        
        return summaryPatterns.any { pattern ->
            messageBody.matches(pattern.toRegex(RegexOption.IGNORE_CASE))
        }
    }

    /**
     * 取得應用程式名稱
     * @param context Context
     * @param packageName 包名
     * @return 應用程式名稱
     */
    private fun getLabel(context: Context, packageName: String) : String{
        val packageManager = context.packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val applicationLabel = packageManager.getApplicationLabel(applicationInfo).toString()

            applicationLabel
        } catch (e: PackageManager.NameNotFoundException) {
            // 找不到指定套件名稱的應用程式
            e.printStackTrace()
            packageName
        }
    }
}