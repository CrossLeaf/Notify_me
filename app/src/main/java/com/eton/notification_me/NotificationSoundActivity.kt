package com.eton.notification_me

import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.media.Ringtone
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eton.notification_me.ui.theme.NotificationMeTheme
import com.eton.notification_me.viewmodel.SoundViewModel
import kotlinx.coroutines.*

class NotificationSoundActivity : ComponentActivity() {
    private var currentPlayingRingtone: Ringtone? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            NotificationMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SoundSelectionScreen(
                        onSoundSelected = { soundItem ->
                            playSound(soundItem)
                        },
                        onTestNotification = {
                            testNotification()
                        }
                    )
                }
            }
        }
    }

    private fun playSound(soundItem: SoundItem) {
        currentPlayingRingtone?.stop()
        try {
            currentPlayingRingtone = RingtoneManager.getRingtone(this, soundItem.uri)
            currentPlayingRingtone?.play()
        } catch (e: Exception) {
            Log.e("NotificationSoundActivity", "Error playing sound: ${e.message}")
        }
    }

    private fun testNotification() {
        val spUtil = SpUtil(this)
        
        // Ensure package name is in monitoring list
        val currentPackages = spUtil.getPackageName().toMutableSet()
        currentPackages.add(packageName)
        spUtil.editPackageName(currentPackages)
        
        // Ensure test condition exists
        val currentConditions = spUtil.getCondition()?.toMutableSet() ?: mutableSetOf()
        currentConditions.add("測試")
        spUtil.editCondition(currentConditions)
        
        val notificationUtils = NotificationUtils()
        notificationUtils.sendNotification(
            context = this,
            packageName = packageName,
            messageBody = "測試通知 - ${spUtil.getNotificationSoundName()} - ${System.currentTimeMillis()}",
            smallIcon = null
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        currentPlayingRingtone?.stop()
        scope.cancel()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundSelectionScreen(
    onSoundSelected: (SoundItem) -> Unit,
    onTestNotification: () -> Unit,
    viewModel: SoundViewModel = viewModel()
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.loadSounds(context)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "選擇通知音效",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "點擊音效名稱可預覽並選擇",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onTestNotification,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("測試通知")
                }
            }
        }
        
        // Sound List
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "可用音效",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (viewModel.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.soundList) { soundItem ->
                            SoundItemCard(
                                soundItem = soundItem,
                                onSoundSelected = { sound ->
                                    Log.d("NotificationSoundActivity", "🎵 用戶選擇音效: ${sound.name}")
                                    viewModel.selectSound(sound)
                                    viewModel.saveSoundSelection(context, sound)
                                    onSoundSelected(sound)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoundItemCard(
    soundItem: SoundItem,
    onSoundSelected: (SoundItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (soundItem.isSelected) 
                MaterialTheme.colorScheme.secondaryContainer
            else 
                MaterialTheme.colorScheme.surface
        ),
        onClick = { onSoundSelected(soundItem) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = soundItem.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (soundItem.isSelected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                if (soundItem.isSelected) {
                    Text(
                        text = "已選中",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            IconButton(
                onClick = { onSoundSelected(soundItem) }
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "播放並選擇",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            if (soundItem.isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已選中",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

data class SoundItem(
    val uri: Uri?,
    val name: String,
    val isSelected: Boolean = false
)