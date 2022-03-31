package cn.copaint.audience.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display

import android.view.WindowManager




object ScreenUtils {
    /**
     * 获取屏幕的宽高 单位px
     * x = width
     * y = height
     * @param context Context
     * @return Point
     */
    fun getScreenPoint(context: Context): Point {
        val screenPoint = Point()
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        screenPoint.x = displayMetrics.widthPixels
        screenPoint.y = displayMetrics.heightPixels
        return screenPoint
    }

    /**
     * 获取屏幕宽
     *
     * @param context 上下文
     * @return int ，单位px
     */
    fun getWidth(context: Context): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return displayMetrics.widthPixels
    }

    /**
     * 获取屏幕高
     *
     * @param context 上下文
     * @return int ，单位px
     */
    fun getHeight(context: Context): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return displayMetrics.heightPixels
    }

    /**
     * 获取屏幕的真实高度，包含导航栏、状态栏
     * @param context 上下文
     * @return int,单位px
     */
    fun getRealHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = windowManager.defaultDisplay
        val dm = DisplayMetrics()
        display.getRealMetrics(dm)
        return dm.heightPixels
    }

    /**
     * 获取屏幕的真实高度，包含导航栏、状态栏
     * @param context 上下文
     * @return int,单位px
     */
    fun getRealWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = windowManager.defaultDisplay
        val dm = DisplayMetrics()
        display.getRealMetrics(dm)
        return dm.widthPixels
    }

    /**
     * 是否在屏幕右侧
     *
     * @param mContext 上下文
     * @param xPos     位置的x坐标值
     * @return true：是。
     */
    fun isInRight(mContext: Context, xPos: Int): Boolean {
        return xPos > getWidth(mContext) / 2
    }

    /**
     * 是否在屏幕左侧
     *
     * @param mContext 上下文
     * @param xPos     位置的x坐标值
     * @return true：是。
     */
    fun isInLeft(mContext: Context, xPos: Int): Boolean {
        return xPos < getWidth(mContext) / 2
    }

}