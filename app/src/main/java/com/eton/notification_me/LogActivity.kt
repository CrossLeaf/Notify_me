package com.eton.notification_me

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.eton.notification_me.ui.theme.NotificationMeTheme
import com.eton.notification_me.util.LogManager

class LogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            NotificationMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LogScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen() {
    val context = LocalContext.current
    val logManager = LogManager.getInstance()
    
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
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "運行日誌",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "應用程式運行和通知處理記錄",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    logManager.clearLogs()
                    logManager.addLog("手動清除日誌", "INFO")
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("清除日誌")
            }
            
            Button(
                onClick = {
                    val success = logManager.saveLogsToFile(context)
                    // TODO: Show toast or snackbar with result
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存日誌")
            }
        }
        
        // Log info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "日誌數量: ${logManager.getAllLogs().size} 條",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "實時更新",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Terminal-style log display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val logs = logManager.getAllLogs()
                val logText = logs.joinToString("\n")
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (logText.isNotEmpty()) {
                            "$ tail -f notification_me.log\n$logText"
                        } else {
                            "$ tail -f notification_me.log\n等待日誌輸出..."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00FF00),
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.2f
                    )
                }
                
                // Cursor blinking effect
                if (logText.isNotEmpty()) {
                    var showCursor by remember { mutableStateOf(true) }
                    LaunchedEffect(Unit) {
                        while (true) {
                            kotlinx.coroutines.delay(500)
                            showCursor = !showCursor
                        }
                    }
                    
                    Text(
                        text = if (showCursor) "_" else " ",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00FF00),
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
            }
        }
    }
}