package com.eton.notification_me.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.eton.notification_me.SpUtil

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val spUtil = SpUtil(application)
    
    // Conditions state
    private val _conditions = mutableStateListOf<String>()
    val conditions: List<String> = _conditions
    
    // Loading state
    var isLoading by mutableStateOf(false)
        private set
    
    // Success message
    var successMessage by mutableStateOf<String?>(null)
        private set
    
    init {
        loadConditions()
    }
    
    private fun loadConditions() {
        val savedConditions = spUtil.getCondition()?.toList() ?: emptyList()
        _conditions.clear()
        _conditions.addAll(savedConditions)
        // Don't add empty condition automatically
    }
    
    fun addCondition() {
        _conditions.add("")
    }
    
    fun updateCondition(index: Int, value: String) {
        if (index < _conditions.size) {
            _conditions[index] = value
        }
    }
    
    fun removeCondition(index: Int) {
        if (index < _conditions.size) {
            _conditions.removeAt(index)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.N)
    fun saveConditions() {
        isLoading = true
        
        val filteredConditions = _conditions.filter { it.isNotBlank() }
        spUtil.editCondition(filteredConditions.toSet())
        
        // Update the list to remove empty conditions
        _conditions.clear()
        _conditions.addAll(filteredConditions)
        
        isLoading = false
        successMessage = "保存成功"
        
        // Clear success message after delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            successMessage = null
        }, 2000)
    }
    
    fun clearSuccessMessage() {
        successMessage = null
    }
}