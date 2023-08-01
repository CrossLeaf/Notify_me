package com.eton.notification_me

import android.content.Context
import android.content.SharedPreferences

class SpUtil(context: Context) {
    companion object SP {
        const val SP_NAME = "Condition"
        const val CONDITION_KEY = "condition_key"
        const val PACKAGE_NAME_KEY = "package_name_key"
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
}