/*
 * Copyright (C) 2020 Wacom.
 * Use of this source code is governed by the MIT License that can be found in the LICENSE file.
 */
package cn.copaint.audience.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.TextureView
import cn.copaint.audience.DrawActivity
import cn.copaint.audience.model.Step
import cn.copaint.audience.raster.RasterInkBuilder
import cn.copaint.audience.tools.raster.*
import cn.copaint.audience.utils.*
import com.wacom.ink.InterpolatedSpline
import com.wacom.ink.StrokeConstants
import com.wacom.ink.egl.EGLRenderingContext
import com.wacom.ink.format.enums.InkInputType
import com.wacom.ink.format.input.SensorChannel
import com.wacom.ink.format.rendering.RasterBrush
import com.wacom.ink.format.tree.data.SensorData
import com.wacom.ink.format.tree.nodes.StrokeNode
import com.wacom.ink.rasterization.InkCanvas
import com.wacom.ink.rasterization.Layer
import com.wacom.ink.rasterization.StrokeRenderer
import com.wacom.ink.rendering.BlendMode
import paint.v1.Paint.Draw

/**
 * This is a surface for drawing raster inking.
 * Extends from SurfaceView
 */
class RasterView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextureView(context, attrs, defStyleAttr) {

    lateinit var inkCanvas: InkCanvas
    lateinit var currentFrameLayer: MutableList<Layer> // 第一层：直接笔画层
    lateinit var strokesLayer: MutableList<Layer> // 第二层：平滑笔画层
    lateinit var finalLayer: Layer // 第三层：合成层

    private lateinit var strokeRenderer: StrokeRenderer
    lateinit var activity: DrawActivity

    private var rasterInkBuilder = RasterInkBuilder() // The ink builder
    var rasterTool: RasterTool = PencilTool(context)
    private var defaults: StrokeConstants = StrokeConstants() // The stroke defaults

    var strokeNodeList = mutableListOf<Pair<StrokeNode, RasterBrush>>()
    var sensorDataList = mutableListOf<SensorData>()

    var isStylus: Boolean = false
    var newTool = false

    var textureWidth = 0
    var textureHeight = 0

    init {
        rasterInkBuilder.updatePipeline(PencilTool(context))
        isOpaque = false

        // the drawing is going to be performer on background
        // on a surface, so we initialize the surface
        surfaceTextureListener = object : SurfaceTextureListener {

            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, w: Int, h: Int) {
                // here, once the surface is crated, we are going to initialize ink canvas

                // first we check that there is no other inkCanvas, in case there is we dispose it
                // if (!inkCanvas.isDisposed) releaseResources()
                textureWidth = w
                textureHeight = h
                inkCanvas = InkCanvas(surfaceTexture, EGLRenderingContext.EGLConfiguration(8, 8, 8, 8, 8, 8))

                strokesLayer = mutableListOf()
                currentFrameLayer = mutableListOf()
                finalLayer = inkCanvas.createViewLayer(w, h)
                activity.onTextureReady()
                inkCanvas.clearLayer(currentFrameLayer[activity.layerPos])
                strokeRenderer = StrokeRenderer(inkCanvas, PencilTool(context).brush.toParticleBrush(), w, h)
                drawStrokes(strokeNodeList)
                activity.onSurfaceCreated()
            }

            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, w: Int, h: Int) {
                // Ignored, Camera does all the work for us
            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                releaseResources()
                return true
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
                // Invoked every time there's a new Camera preview frame.
            }
        }
    }

    // This function is going to be call when we touch the surface
    fun surfaceTouch(draw: Draw, phase: Int) {
        if (draw.ToolType == InkInputType.PEN) {
            if ((newTool) || (!isStylus)) {
                newTool = false
                isStylus = true
                rasterInkBuilder.updateInputMethod(isStylus)
            }
        } else {
            if ((newTool) || (isStylus)) {
                newTool = false
                isStylus = false
                rasterInkBuilder.updateInputMethod(isStylus)
            }
        }

        val pointerDataList = draw.toPointerDataList(defaults.alpha, phase)
        for (data in pointerDataList) rasterInkBuilder.add(data, null)

        val (added, predicted) = rasterInkBuilder.build()

        if (added != null) drawStroke(phase, added, predicted)

        renderView()
    }

    fun setStepModel(stepModel: Step) {
        with(stepModel) {
            inkCanvas.setTarget(currentFrameLayer[index])
            inkCanvas.drawLayer(layer, BlendMode.COPY)
            inkCanvas.setTarget(strokesLayer[index])
            inkCanvas.drawLayer(layer, BlendMode.COPY)
        }
        refreshView()
        invalidate()
    }

    fun setTool(tool: Int) {
        newTool = true
        rasterTool = when (tool) {
            0 -> PencilTool(context)
            1 -> WaterbrushTool(context)
            2 -> InkBrushTool(context)
            3 -> CrayonTool(context)
            else -> EraserRasterTool(context)
        }
        strokeRenderer.strokeBrush = rasterTool.brush.toParticleBrush()
        rasterInkBuilder.updatePipeline(rasterTool)
    }

    fun setColor(color: Int) {
        defaults.red = Color.red(color) / 255f
        defaults.green = Color.green(color) / 255f
        defaults.blue = Color.blue(color) / 255f
        defaults.alpha = Color.alpha(color) / 255f
    }

    // Renders the canvas content on the screen.
    fun renderView() {
        inkCanvas.setTarget(finalLayer)
        inkCanvas.clearColor()
        // Copy the current frame layer in the view layer to present it on the screen.
        for ((i, layer) in strokesLayer.withIndex()) {
            if (!activity.smallLayers[i].isShow)continue
            if (i == activity.layerPos) inkCanvas.drawLayer(currentFrameLayer[activity.layerPos], BlendMode.SOURCE_OVER)
            else inkCanvas.drawLayer(layer, BlendMode.SOURCE_OVER)
        }
        inkCanvas.invalidate()
    }

    fun refreshView() {
        inkCanvas.setTarget(finalLayer)
        inkCanvas.clearColor()
        // Copy the current frame layer in the view layer to present it on the screen.
        for ((i, layer) in strokesLayer.withIndex()) {
            if (!activity.smallLayers[i].isShow)continue
            inkCanvas.drawLayer(layer, BlendMode.SOURCE_OVER)
        }
        inkCanvas.invalidate()
    }

    // Draw stroke
    private fun drawStroke(phase: Int, added: InterpolatedSpline, predicted: InterpolatedSpline?) {
        strokeRenderer.drawPoints(added, defaults, phase == MotionEvent.ACTION_UP)

        if (predicted != null) strokeRenderer.drawPrelimPoints(predicted, defaults)

        if (phase != MotionEvent.ACTION_UP) {
            inkCanvas.setTarget(currentFrameLayer[activity.layerPos], strokeRenderer.strokeUpdatedArea)
            inkCanvas.clearColor()
            inkCanvas.drawLayer(strokesLayer[activity.layerPos], BlendMode.SOURCE_OVER)
            strokeRenderer.blendStrokeUpdatedArea(currentFrameLayer[activity.layerPos], rasterTool.getBlendMode())
        } else {
            strokeRenderer.blendStroke(strokesLayer[activity.layerPos], rasterTool.getBlendMode())
            inkCanvas.setTarget(currentFrameLayer[activity.layerPos])
            inkCanvas.clearColor()
            inkCanvas.drawLayer(strokesLayer[activity.layerPos], BlendMode.SOURCE_OVER)
            val stepModel = Step(
                inkCanvas.createLayer(textureWidth, textureHeight),
                activity.layerPos
            )
            inkCanvas.setTarget(stepModel.layer)
            inkCanvas.drawLayer(strokesLayer[activity.layerPos], BlendMode.COPY)
            activity.stepStack.addStep(stepModel)
        }
    }

    fun getStepModel(): Step {
        val stepModel = Step(
            inkCanvas.createLayer(textureWidth, textureHeight),
            activity.layerPos
        )
        inkCanvas.setTarget(stepModel.layer)
        inkCanvas.drawLayer(strokesLayer[activity.layerPos], BlendMode.COPY)
        return stepModel
    }

    // Dispose the resources
    private fun releaseResources() {
        strokeRenderer.dispose()
        for (layer in strokesLayer)layer.dispose()
        for (layer in currentFrameLayer)layer.dispose()
        finalLayer.dispose()
        inkCanvas.dispose()
    }

    fun drawStrokes(strokeList: MutableList<Pair<StrokeNode, RasterBrush>>) {
        for (stroke in strokeList) drawStroke(stroke.first, stroke.second, null)
    }

    fun drawStroke(stroke: StrokeNode, brush: RasterBrush, sensorChannelList: List<SensorChannel>?) {
        val style = stroke.data.style
        val renderModeUri = style?.renderModeUri ?: ""
        val renderMode = BlendMode.values().find { it.uri == renderModeUri } ?: BlendMode.SOURCE_OVER

        defaults.red = style?.props?.red ?: 0f
        defaults.green = style?.props?.green ?: 0f
        defaults.blue = style?.props?.blue ?: 0f
        defaults.alpha = style?.props?.alpha ?: 1f

        val spline = stroke.data.spline
        val (added, _) = rasterInkBuilder.processSpline(spline, null)

        if (added != null) {
            strokeRenderer.strokeBrush = brush.toParticleBrush()
            strokeRenderer.drawPoints(added, defaults, true)
            strokeRenderer.blendStroke(strokesLayer[activity.layerPos], renderMode)
            inkCanvas.setTarget(currentFrameLayer[activity.layerPos])
            inkCanvas.clearColor()
            inkCanvas.drawLayer(strokesLayer[activity.layerPos], BlendMode.SOURCE_OVER)
        }
    }

    fun clear() {
        sensorDataList.clear()
        inkCanvas.clearLayer(currentFrameLayer[activity.layerPos])
        inkCanvas.clearLayer(strokesLayer[activity.layerPos])
        inkCanvas.clearLayer(finalLayer)
        refreshView()
    }

    fun addLayer() {
        strokesLayer.add(inkCanvas.createLayer(textureWidth, textureHeight))
        currentFrameLayer.add(inkCanvas.createLayer(textureWidth, textureHeight))
    }

    fun toBitmap(pos: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tempLayer = inkCanvas.createLayer(width, height)
        inkCanvas.setTarget(tempLayer)
        inkCanvas.clearColor(Color.WHITE)
        inkCanvas.drawLayer(strokesLayer[pos], BlendMode.SOURCE_OVER)
        inkCanvas.readPixels(tempLayer, bitmap, 0, 0, 0, 0, bitmap.width, bitmap.height)
        return bitmap
    }
}
