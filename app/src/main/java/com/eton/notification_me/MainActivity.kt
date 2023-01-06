package com.eton.notification_me

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar


open class MainActivity : AppCompatActivity() {

    var conditionArray = arrayListOf<String>()
    private lateinit var spUtil: SpUtil
    lateinit var adapter: ConditionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NotificationUtils().createNotificationChannel(this)
        spUtil = SpUtil(this)
        val stringSet: MutableSet<String> = spUtil.getCondition() ?: mutableSetOf()
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