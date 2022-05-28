package com.eton.notification_me

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Drawable
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
        var condition = arrayListOf<String>()
    }

    fun sendNotification(context: Context, packageName: String, messageBody: String, smallIcon: Drawable?) {
        if (packageName == context.applicationContext.packageName) {
            return
        }
        Log.d("TAG", "sendNotification: packageName? $packageName")

        condition.any {
            messageBody.contains(it)
        }.run {
            Log.d("TAG", "sendNotification: match? $this")
            if (!this) {
                // 條件不對不執行
                return
            }
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.spy_notification)
                .setLargeIcon(smallIcon?.toBitmap())
                .setContentTitle("你被 tag 了")
                .setContentText(messageBody)
                .setColor(ContextCompat.getColor(context, R.color.black))
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                notify(Calendar.getInstance().timeInMillis.toInt(), builder.build())
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
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

        }
    }

}