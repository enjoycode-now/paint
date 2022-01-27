package cn.copaint.audience.utils

import android.view.MotionEvent
import kotlin.math.abs

fun distance(event1:MotionEvent,event2:MotionEvent?):Long{
    return if (event2 == null) 0L else {
        val distanceX = abs(event1.x - event2.x).toLong()
        val distanceY = abs(event1.y - event2.y).toLong()
        distanceX * distanceX + distanceY * distanceY
    }
}