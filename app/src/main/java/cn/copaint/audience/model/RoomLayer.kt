package cn.copaint.audience.model

import android.graphics.Bitmap
import cn.copaint.audience.utils.BitmapUtils.newBitmap

import java.util.*

data class RoomLayer(
    var bitmap:Bitmap = newBitmap(),
    var alpha:Int=255,
    var isShow:Boolean = true,
    var isLock:Boolean = false
    ) // cell图层
