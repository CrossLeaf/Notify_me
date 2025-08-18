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
        val appName = getLabel(context, packageName)
        
        Log.d("NotificationUtils", "=== Processing notification ===")
        Log.d("NotificationUtils", "App: $appName")
        Log.d("NotificationUtils", "Message: $messageBody")
        
        val spUtil = SpUtil(context)
        
        // Get custom sound settings
        val customSoundUriString = spUtil.getNotificationSoundUri()
        val customSoundUri = if (customSoundUriString != null) {
            Uri.parse(customSoundUriString)
        } else {
            // Use default sound from project
            Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.warning}")
        }
        
        Log.d("NotificationUtils", "🎵 Using sound: ${spUtil.getNotificationSoundName()}")
        Log.d("NotificationUtils", "🎵 Sound URI: $customSoundUri")
        
        // Check if sound changed, create new notification channel if changed
        if (lastSoundUri != customSoundUri.toString()) {
            Log.d("NotificationUtils", "🔄 Sound changed, creating new notification channel")
            currentChannelId = "${CHANNEL_ID_BASE}_${System.currentTimeMillis()}"
            createNotificationChannelWithSound(context, customSoundUri)
            lastSoundUri = customSoundUri.toString()
        }
        
        // Only send notifications for selected apps
        if (!spUtil.getPackageName().contains(packageName)) {
            Log.d("NotificationUtils", "App not selected, skipping processing")
            logManager.addLog("🚫 $appName not monitored")
            return
        }
        
        // Filter out Telegram summary notifications
        if (packageName == "org.telegram.messenger") {
            if (isTelegramSummaryNotification(messageBody)) {
                Log.d("NotificationUtils", "Telegram summary notification, skipping processing")
                logManager.addNotificationLog("Telegram summary notification, skipping processing: $messageBody", "INFO")
                return
            }
        }
        spUtil.getCondition()?.let { conditionSet ->
            conditionSet.any {
                messageBody.contains(it, true)
            }.also { isMatch ->
                Log.d("TAG", "sendNotification: match? $isMatch")
                
                // If conditions don't match, return directly
                if (!isMatch) {
                    Log.d("TAG", "sendNotification: Conditions not met, skipping processing")
                    logManager.addNotificationLog("❌ Conditions not met")
                    return
                }
                
                // Check for duplicate notifications (prevent repeated triggers in short time)
                val currentTime = System.currentTimeMillis()
                val lastNotificationTime = spUtil.getLastNotificationTime()
                val timeDiff = currentTime - lastNotificationTime
                
                // If less than 2 seconds since last notification and message content is the same, skip
                if (timeDiff < 2000 && messageBody == spUtil.getMessageBody()) {
                    Log.d("NotificationUtils", "⚠️ Duplicate notification, skipping processing (time diff: ${timeDiff}ms)")
                    logManager.addNotificationLog("Duplicate notification, skipping processing: $messageBody", "INFO")
                    return
                }
                
                // Record message
                Log.d("NotificationUtils", "Processing message: $messageBody")
                logManager.addNotificationLog("Keyword matched, preparing to send notification: $messageBody (from: $appName)", "INFO")
                
                // Update last processed message content
                spUtil.editMessageBody(messageBody)
                
                // Ensure each notification has a unique identifier
                val uniqueId = currentTime.toInt()
                val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(currentTime))
                
                val builder = NotificationCompat.Builder(context, currentChannelId)
                    .setLargeIcon(
                        ContextCompat.getDrawable(context, R.drawable.ic_notification)?.toBitmap()
                    )
                    .setContentTitle("You've been tagged - ${getLabel(context, packageName)}")
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
                    builder.setSmallIcon(R.drawable.ic_notification_small)
                        .color = ContextCompat.getColor(context, android.R.color.holo_red_light)
                } else {
                    builder.setSmallIcon(R.drawable.ic_notification_small)
                }
                
                // Send notification
                with(NotificationManagerCompat.from(context)) {
                    if (areNotificationsEnabled()) {
                        @Suppress("MissingPermission")
                        notify(uniqueId, builder.build())
                    } else {
                        Log.w("NotificationUtils", "Notification permission not granted, cannot send notification")
                        logManager.addLog("Notification permission not granted, cannot send notification", "WARNING")
                        return
                    }
                    
                    // Notification sound handled by system notification, no need to play manually
                    Log.d("NotificationUtils", "🔊 Notification sound handled by system: ${spUtil.getNotificationSoundName()}")
                    
                    // Update last notification time
                    spUtil.setLastNotificationTime(currentTime)
                    
                    Log.d("NotificationUtils", "🔔 Notification sent, ID: $uniqueId, Time: $timestamp")
                    logManager.addLog("✅ Notification sent ($appName)")
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
            
            Log.d("NotificationUtils", "🎵 Creating notification channel: $currentChannelId, sound: $soundUri")

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
                // Ensure sound is always played
                setSound(null, null) // Clear default sound first
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                enableLights(true)
                lightColor = android.graphics.Color.RED
                setBypassDnd(true)
                setShowBadge(true)
            }
            
            // Get custom sound settings
            val spUtil = SpUtil(context)
            val customSoundUriString = spUtil.getNotificationSoundUri()
            val soundUri = if (customSoundUriString != null) {
                Uri.parse(customSoundUriString)
            } else {
                // Use default sound from project
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
     * Check if it's a Telegram summary notification
     * @param messageBody message content
     * @return whether it's a summary notification
     */
    private fun isTelegramSummaryNotification(messageBody: String): Boolean {
        // Common Telegram summary notification formats
        val summaryPatterns = listOf(
            ".*・\\d+\\s*new\\s*messages?\\s*from\\s*\\d+\\s*chats?.*", // Max・15 new messages from 4 chats
            ".*\\d+\\s*new\\s*messages?\\s*from\\s*.*", // Other summary formats
            ".*unread\\s*messages?.*", // Unread message summary
            ".*messages?\\s*from\\s*\\d+\\s*chats?.*" // Messages from multiple chats
        )
        
        return summaryPatterns.any { pattern ->
            messageBody.matches(pattern.toRegex(RegexOption.IGNORE_CASE))
        }
    }

    /**
     * Get application name
     * @param context Context
     * @param packageName package name
     * @return application name
     */
    private fun getLabel(context: Context, packageName: String) : String{
        val packageManager = context.packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val applicationLabel = packageManager.getApplicationLabel(applicationInfo).toString()

            applicationLabel
        } catch (e: PackageManager.NameNotFoundException) {
            // Cannot find application with specified package name
            e.printStackTrace()
            packageName
        }
    }
}