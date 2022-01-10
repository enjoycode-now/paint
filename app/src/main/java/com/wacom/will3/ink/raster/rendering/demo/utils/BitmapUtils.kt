package com.wacom.will3.ink.raster.rendering.demo.utils

import android.graphics.Bitmap
import android.view.View

object BitmapUtils {

    var penMode=true

    fun newBitmap(width:Int=1200,height:Int=1800) = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888)
}