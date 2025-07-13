package com.eton.notification_me

import android.content.Context
import android.content.SharedPreferences

class SpUtil(context: Context) {
    private companion object SP {
        const val SP_NAME = "Condition"
        const val CONDITION_KEY = "condition_key"
        const val PACKAGE_NAME_KEY = "package_name_key"
        const val MESSAGE_BODY_KEY = "message_body_key"
        const val LAST_NOTIFICATION_TIME_KEY = "last_notification_time_key"
    }

    private lateinit var sp: SharedPreferences

    init {
        getSP(context)
    }

    private fun getSP(context: Context): SharedPreferences {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).also { sp = it }
    }

    fun getCondition(): MutableSet<String>? {
        return sp.getStringSet(CONDITION_KEY, buildSet {})
    }

    fun editCondition(conditionSet: Set<String>) {
        sp.edit().putStringSet(CONDITION_KEY, conditionSet).commit()
    }

    fun getPackageName(): MutableSet<String> {
        return sp.getStringSet(PACKAGE_NAME_KEY, buildSet {}) ?: mutableSetOf()
    }

    fun editPackageName(packageNameSet: Set<String>) {
        sp.edit().putStringSet(PACKAGE_NAME_KEY, packageNameSet).apply()
    }

    /**
     * 獲取訊息內容
     */
    fun getMessageBody(): String {
        return sp.getString(MESSAGE_BODY_KEY, "") ?: ""
    }

    /**
     * 編輯訊息內容
     */
    fun editMessageBody(messageBody: String) {
        sp.edit().putString(MESSAGE_BODY_KEY, messageBody).apply()
    }

    /**
     * 獲取最後通知時間
     */
    fun getLastNotificationTime(): Long {
        return sp.getLong(LAST_NOTIFICATION_TIME_KEY, 0L)
    }

    /**
     * 設定最後通知時間
     */
    fun setLastNotificationTime(timestamp: Long) {
        sp.edit().putLong(LAST_NOTIFICATION_TIME_KEY, timestamp).apply()
    }
}