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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
            // 設定為多媒體音量播放，避免勿擾模式影響
            currentPlayingRingtone?.streamType = android.media.AudioManager.STREAM_MUSIC
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
        currentConditions.add("test")
        spUtil.editCondition(currentConditions)
        
        val notificationUtils = NotificationUtils()
        notificationUtils.sendNotification(
            context = this,
            packageName = packageName,
            messageBody = "Test notification - ${spUtil.getNotificationSoundName()} - ${System.currentTimeMillis()}",
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
                    text = stringResource(R.string.select_notification_sound_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.click_sound_name_to_preview),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
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
                    Text(stringResource(R.string.test_notification))
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
                    text = stringResource(R.string.available_sounds),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Sound count info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (viewModel.isLoading) 
                            stringResource(R.string.found_sounds_loading, viewModel.soundList.size) 
                        else 
                            stringResource(R.string.total_sounds_found, viewModel.soundList.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.reloadSounds(context) },
                            enabled = !viewModel.isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.reload),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.reload),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Group sounds by category
                    val groupedSounds = viewModel.soundList.groupBy { it.category }
                    
                    groupedSounds.forEach { (category, sounds) ->
                        item {
                            // Category header
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(sounds) { soundItem ->
                            SoundItemCard(
                                soundItem = soundItem,
                                onSoundSelected = { sound ->
                                    Log.d("NotificationSoundActivity", "🎵 User selected sound: ${sound.name}")
                                    viewModel.selectSound(sound)
                                    viewModel.saveSoundSelection(context, sound)
                                    onSoundSelected(sound)
                                }
                            )
                        }
                    }
                    
                    // Loading indicator at bottom when still loading
                    if (viewModel.isLoading) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // 跑步小人動畫效果
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "🏃",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = viewModel.loadingText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // 進度條
                                LinearProgressIndicator(
                                    progress = { viewModel.loadingProgress },
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(2.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "${(viewModel.loadingProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = soundItem.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (soundItem.isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.selected_sound),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.selected_sound),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            IconButton(
                onClick = { onSoundSelected(soundItem) }
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.play_and_select),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            if (soundItem.isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.selected_sound),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

data class SoundItem(
    val uri: Uri?,
    val name: String,
    val isSelected: Boolean = false,
    val category: String = "Other"
)