package com.eton.notification_me

import android.content.Context
import android.content.SharedPreferences

class SpUtil {
    object SpUtil {
        fun getSP(context: Context): SharedPreferences {
            return context.getSharedPreferences("Condition", Context.MODE_PRIVATE)
        }
    }
}