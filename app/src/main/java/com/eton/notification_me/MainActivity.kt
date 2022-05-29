package com.eton.notification_me

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


open class MainActivity : AppCompatActivity() {

    var conditionArray = arrayListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        NotificationUtils().createNotificationChannel(this)
        conditionArray.add("123")
        conditionArray.add("46")
        conditionArray.add("13")

//        NotificationUtils.condition = arrayListOf("123", "456", "eton")
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
            holder.editText.setText(dataArray[position])
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