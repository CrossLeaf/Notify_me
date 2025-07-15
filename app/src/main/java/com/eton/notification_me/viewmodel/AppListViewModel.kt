package com.eton.notification_me.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.eton.notification_me.AppBean
import com.eton.notification_me.SpUtil

class AppListViewModel(application: Application) : AndroidViewModel(application) {
    private val spUtil = SpUtil(application)
    private val packageNameSet = mutableSetOf<String>()
    
    // App list state
    private val _appList = mutableStateListOf<AppBean>()
    val appList: List<AppBean> = _appList
    
    init {
        packageNameSet.addAll(spUtil.getPackageName())
    }
    
    fun loadApps(context: Context) {
        if (_appList.isNotEmpty()) return // Already loaded
        
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            .filter {
                (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) <= 0
                        && !(it.packageName?.contentEquals(context.packageName) ?: false)
            }
            .sortedBy { it.applicationInfo.loadLabel(packageManager).toString() }
        
        installedApps.forEach { packageInfo ->
            try {
                val appBean = AppBean(
                    label = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                    packageName = packageInfo.packageName,
                    icon = packageInfo.applicationInfo.loadIcon(packageManager),
                    check = packageNameSet.contains(packageInfo.packageName)
                )
                _appList.add(appBean)
                Log.d("AppListViewModel", "Added app: ${appBean.label}")
            } catch (e: Exception) {
                Log.w("AppListViewModel", "Failed to load app: ${packageInfo.packageName}", e)
            }
        }
    }
    
    fun toggleAppSelection(app: AppBean, isSelected: Boolean) {
        val index = _appList.indexOfFirst { it.packageName == app.packageName }
        if (index != -1) {
            _appList[index] = app.copy(check = isSelected)
            
            if (isSelected) {
                packageNameSet.add(app.packageName)
            } else {
                packageNameSet.remove(app.packageName)
            }
            
            spUtil.editPackageName(packageNameSet)
        }
    }
}