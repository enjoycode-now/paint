package cn.copaint.audience.utils

import android.graphics.Color
import android.view.View
import android.view.Window
import android.view.WindowManager

object StatusBarUtils {

    fun initSystemBar(window: Window, colorString :String, isLight: Boolean) {
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        //设置状态栏颜色
        if (isLight) {
            window.statusBarColor = Color.parseColor(colorString)
        } else {
            window.statusBarColor = Color.parseColor("#000000")
        }

        //状态栏颜色接近于白色，文字图标变成黑色
        val decor: View = window.getDecorView()
        var ui = decor.systemUiVisibility
        ui = if (isLight) {
            ui or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            ui and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        decor.systemUiVisibility = ui
    }
}