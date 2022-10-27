package com.eton.notification_me

import android.content.Context
import android.content.SharedPreferences
import com.eton.notification_me.SpUtil.SP.SP_NAME
import java.sql.Array

class SpUtil(context: Context) {
    object SP {
        const val SP_NAME = "Condition"
    }

    lateinit var  sp: SharedPreferences

    init {
        getSP(context)
    }

    private fun getSP(context: Context): SharedPreferences {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).also { sp = it }
    }

    fun getCondition(): MutableSet<String>? {
        return sp.getStringSet(MainActivity.CONDITION_KEY, buildSet {})
    }

    fun editCondition(conditionArray: Set<String>) {
        sp.edit().putStringSet(MainActivity.CONDITION_KEY, conditionArray).apply()
    }
}