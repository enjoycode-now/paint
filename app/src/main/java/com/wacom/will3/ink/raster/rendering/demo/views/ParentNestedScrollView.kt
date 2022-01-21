package com.wacom.will3.ink.raster.rendering.demo.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.widget.NestedScrollView

class ParentNestedScrollView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : NestedScrollView(context, attrs, defStyleAttr) {
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if(canScrollVertically(dy) && dy>0){
            scrollTo(0,scrollY+dy)
            consumed[1]=dy
        }
    }
}