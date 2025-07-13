package com.eton.notification_me

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.eton.notification_me.util.LogManager
import android.widget.TextView
import android.widget.Button
import android.widget.ScrollView
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.Spanned


open class MainActivity : AppCompatActivity() {

    var conditionArray = arrayListOf<String>()
    private lateinit var spUtil: SpUtil
    lateinit var adapter: ConditionAdapter
    private var alertDialog: AlertDialog? = null
    private lateinit var logManager: LogManager
    private lateinit var tvLogs: TextView
    private lateinit var tvLogCount: TextView
    private lateinit var scrollView: ScrollView
    private val logUpdateHandler = Handler(Looper.getMainLooper())
    private val logUpdateRunnable = object : Runnable {
        override fun run() {
            updateLogDisplay()
            logUpdateHandler.postDelayed(this, 2000) // 每2秒更新一次
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue with the action or workflow in your app.
            Snackbar.make(findViewById(R.id.parentView), "Notification permission granted", Snackbar.LENGTH_SHORT).show()
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied.
            Snackbar.make(findViewById(R.id.parentView), "Notification permission denied", Snackbar.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NotificationUtils().createNotificationChannel(this)
        spUtil = SpUtil(this)
        val stringSet: MutableSet<String> = spUtil.getCondition() ?: mutableSetOf()
        conditionArray = stringSet.toMutableList() as ArrayList<String>

        // 初始化 LogManager
        logManager = LogManager.getInstance()
        logManager.addLog("MainActivity 啟動", "INFO")
        logManager.loadLogsFromFile(this)

        initView()
        
        // 開始自動更新日誌顯示
        logUpdateHandler.post(logUpdateRunnable)
    }

    override fun onResume() {
        super.onResume()
        if (!isPurview(this)) { // 檢查權限是否開啟，未開啟則開啟對話框
            alertDialog?.dismiss()
            if (alertDialog != null) {
                alertDialog?.show()
                return
            }
            alertDialog = AlertDialog.Builder(this@MainActivity)
                .setTitle(R.string.app_name)
                .setMessage("請啟用通知欄擷取權限")
                .setIcon(R.mipmap.ic_launcher_round)
                .setOnCancelListener { // 對話框取消事件
                    finish()
                }
                .setPositiveButton(
                    "前往"
                ) { _, _ ->
                    // 對話框按鈕事件
                    // 跳轉自開啟權限畫面，權限開啟後通知欄擷取服務將自動啟動。
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }.create()
            alertDialog?.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                val inputMethodManager =
                    getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                saveCondition()
                adapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCondition)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = ConditionAdapter(conditionArray)
        recyclerView.adapter = adapter

        findViewById<AppCompatButton>(R.id.btnAdd).setOnClickListener {
            conditionArray.add("")
            adapter.notifyItemRangeInserted(
                conditionArray.size - 1,
                conditionArray.size
            )
        }

        findViewById<AppCompatButton>(R.id.btnPickApp).setOnClickListener {
            startActivity(Intent(this, AppListActivity::class.java))
        }
        findViewById<AppCompatButton>(R.id.btnSetNotificationVolume).setOnClickListener {
            startActivity(Intent(this, NotificationVolumeActivity::class.java))
        }
        
        // 初始化日誌相關UI
        tvLogs = findViewById(R.id.tvLogs)
        tvLogCount = findViewById(R.id.tvLogCount)
        scrollView = findViewById(R.id.scrollViewLogs)
        
        // 清除日誌按鈕
        findViewById<Button>(R.id.btnClearLogs).setOnClickListener {
            logManager.clearLogs()
            logManager.addLog("手動清除日誌", "INFO")
            updateLogDisplay()
        }
        
        // 保存日誌按鈕
        findViewById<Button>(R.id.btnSaveLogs).setOnClickListener {
            val success = logManager.saveLogsToFile(this)
            if (success) {
                Snackbar.make(findViewById(R.id.parentView), "日誌已保存", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(findViewById(R.id.parentView), "日誌保存失敗", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 更新日誌顯示
     */
    private fun updateLogDisplay() {
        val logs = logManager.getAllLogs()
        val logText = logs.joinToString("<br>")
        
        // 保存當前滾動位置
        val currentScrollY = scrollView.scrollY
        
        if (logText.isNotEmpty()) {
            val spanned: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(logText, Html.FROM_HTML_MODE_COMPACT)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(logText)
            }
            tvLogs.text = spanned
        } else {
            tvLogs.text = "日誌將在此處顯示..."
        }
        
        tvLogCount.text = "${logs.size} 條"
        
        // 恢復滾動位置，防止自動滾動
        scrollView.post {
            scrollView.scrollTo(0, currentScrollY)
        }
        
        // 自動保存（每10次更新保存一次）
        if (logs.size % 10 == 0 && logs.isNotEmpty()) {
            logManager.saveLogsToFile(this)
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

    /**
     * 儲存條件
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveCondition() {
        val temp = conditionArray.toMutableList()
        temp.removeIf {
            it.isEmpty()
        }
        conditionArray.clear()
        conditionArray.addAll(temp)
        // 保存至 Sp
        spUtil.editCondition(conditionArray.toSet())
        // 顯示保存成功 Snackbar/
        Snackbar.make(findViewById(R.id.parentView), "保存成功", Snackbar.LENGTH_SHORT).show()
    }

    private fun isPurview(context: Context): Boolean { // 檢查權限是否開啟 true = 開啟 ，false = 未開啟
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected.
                    Snackbar.make(findViewById(R.id.parentView), "Notification permission is required to send notifications", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK") {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }.show()
                }
                else -> {
                    // Directly ask for the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        return packageNames.contains(context.packageName)
    }

    class ConditionAdapter(private val dataArray: ArrayList<String>) :
        RecyclerView.Adapter<ConditionAdapter.ViewHolder>() {

        class ViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
            val editText: EditText = view.findViewById(R.id.etCondition)
            val imgRemove: ImageView = view.findViewById(R.id.imgRemove)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_condition, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            @SuppressLint("RecyclerView") position: Int
        ) {
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(editable: Editable?) {
                    dataArray[position] = editable.toString()
                }
            }
            holder.editText.apply {
                setText(dataArray[position])
                this.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        this.addTextChangedListener(textWatcher)
                    } else {
                        this.removeTextChangedListener(textWatcher)
                    }
                }
            }
            holder.imgRemove.setOnClickListener {
                dataArray.removeAt(position)
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int {
            return dataArray.size
        }
    }
}