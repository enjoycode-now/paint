package com.wacom.will3.ink.raster.rendering.demo.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

object ToastUtils {
    lateinit var app: AppCompatActivity
    var lastDisplayString = ""
    var lastDisplayTime = 0L

    fun toast(string: String){
        if(string == lastDisplayString && System.currentTimeMillis() < lastDisplayTime) return
        lastDisplayString= string
        lastDisplayTime = System.currentTimeMillis()+2500
        app.runOnUiThread { Toast.makeText(app, string, Toast.LENGTH_SHORT).show() }
    }

    fun toastNetError(){
        toast("网络连接已断开,请检查网络设置")
    }

    fun toastAndFinish(string: String) {
        toast(string)
        app.finish()
    }
}