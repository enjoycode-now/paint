package com.wacom.will3.ink.raster.rendering.demo.tools.raster

import android.content.Context
import com.wacom.ink.Calculator
import com.wacom.ink.PathPoint
import com.wacom.ink.PathPointLayout
import com.wacom.ink.format.rendering.RasterBrush
import com.wacom.will3.ink.raster.rendering.demo.brush.BrushPalette
import com.wacom.will3.ink.raster.rendering.demo.brush.URIBuilder
import com.wacom.will3.ink.raster.rendering.demo.computeValueBasedOnPressure
import kotlin.math.*

class PenTool(context: Context) : RasterTool(context) {


    private var previousSize = MIN_PENCIL_SIZE

    companion object {
        val uri = URIBuilder.getToolURI("raster", "pen")

        // Minimum size of the pencil tip
        val MIN_PENCIL_SIZE = 1f

        // Maximum size of the pencil tip
        val MAX_PENCIL_SIZE = 10.5f

        // Minimum alpha values for the particles
        var MIN_ALPHA = 0.05f

        // Maximum alpha values for the particles
        var MAX_ALPHA = 0.2f

        //  Unit for speed is px/second.
        //  NOTE: This needs to be optimized for different Pixel densities of different devices
        val MAX_SPEED = 2000f

        // Half PI value
        val PI_HALF = (PI / 2f).toFloat()

        // Minimum altitude angle seen in experiments
        val MIN_ALTITUDE_ANGLE = 0.4
    }

    override var brush = BrushPalette.pen(context)
    var previousAlpha = 0.2f

    override fun getLayout(): PathPointLayout {
        // Define different layouts for stylus and touch input
        if (isStylus) {
            return PathPointLayout(
                PathPoint.Property.X,
                PathPoint.Property.Y,
                PathPoint.Property.SIZE,
                PathPoint.Property.ALPHA,
                PathPoint.Property.OFFSET_X,
                PathPoint.Property.OFFSET_Y
            )
        } else {
            return PathPointLayout(
                PathPoint.Property.X,
                PathPoint.Property.Y,
                PathPoint.Property.SIZE,
                PathPoint.Property.ALPHA
            )
        }
    }

    override val touchCalculator: Calculator = { previous, current, next ->
        // Use the following to compute size based on speed:
        var size = current.computeValueBasedOnSpeed(
            previous,
            next,
            minValue = MIN_PENCIL_SIZE,
            maxValue = MAX_PENCIL_SIZE,
            minSpeed = 5f,
            maxSpeed = MAX_SPEED,
            // reverse behaviour
            remap = { 1f - it }
        )

        if (size == null) {
            size = previousSize
        } else {
            previousSize = size
        }

        var alpha = current.computeValueBasedOnSpeed(
            previous,
            next,
            minValue = MIN_ALPHA,
            maxValue = MAX_ALPHA,
            minSpeed = 5f,
            maxSpeed = MAX_SPEED,
            // reverse behaviour
            remap = { 1f - it }
        )

        if (alpha == null) {
            alpha = previousAlpha
        } else {
            previousAlpha = alpha
        }

        PathPoint(current.x, current.y, size = size, alpha = alpha)
    }

    override val stylusCalculator: Calculator = { previous, current, next ->
        // calculate the offset of the pencil tip due to tilted position
        val cosAltitudeAngle = cos(current.altitudeAngle!!)
        val sinAzimuthAngle = sin(current.azimuthAngle!!)
        val cosAzimuthAngle = cos(current.azimuthAngle!!)
        val x = sinAzimuthAngle * cosAltitudeAngle
        val y = cosAltitudeAngle * cosAzimuthAngle
        val offsetY = 5f * -x
        val offsetX = 5f * -y
        // compute the rotation
        val rotation = current.computeNearestAzimuthAngle(previous)
        // Normalize the tilt be minimum seen altitude angle and the maximum with the pen straight up
        val tiltScale = min(1f,
            ((PI_HALF - current.altitudeAngle!!) / (PI_HALF - MIN_ALTITUDE_ANGLE).toFloat()))
        // now, based on the tilt of the pencil the size of the brush size is increasing, as the
        // pencil tip is covering a larger area
        var size = if (current.force == -1f) {
            current.computeValueBasedOnSpeed(
                previous,
                next,
                minValue = 4f,
                maxValue = 12f,
                minSpeed = 50f,
                maxSpeed = 2000f,
                remap = { v: Float -> v.toDouble().pow(1.17).toFloat() }
            )

        } else {
            current.computeValueBasedOnPressure(
                minValue = 4f,
                maxValue = 12f,
                minPressure = 0.1f,
                maxPressure = 0.85f,
                remap = { v: Float -> v.toDouble().pow(1.17).toFloat() }
            )
        }
        if (size == null) {
            size = previousSize
        } else {
            previousSize = size
        }
        // Change the intensity of alpha value by pressure of speed, if available else use speed
        var alpha = if (current.force == -1f) {
            current.computeValueBasedOnSpeed(
                previous,
                next,
                minValue = MIN_ALPHA,
                maxValue = MAX_ALPHA,
                minSpeed = 5f,
                maxSpeed = MAX_SPEED,
                // reverse behaviour
                remap = { 1.0f - it }
            )
        } else {
            current.computeValueBasedOnPressure(
                minValue = MIN_ALPHA,
                maxValue = MAX_ALPHA,
                minPressure = 0.1f,
                maxPressure = 0.85f,
                remap = { v: Float -> v.toDouble().pow(1).toFloat() }
            )
        }

        if (alpha == null) {
            alpha = previousAlpha
        } else {
            previousAlpha = alpha
        }
        PathPoint(
            current.x, current.y,
            alpha = alpha, size = size, rotation = rotation,
            offsetX = offsetX, offsetY = offsetY
        )
    }

}