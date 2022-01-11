package com.wacom.will3.ink.raster.rendering.demo.model

import android.graphics.Bitmap
import com.wacom.will3.ink.raster.rendering.demo.utils.BitmapUtils.newBitmap

import java.util.*

data class RoomLayer(
    var bitmap:Bitmap = newBitmap(),
    var alpha:Int=255,
    var isShow:Boolean = true,
    var isLock:Boolean = false
    ) // cell图层
