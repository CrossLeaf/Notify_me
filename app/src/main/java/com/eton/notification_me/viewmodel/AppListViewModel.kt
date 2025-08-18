package com.eton.notification_me.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eton.notification_me.AppBean
import com.eton.notification_me.SpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppListViewModel(application: Application) : AndroidViewModel(application) {
    private val spUtil = SpUtil(application)
    private val packageNameSet = mutableSetOf<String>()
    
    // App list state
    private val _appList = mutableStateListOf<AppBean>()
    val appList: List<AppBean> = _appList
    
    var isLoading by mutableStateOf(false)
        private set
    
    var loadingProgress by mutableStateOf(0f)
        private set
    
    var loadingText by mutableStateOf("Loading...")
        private set
    
    init {
        packageNameSet.addAll(spUtil.getPackageName())
    }
    
    fun loadApps(context: Context) {
        if (_appList.isNotEmpty()) return // Already loaded
        
        viewModelScope.launch {
            isLoading = true
            loadingProgress = 0f
            loadingText = "Scanning installed apps..."
            
            try {
                val apps = withContext(Dispatchers.IO) {
                    loadInstalledApps(context)
                }
                
                loadingText = "Loading app info..."
                loadingProgress = 0.5f
                
                // Progressively load applications to UI
                apps.forEachIndexed { index, appBean ->
                    _appList.add(appBean)
                    loadingProgress = 0.5f + (0.5f * (index + 1) / apps.size)
                    loadingText = "Loading... (${index + 1}/${apps.size})"
                    
                    // Pause every 10 applications to give UI time to update
                    if (index % 10 == 0) {
                        kotlinx.coroutines.delay(50)
                    }
                }
                
                loadingText = "Load complete"
                loadingProgress = 1.0f
                
                Log.d("AppListViewModel", "Loaded ${apps.size} apps")
                
            } catch (e: Exception) {
                Log.e("AppListViewModel", "Error loading apps: ${e.message}")
                loadingText = "Load failed: ${e.message}"
            } finally {
                kotlinx.coroutines.delay(300) // Show completion state briefly
                isLoading = false
                loadingProgress = 0f
            }
        }
    }
    
    private fun loadInstalledApps(context: Context): List<AppBean> {
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            .filter {
                (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) <= 0
                        && !(it.packageName?.contentEquals(context.packageName) ?: false)
            }
            .sortedBy { it.applicationInfo.loadLabel(packageManager).toString() }
        
        val apps = mutableListOf<AppBean>()
        installedApps.forEach { packageInfo ->
            try {
                val appBean = AppBean(
                    label = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                    packageName = packageInfo.packageName,
                    icon = packageInfo.applicationInfo.loadIcon(packageManager),
                    check = packageNameSet.contains(packageInfo.packageName)
                )
                apps.add(appBean)
            } catch (e: Exception) {
                Log.w("AppListViewModel", "Failed to load app: ${packageInfo.packageName}", e)
            }
        }
        return apps
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