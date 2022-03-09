package cn.copaint.audience.utils

import android.widget.TextView

object TextUtils {
    fun boldText(text: TextView) {
        text.textSize = 28f
        text.paint.isFakeBoldText = true
    }

    fun normalText(text: TextView) {
        text.textSize = 24f
        text.paint.isFakeBoldText = false
    }
}

fun TextView.bold() {
    textSize = 28f
    paint.isFakeBoldText = true
}

fun TextView.normal() {
    textSize = 24f
    paint.isFakeBoldText = false
}