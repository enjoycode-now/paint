package cn.copaint.audience.tools

import com.wacom.ink.Calculator
import com.wacom.ink.PathPoint
import com.wacom.ink.PathPointLayout

open class Tool {
    var isStylus: Boolean = false

    open fun getLayout(): PathPointLayout {
        if (isStylus) {
            /**
             * The currently layout will use:
             * - Coordinate values (X and Y)
             * - Size - the size of the brush at any point of the stroke
             * - Rotation - the rotation of the brush
             */
            return PathPointLayout(
                PathPoint.Property.X,
                PathPoint.Property.Y,
                PathPoint.Property.SIZE,
                PathPoint.Property.ROTATION
            )
        } else {
            /**
             * The currently layout will use:
             * - Coordinate values (X and Y)
             * - Size - the size of the brush at any point of the stroke
             */
            return PathPointLayout(
                PathPoint.Property.X,
                PathPoint.Property.Y,
                PathPoint.Property.SIZE
            )
        }
    }

    open val touchCalculator: Calculator = { previous, current, next ->
        // Use the following to compute size based on speed:
        var size = current.computeValueBasedOnSpeed(previous, next, minValue = 0.5f, maxValue = 1.5f)
        if (size == null) size = 1.0f
        PathPoint(current.x, current.y, size = size)
    }

    open val stylusCalculator: Calculator = { previous, current, _ ->
        // Use the following to compute the size based on pressure:
        val size = 0.2f + current.force!! * (0.8f - 0.2f)
        val rotation = current.computeNearestAzimuthAngle(previous)
        PathPoint(current.x, current.y, size = size, rotation = rotation)
    }

    fun getCalculator() = if (isStylus) stylusCalculator else touchCalculator
}
