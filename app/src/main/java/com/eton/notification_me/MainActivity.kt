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
                    IconButton(
                        onClick = { viewModel.addCondition() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "新增條件"
                        )
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    itemsIndexed(viewModel.conditions) { index, condition ->
                        ConditionItem(
                            condition = condition,
                            onValueChange = { viewModel.updateCondition(index, it) },
                            onRemove = { viewModel.removeCondition(index) }
                        )
                    }
                }
                
                Button(
                    onClick = { viewModel.saveConditions() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存條件")
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
                
                Button(
                    onClick = {
                        context.startActivity(Intent(context, AppListActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("選擇要監聽的應用程式")
                }
                
                Button(
                    onClick = {
                        context.startActivity(Intent(context, NotificationVolumeActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("設定通知聲音自動化處理")
                }
                
                Button(
                    onClick = {
                        context.startActivity(Intent(context, NotificationSoundActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("選擇通知音效")
                }
                
                Button(
                    onClick = {
                        context.startActivity(Intent(context, LogActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("查看運行日誌")
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
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = condition,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("輸入監控條件") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "刪除條件",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}