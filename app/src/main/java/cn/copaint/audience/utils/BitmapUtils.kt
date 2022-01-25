package cn.copaint.audience.utils

import android.graphics.Bitmap

object BitmapUtils {
    fun newBitmap(width:Int=1200,height:Int=1800) = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888)
}