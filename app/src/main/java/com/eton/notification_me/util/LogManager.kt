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
        private const val MAX_LOG_SIZE = 10000 // Maximum log lines
        
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
            
            // Limit log count to avoid excessive memory usage
            if (logs.size > MAX_LOG_SIZE) {
                logs.removeAt(0)
            }
        }
        
        // Also output to system log
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
            
            // Limit log count to avoid excessive memory usage
            if (logs.size > MAX_LOG_SIZE) {
                logs.removeAt(0)
            }
        }
        
        // Also output to system log
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
        
        // Save to file immediately after clearing
        context?.let { 
            saveLogsToFile(it)
        }
    }
    
    fun saveLogsToFile(context: Context): Boolean {
        return try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            val writer = FileWriter(file, false) // false = overwrite file
            
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