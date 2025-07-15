package com.eton.notification_me.viewmodel

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.eton.notification_me.WifiVolumeService

class VolumeViewModel : ViewModel() {
    private var audioManager: AudioManager? = null
    private var contentResolver: android.content.ContentResolver? = null
    
    var currentVolume by mutableStateOf(0)
        private set
    
    var maxVolume by mutableStateOf(15)
        private set
    
    var isAutoAdjustEnabled by mutableStateOf(false)
        private set
    
    // ContentObserver to monitor system volume changes
    private val volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            checkVolumeChange()
        }
    }
    
    fun initialize(context: Context) {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        contentResolver = context.contentResolver
        
        audioManager?.let { am ->
            currentVolume = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
        }
        
        isAutoAdjustEnabled = isServiceRunning(context, WifiVolumeService::class.java)
        
        // Register content observer to monitor system volume changes
        contentResolver?.registerContentObserver(
            Settings.System.CONTENT_URI, true, volumeObserver
        )
    }
    
    fun cleanup() {
        contentResolver?.unregisterContentObserver(volumeObserver)
    }
    
    fun setVolume(volume: Int) {
        audioManager?.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0)
        currentVolume = volume
    }
    
    fun toggleAutoAdjust(context: Context, enable: Boolean) {
        if (enable) {
            val intent = Intent(context, WifiVolumeService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            context.stopService(Intent(context, WifiVolumeService::class.java))
        }
        isAutoAdjustEnabled = enable
    }
    
    private fun checkVolumeChange() {
        audioManager?.let { am ->
            val newVolume = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            if (newVolume != currentVolume) {
                currentVolume = newVolume
            }
        }
    }
    
    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}