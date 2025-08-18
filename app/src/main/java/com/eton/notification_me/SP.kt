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
        const val NOTIFICATION_SOUND_URI_KEY = "notification_sound_uri_key"
        const val NOTIFICATION_SOUND_NAME_KEY = "notification_sound_name_key"
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
     * Get message content
     */
    fun getMessageBody(): String {
        return sp.getString(MESSAGE_BODY_KEY, "") ?: ""
    }

    /**
     * Edit message content
     */
    fun editMessageBody(messageBody: String) {
        sp.edit().putString(MESSAGE_BODY_KEY, messageBody).apply()
    }

    /**
     * Get last notification time
     */
    fun getLastNotificationTime(): Long {
        return sp.getLong(LAST_NOTIFICATION_TIME_KEY, 0L)
    }

    /**
     * Set last notification time
     */
    fun setLastNotificationTime(timestamp: Long) {
        sp.edit().putLong(LAST_NOTIFICATION_TIME_KEY, timestamp).apply()
    }

    /**
     * Get notification sound URI
     */
    fun getNotificationSoundUri(): String? {
        return sp.getString(NOTIFICATION_SOUND_URI_KEY, null)
    }

    /**
     * Set notification sound URI
     */
    fun setNotificationSoundUri(uri: String?) {
        sp.edit().putString(NOTIFICATION_SOUND_URI_KEY, uri).apply()
    }

    /**
     * Get notification sound name
     */
    fun getNotificationSoundName(): String {
        return sp.getString(NOTIFICATION_SOUND_NAME_KEY, "Default notification sound (Warning)") ?: "Default notification sound (Warning)"
    }

    /**
     * Set notification sound name
     */
    fun setNotificationSoundName(name: String) {
        sp.edit().putString(NOTIFICATION_SOUND_NAME_KEY, name).apply()
    }
}