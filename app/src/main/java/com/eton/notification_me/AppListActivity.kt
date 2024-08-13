package com.eton.notification_me

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppListActivity : AppCompatActivity() {
    lateinit var adapter: PackageAdapter
    var dataArray = arrayListOf<AppBean>()
    lateinit var spUtil: SpUtil
    val packageNameSet = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)
        initData()
        initView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAppList)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        adapter = PackageAdapter(dataArray)
        recyclerView.adapter = adapter
    }

    private fun initData() {
        spUtil = SpUtil(this)
        packageNameSet.addAll(spUtil.getPackageName())

        packageManager.getInstalledPackages(0)
            .filter {
                (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) <= 0
                        // 判斷是否是自己的 app, 是的話就不顯示
                        && !(it.packageName?.contentEquals(this.applicationContext.packageName)
                    ?: false)
            }
            .sortedBy { it.applicationInfo.loadLabel(packageManager).toString() } // 按照應用程式名稱排序
            .forEach {
                Log.d("TAG", "initData: ${it.applicationInfo.loadLabel(packageManager)}")
                dataArray.add(
                    AppBean(
                        it.applicationInfo.loadLabel(packageManager).toString(),
                        it.packageName,
                        it.applicationInfo.loadIcon(packageManager),
                        packageNameSet.contains(it.packageName)
                    )
                )
            }
    }

    inner class PackageAdapter(private val dataArray: ArrayList<AppBean>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val VIEW_TYPE_ITEM = 0
        private val VIEW_TYPE_FOOTER = 1

        inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imgIcon: ImageView = view.findViewById(R.id.imgIcon)
            val checkBox: CheckBox = view.findViewById(R.id.checkBox)
            val tvName: TextView = view.findViewById(R.id.tvName)
        }

        inner class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val footerText: TextView = view.findViewById(R.id.footerText)
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == dataArray.size) VIEW_TYPE_FOOTER else VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == VIEW_TYPE_ITEM) {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_app_list, parent, false)
                ItemViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_footer, parent, false)
                FooterViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ItemViewHolder) {
                holder.apply {
                    dataArray[position].also {
                        checkBox.isChecked = it.check
                        imgIcon.setImageDrawable(it.icon)
                        tvName.text = it.label
                        checkBox.setOnClickListener { view ->
                            val isChecked = (view as CheckBox).isChecked
                            it.check = isChecked
                            if (isChecked) {
                                packageNameSet.add(it.packageName)
                            } else {
                                packageNameSet.remove(it.packageName)
                            }
                            spUtil.editPackageName(packageNameSet)
                        }
                    }
                }
            } else if (holder is FooterViewHolder) {
                holder.footerText.text = "No more data"
            }
        }

        override fun getItemCount(): Int {
            return dataArray.size + 1
        }
    }
}

data class AppBean(
    val label: String,
    val packageName: String,
    val icon: Drawable,
    var check: Boolean
)