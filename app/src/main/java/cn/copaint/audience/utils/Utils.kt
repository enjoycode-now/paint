/*
 * Copyright (C) 2020 Wacom.
 * Use of this source code is governed by the MIT License that can be found in the LICENSE file.
 */
package cn.copaint.audience.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.MotionEvent
import cn.copaint.audience.tools.Tool
import cn.copaint.audience.tools.raster.*
import com.wacom.ink.Phase
import com.wacom.ink.PointerData
import com.wacom.ink.format.enums.InkInputType
import com.wacom.ink.format.rendering.RasterBrush
import com.wacom.ink.rasterization.InkCanvas
import com.wacom.ink.rasterization.Layer
import com.wacom.ink.rasterization.ParticleBrush
import com.wacom.ink.rasterization.RotationMode
import com.wacom.ink.rendering.BlendMode
import paint.v1.Paint.Draw
import kotlin.math.max
import kotlin.math.min

/**
 * Converts a motion event to PointerData.
 *
 * @return the PointerData for the event.
 */
fun Draw.toPointerDataList(alphaBias: Float): MutableList<PointerData> {
    val pointDataList = mutableListOf<PointerData>()
    for (point in pointsList) {
        pointDataList.add(
            PointerData(
                pointsList[0].x,
                pointsList[0].y,
                phase = Phase.UPDATE,
                timestamp = 0L,
                force = pointsList[0].pressure * alphaBias,
                altitudeAngle = 0f,
                azimuthAngle = 0f
            )
        )
    }
    if (phase == MotionEvent.ACTION_DOWN)pointDataList.first().phase = Phase.BEGIN
    if (phase == MotionEvent.ACTION_UP)pointDataList.last().phase = Phase.END
    return pointDataList
}

val Draw.ToolType: InkInputType
    get() = when (this.tool) {
        MotionEvent.TOOL_TYPE_STYLUS -> InkInputType.PEN
        MotionEvent.TOOL_TYPE_FINGER -> InkInputType.TOUCH
        else -> InkInputType.PEN
    }

fun com.wacom.ink.format.enums.RotationMode.convert(): RotationMode {
    return when (this) {
        com.wacom.ink.format.enums.RotationMode.NONE -> RotationMode.NONE
        com.wacom.ink.format.enums.RotationMode.RANDOM -> RotationMode.RANDOM
        com.wacom.ink.format.enums.RotationMode.TRAJECTORY -> RotationMode.TRAJECTORY
    }
}

fun RasterBrush.toParticleBrush(): ParticleBrush {
    val pb = ParticleBrush()
    pb.blendMode = blendMode
    pb.scattering = scattering
    pb.rotationMode = rotationMode.convert()

    val opts = BitmapFactory.Options()
    opts.inSampleSize = 1
    opts.inScaled = false

    var shapeTextures = Array<Bitmap>(shapeTextures.size) {
        val bytes = shapeTextures[it]
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }

    // OpenGl needs all the texture sizes to be defined on there are more than one size, however we can found brushes without all the texture
    // sizes, so we need to generate the rest
    if (shapeTextures.size > 1) {
        val shapeTextureList = mutableListOf<Bitmap>()
        val maxTexture = shapeTextures.maxByOrNull { it.width }
        var textureWidth = maxTexture!!.width
        while (textureWidth >= 1) {
            val bmp = shapeTextures.find { it.width == textureWidth }
            if (bmp != null) {
                shapeTextureList.add(bmp)
            } else {
                // we need to generate the texture and added it to the list
                // in order to do that we are going to escale the max texture
                shapeTextureList.add(Bitmap.createScaledBitmap(maxTexture, textureWidth, textureWidth, false))
            }
            textureWidth /= 2
        }
        shapeTextures = shapeTextureList.toTypedArray()
    }

    val fillTexture = BitmapFactory.decodeByteArray(fillTexture, 0, fillTexture.size, opts)

    pb.allocateTextures(shapeTextures, arrayOf(fillTexture), fillWidth.toInt(), fillHeight.toInt())

    return pb
}

val Tool.uri: String
    get() {
        when (this) {
            is CrayonTool -> return CrayonTool.uri
            is EraserRasterTool -> return EraserRasterTool.uri
            is PencilTool -> return PencilTool.uri
            is WaterbrushTool -> return WaterbrushTool.uri
        }
        return PencilTool.uri
    }

/**
 * A helper method that calculates a normalized value based on the pressure of the pointer.
 *
 * @param minValue Min result value.
 * @param maxValue Max result value.
 * @param minPressure Pressure is clamped to this value if speed is below the value.
 * @param maxPressure Pressure is clamped to this value if speed is above the value.
 * @param reverse Pressure will be reversed
 * @param remap A lambda that that defines a custom transformation on the normalized speed value.
 * @return
 */
fun PointerData.computeValueBasedOnPressure(
    minValue: Float,
    maxValue: Float,
    minPressure: Float = 100f,
    maxPressure: Float = 4000f,
    reverse: Boolean = false,
    remap: ((Float) -> (Float))
): Float {
    val normalizePressure = if (reverse) {
        minPressure + (1 - force!!) * (maxPressure - minPressure)
    } else {
        minPressure + force!! * (maxPressure - minPressure)
    }

    val pressureClamped = min(max(normalizePressure, minPressure), maxPressure)
    var k = (pressureClamped - minPressure) / (maxPressure - minPressure)

    k = remap(k)

    return minValue + k * (maxValue - minValue)
}

val BlendMode.uri: String
    get() = when (name) {
        "SOURCE_OVER" -> "will://rasterization/3.0/blend-mode/SourceOver"
        "DESTINATION_OVER" -> "will://rasterization/3.0/blend-mode/DestinationOver"
        "DESTINATION_IN" -> "will://rasterization/3.0/blend-mode/DestinationIn"
        "DESTINATION_OUT" -> "will://rasterization/3.0/blend-mode/DestinationOut"
        "LIGHTER" -> "will://rasterization/3.0/blend-mode/Lighter"
        "COPY" -> "will://rasterization/3.0/blend-mode/Copy"
        "MIN" -> "will://rasterization/3.0/blend-mode/Min"
        "MAX" -> "will://rasterization/3.0/blend-mode/Max"
        else -> "will://rasterization/3.0/blend-mode/SourceOver"
    }

fun Layer.toBitmap(inkCanvas: InkCanvas): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val tempLayer = inkCanvas.createLayer(width, height)
    inkCanvas.setTarget(tempLayer)
    inkCanvas.clearColor(Color.WHITE)
    inkCanvas.drawLayer(this, BlendMode.SOURCE_OVER)
    inkCanvas.readPixels(tempLayer, bitmap, 0, 0, 0, 0, bitmap.width, bitmap.height)
    return bitmap
}
