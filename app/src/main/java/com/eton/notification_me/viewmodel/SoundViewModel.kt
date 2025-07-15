package com.eton.notification_me.viewmodel

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
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

class SoundViewModel : ViewModel() {
    private val _soundList = mutableStateListOf<SoundItem>()
    val soundList: List<SoundItem> = _soundList
    
    var isLoading by mutableStateOf(false)
        private set
    
    fun loadSounds(context: Context) {
        viewModelScope.launch {
            isLoading = true
            
            try {
                val sounds = withContext(Dispatchers.IO) {
                    val spUtil = SpUtil(context)
                    val soundItems = mutableListOf<SoundItem>()
                    val ringtoneManager = RingtoneManager(context)
                    ringtoneManager.setType(RingtoneManager.TYPE_NOTIFICATION)
                    
                    val cursor = ringtoneManager.cursor
                    val currentSoundUri = spUtil.getNotificationSoundUri()
                    
                    // Add default sound option (using project's warning.mp3)
                    val defaultSoundUri = Uri.parse("${android.content.ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.warning}")
                    val isDefaultSelected = currentSoundUri == null || currentSoundUri == defaultSoundUri.toString()
                    soundItems.add(SoundItem(
                        uri = defaultSoundUri,
                        name = "預設通知音效 (Warning)",
                        isSelected = isDefaultSelected
                    ))
                    
                    // Add system sounds - limit to 20 to avoid ANR
                    if (cursor.moveToFirst()) {
                        var count = 0
                        do {
                            if (count >= 20) break
                            
                            try {
                                val notificationUri = ringtoneManager.getRingtoneUri(cursor.position)
                                val name = try {
                                    ringtoneManager.getRingtone(cursor.position)?.getTitle(context) 
                                        ?: "音效 ${cursor.position + 1}"
                                } catch (e: Exception) {
                                    "音效 ${cursor.position + 1}"
                                }
                                
                                soundItems.add(SoundItem(
                                    uri = notificationUri,
                                    name = name,
                                    isSelected = notificationUri.toString() == currentSoundUri
                                ))
                                count++
                            } catch (e: Exception) {
                                Log.w("SoundViewModel", "Skip sound at position ${cursor.position}: ${e.message}")
                            }
                        } while (cursor.moveToNext())
                    }
                    
                    soundItems
                }
                
                _soundList.clear()
                _soundList.addAll(sounds)
                
            } catch (e: Exception) {
                Log.e("SoundViewModel", "Error loading sounds: ${e.message}")
            } finally {
                isLoading = false
            }
        }
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