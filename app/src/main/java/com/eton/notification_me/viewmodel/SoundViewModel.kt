package com.eton.notification_me.viewmodel

import android.content.Context
import android.database.Cursor
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eton.notification_me.NotificationSoundActivity
import com.eton.notification_me.R
import com.eton.notification_me.SpUtil
import com.eton.notification_me.SoundItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File

class SoundViewModel : ViewModel() {
    private val _soundList = mutableStateListOf<SoundItem>()
    val soundList: List<SoundItem> = _soundList
    
    var isLoading by mutableStateOf(false)
        private set
    
    var loadingProgress by mutableStateOf(0f)
        private set
    
    var loadingText by mutableStateOf("載入中...")
        private set
    
    fun loadSounds(context: Context) {
        if (_soundList.isNotEmpty()) return // Already loaded
        
        loadSoundsInternal(context)
    }
    
    fun reloadSounds(context: Context) {
        _soundList.clear()
        loadSoundsInternal(context)
    }
    
    private fun loadSoundsInternal(context: Context) {
        viewModelScope.launch {
            isLoading = true
            loadingProgress = 0f
            loadingText = "開始載入..."
            
            try {
                val spUtil = SpUtil(context)
                val currentSoundUri = spUtil.getNotificationSoundUri()
                
                // Add default sound option (using project's warning.mp3)
                loadingText = "載入預設音效..."
                loadingProgress = 0.1f
                
                val defaultSoundUri = Uri.parse("${android.content.ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.warning}")
                val isDefaultSelected = currentSoundUri == null || currentSoundUri == defaultSoundUri.toString()
                val defaultSound = SoundItem(
                    uri = defaultSoundUri,
                    name = "預設通知音效 (Warning)",
                    isSelected = isDefaultSelected,
                    category = "應用內建"
                )
                _soundList.add(defaultSound)
                
                // Load system notification sounds
                loadingText = "載入系統通知音效..."
                loadingProgress = 0.2f
                try {
                    val notificationSounds = withTimeout(10000L) { // 10 seconds timeout
                        withContext(Dispatchers.IO) {
                            loadSystemNotificationSounds(context, currentSoundUri)
                        }
                    }
                    _soundList.addAll(notificationSounds)
                    Log.d("SoundViewModel", "Loaded ${notificationSounds.size} notification sounds")
                } catch (e: Exception) {
                    Log.w("SoundViewModel", "Failed to load notification sounds: ${e.message}")
                }
                
                // Load system ringtones
                loadingText = "載入系統鈴聲..."
                loadingProgress = 0.4f
                try {
                    val ringtones = withTimeout(10000L) { // 10 seconds timeout
                        withContext(Dispatchers.IO) {
                            loadSystemRingtones(context, currentSoundUri)
                        }
                    }
                    _soundList.addAll(ringtones)
                    Log.d("SoundViewModel", "Loaded ${ringtones.size} ringtones")
                } catch (e: Exception) {
                    Log.w("SoundViewModel", "Failed to load ringtones: ${e.message}")
                }
                
                // Load system alarm sounds
                loadingText = "載入系統鬧鐘音效..."
                loadingProgress = 0.6f
                try {
                    val alarmSounds = withTimeout(10000L) { // 10 seconds timeout
                        withContext(Dispatchers.IO) {
                            loadSystemAlarmSounds(context, currentSoundUri)
                        }
                    }
                    _soundList.addAll(alarmSounds)
                    Log.d("SoundViewModel", "Loaded ${alarmSounds.size} alarm sounds")
                } catch (e: Exception) {
                    Log.w("SoundViewModel", "Failed to load alarm sounds: ${e.message}")
                }
                
                // Load media store audio files
                loadingText = "載入媒體庫音檔..."
                loadingProgress = 0.8f
                try {
                    val mediaFiles = withTimeout(15000L) { // 15 seconds timeout for media files
                        withContext(Dispatchers.IO) {
                            loadMediaStoreAudioFiles(context, currentSoundUri)
                        }
                    }
                    _soundList.addAll(mediaFiles)
                    Log.d("SoundViewModel", "Loaded ${mediaFiles.size} media files")
                } catch (e: Exception) {
                    Log.w("SoundViewModel", "Failed to load media files: ${e.message}")
                }
                
                loadingText = "完成載入"
                loadingProgress = 1.0f
                
                Log.d("SoundViewModel", "Total sounds loaded: ${_soundList.size}")
                
            } catch (e: Exception) {
                Log.e("SoundViewModel", "Error loading sounds: ${e.message}")
                loadingText = "載入失敗: ${e.message}"
            } finally {
                isLoading = false
                loadingProgress = 0f
            }
        }
    }
    
    private fun loadSystemNotificationSounds(context: Context, currentSoundUri: String?): List<SoundItem> {
        val sounds = mutableListOf<SoundItem>()
        try {
            val ringtoneManager = RingtoneManager(context)
            ringtoneManager.setType(RingtoneManager.TYPE_NOTIFICATION)
            val cursor = ringtoneManager.cursor
            
            if (cursor.moveToFirst()) {
                do {
                    try {
                        val notificationUri = ringtoneManager.getRingtoneUri(cursor.position)
                        val name = try {
                            ringtoneManager.getRingtone(cursor.position)?.getTitle(context) 
                                ?: "通知音效 ${cursor.position + 1}"
                        } catch (e: Exception) {
                            "通知音效 ${cursor.position + 1}"
                        }
                        
                        sounds.add(SoundItem(
                            uri = notificationUri,
                            name = name,
                            isSelected = notificationUri.toString() == currentSoundUri,
                            category = "系統通知"
                        ))
                    } catch (e: Exception) {
                        Log.w("SoundViewModel", "Skip notification sound at position ${cursor.position}: ${e.message}")
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("SoundViewModel", "Error loading notification sounds: ${e.message}")
        }
        return sounds
    }
    
    private fun loadSystemRingtones(context: Context, currentSoundUri: String?): List<SoundItem> {
        val sounds = mutableListOf<SoundItem>()
        try {
            val ringtoneManager = RingtoneManager(context)
            ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE)
            val cursor = ringtoneManager.cursor
            
            if (cursor.moveToFirst()) {
                var count = 0
                do {
                    if (count >= 30) break // Limit ringtones to avoid too many
                    
                    try {
                        val ringtoneUri = ringtoneManager.getRingtoneUri(cursor.position)
                        val name = try {
                            ringtoneManager.getRingtone(cursor.position)?.getTitle(context) 
                                ?: "鈴聲 ${cursor.position + 1}"
                        } catch (e: Exception) {
                            "鈴聲 ${cursor.position + 1}"
                        }
                        
                        sounds.add(SoundItem(
                            uri = ringtoneUri,
                            name = name,
                            isSelected = ringtoneUri.toString() == currentSoundUri,
                            category = "系統鈴聲"
                        ))
                        count++
                    } catch (e: Exception) {
                        Log.w("SoundViewModel", "Skip ringtone at position ${cursor.position}: ${e.message}")
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("SoundViewModel", "Error loading ringtones: ${e.message}")
        }
        return sounds
    }
    
    private fun loadSystemAlarmSounds(context: Context, currentSoundUri: String?): List<SoundItem> {
        val sounds = mutableListOf<SoundItem>()
        try {
            val ringtoneManager = RingtoneManager(context)
            ringtoneManager.setType(RingtoneManager.TYPE_ALARM)
            val cursor = ringtoneManager.cursor
            
            if (cursor.moveToFirst()) {
                var count = 0
                do {
                    if (count >= 20) break // Limit alarms
                    
                    try {
                        val alarmUri = ringtoneManager.getRingtoneUri(cursor.position)
                        val name = try {
                            ringtoneManager.getRingtone(cursor.position)?.getTitle(context) 
                                ?: "鬧鐘音效 ${cursor.position + 1}"
                        } catch (e: Exception) {
                            "鬧鐘音效 ${cursor.position + 1}"
                        }
                        
                        sounds.add(SoundItem(
                            uri = alarmUri,
                            name = name,
                            isSelected = alarmUri.toString() == currentSoundUri,
                            category = "系統鬧鐘"
                        ))
                        count++
                    } catch (e: Exception) {
                        Log.w("SoundViewModel", "Skip alarm sound at position ${cursor.position}: ${e.message}")
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("SoundViewModel", "Error loading alarm sounds: ${e.message}")
        }
        return sounds
    }
    
    private fun loadMediaStoreAudioFiles(context: Context, currentSoundUri: String?): List<SoundItem> {
        val sounds = mutableListOf<SoundItem>()
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.MIME_TYPE
            )
            
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1 OR ${MediaStore.Audio.Media.IS_NOTIFICATION} = 1 OR ${MediaStore.Audio.Media.IS_ALARM} = 1 OR ${MediaStore.Audio.Media.IS_RINGTONE} = 1"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
            
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )
            
            cursor?.use { c ->
                val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val mimeTypeColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                
                var count = 0
                while (c.moveToNext() && count < 100) { // Limit to 100 files
                    try {
                        val id = c.getLong(idColumn)
                        val title = c.getString(titleColumn) ?: "未知音檔"
                        val artist = c.getString(artistColumn) ?: "未知藝術家"
                        val duration = c.getLong(durationColumn)
                        val data = c.getString(dataColumn)
                        val mimeType = c.getString(mimeTypeColumn) ?: ""
                        
                        // Skip very long files (> 2 minutes) as they're likely music
                        if (duration > 120000) continue
                        
                        val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
                        val file = File(data)
                        val displayName = if (title != "未知音檔") {
                            "$title - $artist"
                        } else {
                            file.nameWithoutExtension
                        }
                        
                        val category = when {
                            mimeType.contains("audio") -> "媒體檔案"
                            file.extension.lowercase() in listOf("mp3", "wav", "ogg", "m4a", "aac") -> "音檔"
                            else -> "其他"
                        }
                        
                        sounds.add(SoundItem(
                            uri = uri,
                            name = displayName,
                            isSelected = uri.toString() == currentSoundUri,
                            category = category
                        ))
                        count++
                    } catch (e: Exception) {
                        Log.w("SoundViewModel", "Skip media file: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SoundViewModel", "Error loading media store files: ${e.message}")
        }
        return sounds
    }
    
    fun selectSound(soundItem: SoundItem) {
        viewModelScope.launch {
            Log.d("SoundViewModel", "🎵 選擇音效: ${soundItem.name}")
            
            // Update selection state by replacing the list
            val updatedList = _soundList.map { item ->
                val isSelected = item.uri == soundItem.uri
                Log.d("SoundViewModel", "音效 ${item.name}: $isSelected")
                item.copy(isSelected = isSelected)
            }
            _soundList.clear()
            _soundList.addAll(updatedList)
            
            Log.d("SoundViewModel", "✅ 音效選擇狀態已更新")
        }
    }
    
    fun saveSoundSelection(context: Context, soundItem: SoundItem) {
        viewModelScope.launch {
            val spUtil = SpUtil(context)
            spUtil.setNotificationSoundUri(soundItem.uri?.toString())
            spUtil.setNotificationSoundName(soundItem.name)
            
            // Reset NotificationUtils sound cache
            try {
                val notificationUtilsClass = Class.forName("com.eton.notification_me.NotificationUtils")
                val lastSoundUriField = notificationUtilsClass.getDeclaredField("lastSoundUri")
                lastSoundUriField.isAccessible = true
                lastSoundUriField.set(null, null)
                
                Log.d("SoundViewModel", "🔄 已重置音效快取")
            } catch (e: Exception) {
                Log.w("SoundViewModel", "無法重置音效快取: ${e.message}")
            }
        }
    }
}