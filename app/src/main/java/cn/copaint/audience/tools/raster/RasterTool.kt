package cn.copaint.audience.tools.raster

import android.content.Context
import cn.copaint.audience.brush.BrushPalette
import cn.copaint.audience.tools.Tool
import com.wacom.ink.rendering.BlendMode

abstract class RasterTool(context: Context) : Tool() {

    open var brush = BrushPalette.pencil(context) // default brush

    abstract val toolNumber: Int

    open fun getBlendMode(): BlendMode {
        return BlendMode.SOURCE_OVER
    }
}
