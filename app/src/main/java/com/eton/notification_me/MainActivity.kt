package com.eton.notification_me

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.Spanned
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eton.notification_me.ui.theme.NotificationMeTheme
import com.eton.notification_me.util.LogManager
import com.eton.notification_me.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private var alertDialog: AlertDialog? = null
    private lateinit var logManager: LogManager
    private val logUpdateHandler = Handler(Looper.getMainLooper())
    private val logUpdateRunnable = object : Runnable {
        override fun run() {
            // Update logs will be handled in Compose
            logUpdateHandler.postDelayed(this, 2000)
        }
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize dependencies
        val spUtil = SpUtil(this)
        logManager = LogManager.getInstance()
        logManager.addLog("MainActivity 啟動", "INFO")
        logManager.loadLogsFromFile(this)
        
        // Initialize default sound settings
        if (spUtil.getNotificationSoundUri() == null) {
            val defaultSoundUri = android.net.Uri.parse("${android.content.ContentResolver.SCHEME_ANDROID_RESOURCE}://${packageName}/${R.raw.warning}")
            spUtil.setNotificationSoundUri(defaultSoundUri.toString())
            spUtil.setNotificationSoundName("預設通知音效 (Warning)")
        }
        
        // Create notification channel
        val notificationUtils = NotificationUtils()
        val currentSoundUriString = spUtil.getNotificationSoundUri()
        val currentSoundUri = if (currentSoundUriString != null) {
            android.net.Uri.parse(currentSoundUriString)
        } else {
            android.net.Uri.parse("${android.content.ContentResolver.SCHEME_ANDROID_RESOURCE}://${packageName}/${R.raw.warning}")
        }
        notificationUtils.createNotificationChannelWithSound(this, currentSoundUri)
        
        setContent {
            NotificationMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
        
        logUpdateHandler.post(logUpdateRunnable)
    }

    override fun onResume() {
        super.onResume()
        if (!isPurview(this)) {
            alertDialog?.dismiss()
            if (alertDialog != null) {
                alertDialog?.show()
                return
            }
            alertDialog = AlertDialog.Builder(this@MainActivity)
                .setTitle(R.string.app_name)
                .setMessage("請啟用通知欄擷取權限")
                .setIcon(R.mipmap.ic_launcher_round)
                .setOnCancelListener { finish() }
                .setPositiveButton("前往") { _, _ ->
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }.create()
            alertDialog?.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logUpdateHandler.removeCallbacks(logUpdateRunnable)
        logManager.saveLogsToFile(this)
        logManager.addLog("MainActivity 銷毀", "INFO")
    }

    override fun onPause() {
        super.onPause()
        logManager.saveLogsToFile(this)
    }

    private fun isPurview(context: Context): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show rationale and request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        return packageNames.contains(context.packageName)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Title
        Text(
            text = "通知監控設定",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Conditions Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "監控條件",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row {
                        IconButton(
                            onClick = { viewModel.addCondition() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "新增條件",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.saveConditions() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "保存條件",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                
                // Display success message
                viewModel.successMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                // Dynamic height based on content
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.conditions.forEachIndexed { index, condition ->
                        ConditionItem(
                            condition = condition,
                            onValueChange = { viewModel.updateCondition(index, it) },
                            onRemove = { viewModel.removeCondition(index) }
                        )
                    }
                }
                
                // Quick add button with hint
                if (viewModel.conditions.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "點擊右上角的 + 按鈕新增監控條件",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
        
        // Action Buttons
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "應用程式設定",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                FilledTonalButton(
                    onClick = {
                        context.startActivity(Intent(context, AppListActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "選擇要監聽的應用程式",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                
                FilledTonalButton(
                    onClick = {
                        context.startActivity(Intent(context, NotificationVolumeActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "設定通知聲音自動化處理",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                
                FilledTonalButton(
                    onClick = {
                        context.startActivity(Intent(context, NotificationSoundActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "選擇通知音效",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(context, LogActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "查看運行日誌",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ConditionItem(
    condition: String,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = condition,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        text = "輸入監控條件 (例如：急件、重要通知)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "刪除條件",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}