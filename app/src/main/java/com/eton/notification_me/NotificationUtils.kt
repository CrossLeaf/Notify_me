package com.eton.notification_me

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
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


open class NotificationUtils {
    companion object {
        const val CHANNEL_ID = "work"
//        var condition = arrayListOf<String>()
    }

    private var notificationManager: NotificationManager? = null

    fun sendNotification(
        context: Context,
        packageName: String,
        messageBody: String,
        smallIcon: Drawable?
    ) {
        Log.d("TAG", "sendNotification: packageName? $packageName")
        val spUtil = SpUtil(context)
        // 僅送出被選中的 app
        if ( !spUtil.getPackageName().contains(packageName)) {
            return
        }
        spUtil.getCondition()?.let { conditionSet ->
            conditionSet.any {
                messageBody.contains(it, true)
            }.run {
                Log.d("TAG", "sendNotification: match? $this")
                if (!this) {
                    // 條件不對不執行
                    return
                }
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setLargeIcon(
                        ContextCompat.getDrawable(context, R.drawable.spy_notify)?.toBitmap()
                    )
                    .setContentTitle("你被 tag 了")
                    .setContentText(messageBody)
                    .setColor(ContextCompat.getColor(context, R.color.black))
                    .setAutoCancel(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.setSmallIcon(android.R.drawable.stat_notify_error)
                        .color = ContextCompat.getColor(context, android.R.color.holo_red_light)
                } else {
                    builder.setSmallIcon(android.R.drawable.stat_notify_error)
                }
                with(NotificationManagerCompat.from(context)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(Calendar.getInstance().timeInMillis.toInt(), builder.build())
                }
            }
        }
    }

    fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val uri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(context.packageName)
                .appendPath("raw")
                .appendPath("warning")
                .build()
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            channel.setSound(uri, audioAttributes)

            // Register the channel with the system
            notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)

        }
    }

}