/*
 * Copyright (C) 2020 Wacom.
 * Use of this source code is governed by the MIT License that can be found in the LICENSE file.
 */
package com.wacom.will3.ink.raster.rendering.demo.raster

import android.content.Context
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.TextureView
import com.wacom.ink.InterpolatedSpline
import com.wacom.ink.Phase
import com.wacom.ink.StrokeConstants
import com.wacom.ink.egl.EGLRenderingContext
import com.wacom.ink.format.enums.InkInputType
import com.wacom.ink.format.enums.InkSensorType
import com.wacom.ink.format.input.SensorChannel
import com.wacom.ink.format.rendering.PathPointProperties
import com.wacom.ink.format.rendering.RasterBrush
import com.wacom.ink.format.rendering.Style
import com.wacom.ink.format.tree.data.SensorData
import com.wacom.ink.format.tree.data.Stroke
import com.wacom.ink.format.tree.nodes.StrokeNode
import com.wacom.ink.model.Identifier
import com.wacom.ink.rasterization.InkCanvas
import com.wacom.ink.rasterization.Layer
import com.wacom.ink.rasterization.StrokeRenderer
import com.wacom.ink.rendering.BlendMode
import com.wacom.will3.ink.raster.rendering.demo.*
import com.wacom.will3.ink.raster.rendering.demo.model.StepModel
import com.wacom.will3.ink.raster.rendering.demo.serialization.InkEnvironmentModel
import com.wacom.will3.ink.raster.rendering.demo.tools.raster.EraserRasterTool
import com.wacom.will3.ink.raster.rendering.demo.tools.raster.PencilTool
import com.wacom.will3.ink.raster.rendering.demo.tools.raster.RasterTool
import kotlin.math.min

/**
 * This is a surface for drawing raster inking.
 * Extends from SurfaceView
 */
class RasterView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextureView(context, attrs, defStyleAttr) {

    interface InkingSurfaceListener {
        fun onSurfaceCreated()
    }

    lateinit var inkCanvas: InkCanvas
    lateinit var currentFrameLayer: MutableList<Layer>  // 第一层：直接笔画层
    lateinit var strokesLayer: MutableList<Layer>       // 第二层：平滑笔画层
    lateinit var finalLayer: Layer                      // 第三层：合成层

    private lateinit var strokeRenderer: StrokeRenderer
    lateinit var activity: MainActivity

    private var rasterInkBuilder = RasterInkBuilder() //The ink builder
    var rasterTool: RasterTool = PencilTool(context)
    private var defaults: StrokeConstants = StrokeConstants() //The stroke defaults

    // for serialisation
    lateinit var inkEnvironmentModel: InkEnvironmentModel // information about the environment
    lateinit var sensorData: SensorData
    lateinit var channelList: List<SensorChannel>

    lateinit var listener: InkingSurfaceListener

    var strokeNodeList = mutableListOf<Pair<StrokeNode, RasterBrush>>()
    var sensorDataList = mutableListOf<SensorData>()

    var isStylus: Boolean = false
    var newTool = false

    var textureWidth=0
    var textureHeight=0

    init {
        rasterInkBuilder.updatePipeline(PencilTool(context))
        isOpaque = false

        // the drawing is going to be performer on background
        // on a surface, so we initialize the surface
        surfaceTextureListener = object: SurfaceTextureListener {

            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, w: Int, h: Int) {
                // here, once the surface is crated, we are going to initialize ink canvas

                // first we check that there is no other inkCanvas, in case there is we dispose it
                //if (!inkCanvas.isDisposed) releaseResources()
                textureWidth=w
                textureHeight=h
                inkCanvas = InkCanvas(surfaceTexture,EGLRenderingContext.EGLConfiguration(8, 8, 8, 8, 8, 8))

                strokesLayer = mutableListOf()
                currentFrameLayer = mutableListOf()
                finalLayer = inkCanvas.createViewLayer(w, h)
                activity.onTextureReady()
                inkCanvas.clearLayer(currentFrameLayer[activity.layerPos])
                strokeRenderer = StrokeRenderer(inkCanvas, PencilTool(context).brush.toParticleBrush(), w, h)
                drawStrokes(strokeNodeList)
                listener.onSurfaceCreated()
            }

            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, w: Int, h: Int) {
                // Ignored, Camera does all the work for us
            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                releaseResources();
                return true
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
                // Invoked every time there's a new Camera preview frame.
            }
        }
    }

    // This function is going to be call when we touch the surface
    fun surfaceTouch(event: MotionEvent) {
        if (event.resolveToolType() == InkInputType.PEN) {
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

        for (i in 0 until event.historySize) {
            val pointerData = event.historicalToPointerData(i,defaults.alpha)
            rasterInkBuilder.add(pointerData.phase, pointerData, null)
        }

        val pointerData = event.toPointerData()
        rasterInkBuilder.add(pointerData.phase, pointerData, null)

        val (added, predicted) = rasterInkBuilder.build()

        if (pointerData.phase == Phase.BEGIN) {
            // initialize the sensor data each time a new stroke begin
            val pair = inkEnvironmentModel.createSensorData(event)
            sensorData = pair.first
            channelList = pair.second
        }

        for (channel in channelList) {
            when (channel.typeURI) {
                InkSensorType.X -> sensorData.add(channel, pointerData.x)
                InkSensorType.Y -> sensorData.add(channel, pointerData.y)
                InkSensorType.TIMESTAMP -> sensorData.addTimestamp(channel,pointerData.timestamp)
                InkSensorType.PRESSURE -> sensorData.add(channel, pointerData.force!!)
                InkSensorType.ALTITUDE -> sensorData.add(channel, pointerData.altitudeAngle!!)
                InkSensorType.AZIMUTH -> sensorData.add(channel, pointerData.azimuthAngle!!)
            }
        }

        if ((pointerData.phase == Phase.END) && (rasterInkBuilder.splineProducer.allData != null)) {
            addStroke()
            sensorDataList.add(sensorData)
        }

        if (added != null) drawStroke(event, added, predicted)
        renderView()
    }

    fun setStepModel(stepModel:StepModel){
        with(stepModel){
            inkCanvas.setTarget(currentFrameLayer[index])
            inkCanvas.drawLayer(layer,BlendMode.COPY)
            inkCanvas.setTarget(strokesLayer[index])
            inkCanvas.drawLayer(layer,BlendMode.COPY)
        }
        refreshView()
        invalidate()
    }

    private fun addStroke() {
        // Adding the style
        val style = Style(
            rasterTool.brush.name,          // Brush URI
            particlesRandomSeed = 1,        // Particle random seed
            props = PathPointProperties(    // Coloring path properties
                red = defaults.red,
                green = defaults.green,
                blue = defaults.blue,
                alpha = defaults.alpha
            ),
            renderModeUri = rasterTool.getBlendMode().uri()
        )
        // Adding stroke to the Stroke Repository
        val path = Stroke(
            Identifier(),                    // Generated UUID
            rasterInkBuilder.splineProducer.allData!!.copy(),   // Spline
            style                                               // Style
        )

        // Adding a node to the Ink tree
        val node = StrokeNode(path)
        node.data.sensorDataID = sensorData.id
        node.data.sensorDataOffset = 0
        strokeNodeList.add(Pair(node, rasterTool.brush))
    }

    fun setTool(tool: RasterTool) {
        newTool = true
        rasterTool = tool
        strokeRenderer.strokeBrush = rasterTool.brush.toParticleBrush()
        rasterInkBuilder.updatePipeline(rasterTool)

        if (tool is EraserRasterTool) defaults.alpha = 0f

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
        for ((i,layer) in strokesLayer.withIndex()){
            if (!activity.smallLayerList[i].isShow)continue
            if (i==activity.layerPos) inkCanvas.drawLayer(currentFrameLayer[activity.layerPos], BlendMode.SOURCE_OVER)
            else inkCanvas.drawLayer(layer, BlendMode.SOURCE_OVER)
        }
        inkCanvas.invalidate()
    }

    fun refreshView() {
        inkCanvas.setTarget(finalLayer)
        inkCanvas.clearColor()
        // Copy the current frame layer in the view layer to present it on the screen.
        for ((i,layer) in strokesLayer.withIndex()){
            if (!activity.smallLayerList[i].isShow)continue
            inkCanvas.drawLayer(layer, BlendMode.SOURCE_OVER)
        }
        inkCanvas.invalidate()
    }

    // Draw stroke
    private fun drawStroke(event: MotionEvent, added: InterpolatedSpline, predicted: InterpolatedSpline?) {
        strokeRenderer.drawPoints(added, defaults, event.action == MotionEvent.ACTION_UP)

        if (predicted != null) strokeRenderer.drawPrelimPoints(predicted, defaults)

        if (event.action != MotionEvent.ACTION_UP) {
            inkCanvas.setTarget(currentFrameLayer[activity.layerPos], strokeRenderer.strokeUpdatedArea)
            inkCanvas.clearColor()
            inkCanvas.drawLayer(strokesLayer[activity.layerPos], BlendMode.SOURCE_OVER)
            strokeRenderer.blendStrokeUpdatedArea(currentFrameLayer[activity.layerPos], rasterTool.getBlendMode())
        } else {
            strokeRenderer.blendStroke(strokesLayer[activity.layerPos], rasterTool.getBlendMode())
            inkCanvas.setTarget(currentFrameLayer[activity.layerPos])
            inkCanvas.clearColor()
            inkCanvas.drawLayer(strokesLayer[activity.layerPos], BlendMode.SOURCE_OVER)
            val stepModel = StepModel(
                inkCanvas.createLayer(textureWidth,textureHeight),
                activity.layerPos
            )
            inkCanvas.setTarget(stepModel.layer)
            inkCanvas.drawLayer(strokesLayer[activity.layerPos], BlendMode.COPY)
            activity.stepStack.addStep(stepModel)
        }
    }

    fun getStepModel():StepModel {
        val stepModel = StepModel(
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
        val renderMode = BlendMode.values().find { it.uri() == renderModeUri } ?: BlendMode.SOURCE_OVER

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
        strokeNodeList.clear()
        sensorDataList.clear()
        inkCanvas.clearLayer(currentFrameLayer[activity.layerPos])
        inkCanvas.clearLayer(strokesLayer[activity.layerPos])
        inkCanvas.clearLayer(finalLayer)
        refreshView()
    }

    fun addLayer(){
        strokesLayer.add(inkCanvas.createLayer(textureWidth, textureHeight))
        currentFrameLayer.add(inkCanvas.createLayer(textureWidth, textureHeight))
    }

    fun scaleValues(stroke: StrokeNode, channelList: List<SensorChannel>, resolution: Double) {
        var resX = 0.0
        var resY = 0.0
        for (channel in channelList) {
            when (channel.typeURI) {
                InkSensorType.X -> resX = channel.resolution
                InkSensorType.Y -> resY = channel.resolution
            }
        }

        if ((resX > 0) && (resY > 0)) {
            val scaleFactor = min(resolution / resX, resolution / resY).toFloat()
            if (scaleFactor != 1f) stroke.data.spline.transform(scaleFactor, scaleFactor, scaleFactor, 0f, 0f, 0f, 0f, 0f)
        }
    }
}