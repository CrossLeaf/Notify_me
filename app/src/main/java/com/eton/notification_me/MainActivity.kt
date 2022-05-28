package com.eton.notification_me

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import kotlin.math.log


open class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        smallIcon = findViewById(R.id.smallIcon)
        largeIcon = findViewById(R.id.largeIcon)

        if (!isPurview(this)) { // 檢查權限是否開啟，未開啟則開啟對話框
            AlertDialog.Builder(this@MainActivity)
                .setTitle(R.string.app_name)
                .setMessage("請啟用通知欄擷取權限")
                .setIcon(R.mipmap.ic_launcher_round)
                .setOnCancelListener { // 對話框取消事件
                    finish()
                }
                .setPositiveButton(
                    "前往"
                ) { _, _ ->
                    // 對話框按鈕事件
                    // 跳轉自開啟權限畫面，權限開啟後通知欄擷取服務將自動啟動。
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }.show()
        }
    }

    private fun isPurview(context: Context): Boolean { // 檢查權限是否開啟 true = 開啟 ，false = 未開啟
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        return packageNames.contains(context.packageName)
    }

    companion object {
        var textView: TextView? = null
        var smallIcon: ImageView? = null
        var largeIcon: ImageView? = null

        //儲存通知訊息的應用程式小圖示
        var drawableIcon: Drawable? = null

        //儲存通知訊息大圖示
        var bitmapIcon: Bitmap? = null

        //儲存包名、標題、內容文字
        var string: String? = null

        val handler = Handler(Looper.getMainLooper()) {
            //將資料顯示，更新至畫面
            textView!!.text = string
            smallIcon!!.setImageDrawable(drawableIcon)
            largeIcon!!.setImageBitmap(bitmapIcon)
            true
        }

        fun show(
            packageName: String,
            title: String?,
            text: String?,
            small: Drawable?,
            large: Bitmap?
        ) {
            string = """
             包名：$packageName
             
             標題：$title
             
             文字：$text
             
             
             """.trimIndent()
            drawableIcon = small
            bitmapIcon = large
            Thread {
                val msg: Message = Message.obtain()
                handler.sendMessage(msg)
            }.start()
            Log.d(packageName, "show: title = $title, text = $text")
        }
    }
}