package cn.copaint.audience.tools.raster

import android.content.Context
import cn.copaint.audience.brush.BrushPalette
import cn.copaint.audience.brush.URIBuilder
import cn.copaint.audience.utils.computeValueBasedOnPressure
import com.wacom.ink.Calculator
import com.wacom.ink.PathPoint
import com.wacom.ink.PathPointLayout
import com.wacom.ink.rendering.BlendMode
import kotlin.math.pow

class EraserRasterTool(context: Context) : RasterTool(context) {
    override val toolNumber = 4

    companion object {
        val uri = URIBuilder.getToolURI("raster", "eraser")
    }

    override var brush = BrushPalette.eraser(context)

    override fun getLayout(): PathPointLayout {
        return PathPointLayout(
            PathPoint.Property.X,
            PathPoint.Property.Y,
            PathPoint.Property.SIZE,
            PathPoint.Property.RED,
            PathPoint.Property.GREEN,
            PathPoint.Property.BLUE,
            PathPoint.Property.ALPHA
        )
    }

    override val touchCalculator: Calculator = { previous, current, next ->
        // Use the following to compute size based on speed:
        var size = current.computeValueBasedOnSpeed(
            previous,
            next,
            minValue = 32f,
            maxValue = 128f,
            minSpeed = 720f,
            maxSpeed = 3900f
        )
        if (size == null) size = 32f

        PathPoint(current.x, current.y, size = size, red = 1f, green = 1f, blue = 1f, alpha = 1f)
    }

    override val stylusCalculator: Calculator = { previous, current, next ->
        // Use the following to compute size based on speed:
        val size = current.computeValueBasedOnPressure(
            minValue = 30f,
            maxValue = 80f,
            minPressure = 0.0f,
            maxPressure = 1.0f,
            remap = { v: Float -> v.toDouble().pow(1.17).toFloat() }
        )

        PathPoint(current.x, current.y, size = size, red = 1f, green = 1f, blue = 1f, alpha = 1f)
    }

    override fun getBlendMode(): BlendMode {
        return BlendMode.DESTINATION_OUT
    }
}
