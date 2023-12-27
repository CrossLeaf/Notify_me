package com.eton.notification_me

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.MenuItem
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class NotificationVolumeActivity : AppCompatActivity() {
    // 定義 AudioManager 來控制音量
    private lateinit var audioManager: AudioManager

    // 定義 SeekBar 來顯示和控制音量
    private lateinit var seekBar: SeekBar

    // 定義一個變量來存儲當前的音量
    private var currentVolume: Int = 0

    // 定義一個 ContentObserver 來監聽系統設置的變化
    private val volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            // 當系統設置變化時，檢查音量是否有變化
            checkVolumeChange()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_volume)
        setTitle("Notification Volume")

        // 獲取 AudioManager 服務
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // 獲取當前的通知音量
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)

        // 獲取 SeekBar 的實例並設定其最大值和當前進度
        seekBar = findViewById(R.id.seekBar)
        seekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
        seekBar.progress = currentVolume

        // 為 SeekBar 設定一個監聽器，當進度變化時，更新通知音量
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, progress, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 註冊 ContentObserver 來監聽系統設置的變化
        contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI, true, volumeObserver
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // 當點擊返回鍵時，結束當前的 Activity
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 在 Activity 被銷毀時，取消註冊 ContentObserver
        contentResolver.unregisterContentObserver(volumeObserver)
    }

    /**
     * Check if the volume has changed and update the seek bar if it has.
     */
    private fun checkVolumeChange() {
        // 獲取新的通知音量
        val newVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        // 如果新的音量與當前的音量不同，則更新 SeekBar 的進度
        if (newVolume != currentVolume) {
            currentVolume = newVolume
            seekBar.progress = currentVolume
        }
    }
}