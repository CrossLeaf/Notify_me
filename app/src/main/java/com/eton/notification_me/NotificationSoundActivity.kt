package com.eton.notification_me

import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.media.Ringtone
import android.util.Log
import kotlinx.coroutines.*
import android.widget.ProgressBar

class NotificationSoundActivity : AppCompatActivity() {
    private lateinit var spUtil: SpUtil
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SoundAdapter
    private val soundList = mutableListOf<SoundItem>()
    private var currentPlayingRingtone: Ringtone? = null
    private lateinit var progressBar: ProgressBar
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_sound)
        
        spUtil = SpUtil(this)
        initViews()
        loadSounds()
    }

    private fun initViews() {
        // Use the default action bar instead of custom toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "選擇通知音效"
        
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerViewSounds)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SoundAdapter(soundList) { soundItem ->
            onSoundSelected(soundItem)
        }
        recyclerView.adapter = adapter
        
        // Test notification button
        findViewById<android.widget.Button>(R.id.btnTestNotification).setOnClickListener {
            testNotification()
        }
        
        // Initially hide RecyclerView and show progress bar
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun loadSounds() {
        scope.launch {
            try {
                val sounds = withContext(Dispatchers.IO) {
                    val soundItems = mutableListOf<SoundItem>()
                    val ringtoneManager = RingtoneManager(this@NotificationSoundActivity)
                    ringtoneManager.setType(RingtoneManager.TYPE_NOTIFICATION)
                    
                    val cursor = ringtoneManager.cursor
                    val currentSoundUri = spUtil.getNotificationSoundUri()
                    
                    // 添加預設音效選項 (使用專案內的warning.mp3)
                    val defaultSoundUri = Uri.parse("${android.content.ContentResolver.SCHEME_ANDROID_RESOURCE}://${this@NotificationSoundActivity.packageName}/${R.raw.warning}")
                    val isDefaultSelected = currentSoundUri == null || currentSoundUri == defaultSoundUri.toString()
                    soundItems.add(SoundItem(
                        uri = defaultSoundUri,
                        name = "預設通知音效 (Warning)",
                        isSelected = isDefaultSelected
                    ))
                    
                    // 添加系統音效 - 限制數量以避免過長載入時間
                    if (cursor.moveToFirst()) {
                        var count = 0
                        do {
                            if (count >= 20) break // 限制最多20個音效以避免ANR
                            
                            try {
                                val notificationUri = ringtoneManager.getRingtoneUri(cursor.position)
                                val name = try {
                                    ringtoneManager.getRingtone(cursor.position)?.getTitle(this@NotificationSoundActivity) 
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
                                Log.w("NotificationSoundActivity", "Skip sound at position ${cursor.position}: ${e.message}")
                            }
                        } while (cursor.moveToNext())
                    }
                    
                    soundItems
                }
                
                // Update UI on main thread
                soundList.clear()
                soundList.addAll(sounds)
                adapter.notifyDataSetChanged()
                
                // Hide progress bar and show list
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                
            } catch (e: Exception) {
                Log.e("NotificationSoundActivity", "Error loading sounds: ${e.message}")
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                Toast.makeText(this@NotificationSoundActivity, "載入音效失敗: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onSoundSelected(selectedSound: SoundItem) {
        // 停止當前播放的音效
        currentPlayingRingtone?.stop()
        
        // 更新選中狀態
        soundList.forEach { it.isSelected = false }
        selectedSound.isSelected = true
        adapter.notifyDataSetChanged()
        
        // 播放選中的音效
        try {
            currentPlayingRingtone = RingtoneManager.getRingtone(this, selectedSound.uri)
            currentPlayingRingtone?.play()
        } catch (e: Exception) {
            Log.e("NotificationSoundActivity", "Error playing sound: ${e.message}")
        }
        
        // 保存選擇
        spUtil.setNotificationSoundUri(selectedSound.uri?.toString())
        spUtil.setNotificationSoundName(selectedSound.name)
        
        // 重置NotificationUtils的音效快取，強制重新創建通知頻道
        try {
            val notificationUtilsClass = Class.forName("com.eton.notification_me.NotificationUtils")
            val lastSoundUriField = notificationUtilsClass.getDeclaredField("lastSoundUri")
            lastSoundUriField.isAccessible = true
            lastSoundUriField.set(null, null) // 重置快取
            
            Log.d("NotificationSoundActivity", "🔄 已重置音效快取")
        } catch (e: Exception) {
            Log.w("NotificationSoundActivity", "無法重置音效快取: ${e.message}")
        }
        
        Toast.makeText(this, "已選擇: ${selectedSound.name}", Toast.LENGTH_SHORT).show()
    }

    private fun testNotification() {
        // 確保包名在監聽列表中
        val currentPackages = spUtil.getPackageName().toMutableSet()
        currentPackages.add(packageName)
        spUtil.editPackageName(currentPackages)
        
        // 確保有測試條件
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
        Toast.makeText(this, "已發送測試通知", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentPlayingRingtone?.stop()
        scope.cancel() // Cancel coroutines to prevent memory leaks
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    data class SoundItem(
        val uri: Uri?,
        val name: String,
        var isSelected: Boolean = false
    )

    class SoundAdapter(
        private val soundList: List<SoundItem>,
        private val onSoundClick: (SoundItem) -> Unit
    ) : RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {

        class SoundViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvSoundName: TextView = view.findViewById(R.id.tvSoundName)
            val tvSelected: TextView = view.findViewById(R.id.tvSelected)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sound, parent, false)
            return SoundViewHolder(view)
        }

        override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
            val sound = soundList[position]
            holder.tvSoundName.text = sound.name
            holder.tvSelected.visibility = if (sound.isSelected) View.VISIBLE else View.GONE
            
            holder.itemView.setOnClickListener {
                onSoundClick(sound)
            }
        }

        override fun getItemCount() = soundList.size
    }
}