package com.eton.notification_me

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.NotificationManagerCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


open class MainActivity : AppCompatActivity() {
    companion object {
        const val CONDITION_KEY = "condition_key"
    }

    var conditionArray = arrayListOf<String>()
    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NotificationUtils().createNotificationChannel(this)

        getSP()
        val stringSet: MutableSet<String> =
            pref.getStringSet(CONDITION_KEY, buildSet {}) as MutableSet<String>
        conditionArray = stringSet.toMutableList() as ArrayList<String>

        initView()
    }

    override fun onResume() {
        super.onResume()
        if (!isPurview(this)) { // 檢查權限是否開啟，未開啟則開啟對話框
            AlertDialog.Builder(this@MainActivity)
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
                }.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                saveCondition()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCondition)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = ConditionAdapter(conditionArray)

        findViewById<AppCompatButton>(R.id.btnAdd).setOnClickListener {
            conditionArray.add("")
            (recyclerView.adapter as ConditionAdapter).notifyItemRangeInserted(
                conditionArray.size - 1,
                conditionArray.size
            )
        }
    }

    private fun getSP() {
        pref = this.getSharedPreferences("Condition", Context.MODE_PRIVATE)
    }

    /**
     * 儲存條件
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveCondition() {
        conditionArray.removeIf {
            it.isEmpty()
        }
        // 保存至 Sp
        pref.edit().putStringSet(CONDITION_KEY, conditionArray.toSet()).apply()
        // 通知條件設定
        NotificationUtils.condition = conditionArray
        // 顯示保存成功 toast
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
    }

    private fun isPurview(context: Context): Boolean { // 檢查權限是否開啟 true = 開啟 ，false = 未開啟
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        return packageNames.contains(context.packageName)
    }

    class ConditionAdapter(private val dataArray: ArrayList<String>) :
        RecyclerView.Adapter<ConditionAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val editText: EditText = view.findViewById(R.id.etCondition)
            val imgRemove: ImageView = view.findViewById(R.id.imgRemove)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_condition, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.editText.apply {
                setText(dataArray[position])
                doAfterTextChanged {
                    dataArray[position] = it.toString()
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