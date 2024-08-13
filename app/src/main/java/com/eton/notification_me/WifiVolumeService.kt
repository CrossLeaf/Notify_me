package com.eton.notification_me

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder

class WifiVolumeService : Service() {
    private lateinit var audioManager: AudioManager
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "WifiVolumeService",
                "Wifi Volume Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            val notification = Notification.Builder(this, "WifiVolumeService").build()
            startForeground(1, notification)
        }

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Wi-Fi is connected, set the volume 5, 0~7
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 5, 0)
            }

            override fun onLost(network: Network) {
                // Wi-Fi is disconnected, max the volume
                audioManager.setStreamVolume(
                    AudioManager.STREAM_NOTIFICATION,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
                    0
                )
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
//        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}