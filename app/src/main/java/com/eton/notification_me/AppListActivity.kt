package com.eton.notification_me

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)
        initData()
        initView()
    }

    fun initView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAppList)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        adapter = PackageAdapter(dataArray)
        recyclerView.adapter = adapter
    }

    private fun initData() {
        val installedApplications = packageManager.getInstalledPackages(0)
        installedApplications
            .filter {
                (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) <= 0
            }
            .forEach {
                dataArray.add(
                    AppBean(
                        it.applicationInfo.loadLabel(packageManager).toString(),
                        it.packageName,
                        it.applicationInfo.loadIcon(packageManager),
                        false
                    )
                )
            }
    }

    class PackageAdapter(private val dataArray: ArrayList<AppBean>) :
        RecyclerView.Adapter<PackageAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imgIcon: ImageView = view.findViewById(R.id.imgIcon)
            val checkBox: CheckBox = view.findViewById(R.id.checkBox)
            val tvName: TextView = view.findViewById(R.id.tvName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_list, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.apply {
                checkBox.isChecked = dataArray[position].check
                imgIcon.setImageDrawable(dataArray[position].icon)
                tvName.text = dataArray[position].label
            }
            // TODO: 儲存所選的 app package name
        }

        override fun getItemCount(): Int {
            return dataArray.size
        }
    }
}

data class AppBean(
    val label: String,
    val packageName: String,
    val icon: Drawable,
    var check: Boolean
)