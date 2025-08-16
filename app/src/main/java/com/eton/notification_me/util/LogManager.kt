package com.eton.notification_me.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class LogManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: LogManager? = null
        private const val TAG = "LogManager"
        private const val LOG_FILE_NAME = "notification_logs.txt"
        private const val MAX_LOG_SIZE = 10000 // 最大日誌行數
        
        fun getInstance(): LogManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LogManager().also { INSTANCE = it }
            }
        }
    }
    
    private val logs = mutableListOf<String>()
    private val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
    
    fun addLog(message: String, level: String = "INFO") {
        val timestamp = dateFormat.format(Date())
        val logEntry = "[$timestamp] $message"
        
        synchronized(logs) {
            logs.add(logEntry)
            
            // 限制日誌數量，避免記憶體過大
            if (logs.size > MAX_LOG_SIZE) {
                logs.removeAt(0)
            }
        }
        
        // 同時輸出到系統日誌
        when (level) {
            "ERROR" -> Log.e(TAG, message)
            "WARN" -> Log.w(TAG, message)
            "DEBUG" -> Log.d(TAG, message)
            else -> Log.i(TAG, message)
        }
    }
    
    fun addNotificationLog(message: String, level: String = "INFO") {
        val timestamp = dateFormat.format(Date())
        val logEntry = "[$timestamp] $message"
        
        synchronized(logs) {
            logs.add(logEntry)
            
            // 限制日誌數量，避免記憶體過大
            if (logs.size > MAX_LOG_SIZE) {
                logs.removeAt(0)
            }
        }
        
        // 同時輸出到系統日誌
        when (level) {
            "ERROR" -> Log.e(TAG, message)
            "WARN" -> Log.w(TAG, message)
            "DEBUG" -> Log.d(TAG, message)
            else -> Log.i(TAG, message)
        }
    }
    
    fun getAllLogs(): List<String> {
        return synchronized(logs) {
            logs.toList()
        }
    }
    
    fun clearLogs(context: Context? = null) {
        synchronized(logs) {
            logs.clear()
        }
        addLog("🧹 Logs cleared")
        
        // 清除後立即保存到文件
        context?.let { 
            saveLogsToFile(it)
        }
    }
    
    fun saveLogsToFile(context: Context): Boolean {
        return try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            val writer = FileWriter(file, false) // false = 覆寫文件
            
            synchronized(logs) {
                logs.forEach { logEntry ->
                    writer.appendLine(logEntry)
                }
            }
            
            writer.close()
            addLog("Logs saved to file: ${file.absolutePath}")
            true
        } catch (e: IOException) {
            addLog("Save logs failed: ${e.message}", "ERROR")
            false
        }
    }
    
    fun loadLogsFromFile(context: Context): Boolean {
        return try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (!file.exists()) {
                addLog("Log file does not exist")
                return false
            }
            
            val lines = file.readLines()
            synchronized(logs) {
                logs.clear()
                logs.addAll(lines)
            }
            
            addLog("Loaded ${lines.size} logs from file")
            true
        } catch (e: IOException) {
            addLog("Load logs failed: ${e.message}", "ERROR")
            false
        }
    }
    
    fun getLogFilePath(context: Context): String {
        return File(context.filesDir, LOG_FILE_NAME).absolutePath
    }
    
    fun getLogCount(): Int {
        return synchronized(logs) {
            logs.size
        }
    }
}