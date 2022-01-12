/*
 * Copyright (C) 2020 Wacom.
 * Use of this source code is governed by the MIT License that can be found in the LICENSE file.
 */
package com.wacom.will3.ink.raster.rendering.demo

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.wacom.ink.format.InkModel
import com.wacom.ink.format.input.*
import com.wacom.ink.format.tree.groups.StrokeGroupNode
import com.wacom.ink.model.Identifier
import com.wacom.will3.ink.raster.rendering.demo.adapter.LayerAdapter
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityMainBinding
import com.wacom.will3.ink.raster.rendering.demo.databinding.ItemToolsmenuBinding
import com.wacom.will3.ink.raster.rendering.demo.model.RoomLayer
import com.wacom.will3.ink.raster.rendering.demo.raster.RasterView
import com.wacom.will3.ink.raster.rendering.demo.serialization.InkEnvironmentModel
import com.wacom.will3.ink.raster.rendering.demo.tools.raster.*
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.app
import kotlinx.android.synthetic.main.activity_main.*
import top.defaults.colorpicker.ColorPickerPopup
import top.defaults.colorpicker.ColorPickerPopup.ColorPickerObserver
import java.util.*
import kotlin.math.abs

class MainActivity : AppCompatActivity(), RasterView.InkingSurfaceListener {

    //-- Variables For serialisation
    private lateinit var mainGroup: StrokeGroupNode // This is a list of StrokeNode.

    // A StrokeNode contains information about an Stroke
    private lateinit var inkModel: InkModel // This is the main serializable class, what we save and

    // Environment information to save in the ink model
    private lateinit var inkEnvironmentModel: InkEnvironmentModel

    //-- End serialisation
    private var defaultDrawingTool = PencilTool.uri
    private var drawingTool: RasterTool? = null

    private lateinit var popupWindow: PopupWindow

    private var drawingColor: Int = Color.argb(255, 74, 74, 74)
    private var lastEvent: MotionEvent? = null
    var lineProtect = false

    private var currentBackground = 3
    val layerAdapter = LayerAdapter(this)
    private lateinit var binding: ActivityMainBinding

    // 多图层
    var smallLayerList = mutableListOf(RoomLayer())

    lateinit var popupwindow: PopupWindow


    //   加一个图层
    fun add(view: View) {
        rasterDrawingSurface.addLayer()
        onTextureReady()
    }


    @SuppressLint("ClickableViewAccessibility")
    //   跳转到指定图层
    fun changeToLayer(position: Int) {
        rasterDrawingSurface.changeToLayer(position)
        rasterDrawingSurface.refreshView()
        rasterDrawingSurface.invalidate()
    }


    fun onTextureReady() {
        smallLayerList.clear()
        for (i in 0..rasterDrawingSurface.currentFrameLayer.lastIndex) {
            val roomLayer = RoomLayer()
            roomLayer.bitmap = rasterDrawingSurface.toBitmap(i)
            smallLayerList.add(roomLayer)
        }
        layerAdapter.notifyDataSetChanged()
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        resetInkModel()
        app = this
        inkEnvironmentModel =
            InkEnvironmentModel(this) // Initializes the environment data for serialization

        setColor(drawingColor) //set default color

        rasterDrawingSurface.mainActivity = this
        rasterDrawingSurface.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_DOWN) lineProtect = false

            val distanceX = abs(event.x - (lastEvent?.x ?: event.x))
            val distanceY = abs(event.y - (lastEvent?.y ?: event.y))
            val distance = distanceX * distanceX + distanceY * distanceY
            if (distance > 65536) {
                lineProtect = true
                lastEvent?.action = MotionEvent.ACTION_UP
                rasterDrawingSurface.surfaceTouch(lastEvent!!)
                lastEvent = null
            }
            if (lineProtect) return@setOnTouchListener true

            if (
                (event.action == MotionEvent.ACTION_DOWN) ||
                (event.action == MotionEvent.ACTION_MOVE) ||
                (event.action == MotionEvent.ACTION_UP)
            ) {
                lastEvent = MotionEvent.obtain(event)
                rasterDrawingSurface.surfaceTouch(lastEvent!!)
                if (event.action == MotionEvent.ACTION_UP) {
                    smallLayerList[rasterDrawingSurface.layerPos].bitmap = rasterDrawingSurface.toBitmap(rasterDrawingSurface.layerPos)
                    layerAdapter.notifyDataSetChanged()
                    lastEvent = null
                }
            }
            true
        }

        rasterDrawingSurface.inkEnvironmentModel = inkEnvironmentModel
        rasterDrawingSurface.listener = this

        selectPaper(currentBackground)

        binding.layerRecycle.layoutManager = LinearLayoutManager(this)
        binding.layerRecycle.adapter = layerAdapter
    }

    fun selectColor(view: View) {
        ColorPickerPopup.Builder(this)
            .initialColor(drawingColor) // Set initial color
            .enableBrightness(true) // Enable brightness slider or not
            .enableAlpha(true) // Enable alpha slider or not
            .okTitle("Choose")
            .cancelTitle("Cancel")
            .showIndicator(false)
            .showValue(false)
            .build()
            .show(view, object : ColorPickerObserver() {
                override fun onColorPicked(color: Int) {
                    this@MainActivity.setColor(color)
                }

                fun onColor(color: Int, fromUser: Boolean) {}
            })
    }

    fun setColor(color: Int) {
        drawingColor = color
        btnColor.setColorFilter(drawingColor, PorterDuff.Mode.SRC_ATOP)
        rasterDrawingSurface.setColor(drawingColor)
    }

    fun selectTool(uri: String) {
        val view = when (uri) {
            WaterbrushTool.uri -> btn_water_brush
            CrayonTool.uri -> btn_crayon
            EraserRasterTool.uri -> btn_eraser
            else -> btn_pencil
        }
        selectTool(view)
    }

    fun selectTool(view: View) {
        when (view.id) {
            R.id.btn_pencil -> setTool(view, PencilTool(this))
            R.id.btn_water_brush -> setTool(view, WaterbrushTool(this))
            R.id.btn_crayon -> setTool(view, CrayonTool(this))
            R.id.btn_eraser -> setTool(view, EraserRasterTool(this))
        }
    }

    fun setTool(view: View, tool: RasterTool) {
        drawingTool = tool
        val dt = drawingTool as RasterTool
        rasterDrawingSurface.setTool(dt)
        highlightTool(view)
    }

    fun highlightTool(view: View) {
        btn_pencil.isActivated = false
        btn_water_brush.isActivated = false
        btn_ink_brush.isActivated = false
        btn_crayon.isActivated = false
        btn_eraser.isActivated = false
        view.isActivated = true
    }

    private fun resetInkModel() {
        inkModel = InkModel()
        val root = StrokeGroupNode(Identifier())
        inkModel.inkTree.root = root
        mainGroup = StrokeGroupNode(Identifier())
        root.add(mainGroup)
        //TODO inkModel.brushRepository.addVectorBrush(brush)
        //TODO splines.clear()
        //TODO paths.clear()
    }

    override fun onSurfaceCreated() {
        if (drawingTool != null) rasterDrawingSurface.setTool(drawingTool!!)
        else selectTool(defaultDrawingTool) // set default tool
    }

    fun clear(view: View) {
        resetInkModel()
        rasterDrawingSurface.clear()
        rasterDrawingSurface.refreshView()
        smallLayerList[rasterDrawingSurface.layerPos].bitmap = rasterDrawingSurface.toBitmap()
        layerAdapter.notifyDataSetChanged()
    }

    fun openPaperDialog(view: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.background_selector, null)

        // create the popup window
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true // lets taps outside the popup also dismiss it
        popupWindow = PopupWindow(popupView, width, height, focusable)

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        val screenPos = IntArray(2)
        view.getLocationOnScreen(screenPos)
        popupWindow.showAtLocation(
            view,
            Gravity.NO_GRAVITY,
            screenPos[0],
            screenPos[1] + navbar_container.height
        )
    }

    fun selectPaper(pos: Int) {
        when (pos) {
            0 -> changeBackground(R.drawable.btn_paper_01, R.drawable.background1)
            1 -> changeBackground(R.drawable.btn_paper_02, R.drawable.background2)
            2 -> changeBackground(R.drawable.btn_paper_03, R.drawable.btn_paper_03)
            else -> changeBackground(R.drawable.btn_paper_04, R.drawable.btn_paper_04)
        }
    }

    fun selectPaper(view: View) {
        when (view.id) {
            R.id.btnBackground1 -> {
                changeBackground(R.drawable.btn_paper_01, R.drawable.background1)
                currentBackground = 0
            }
            R.id.btnBackground2 -> {
                changeBackground(R.drawable.btn_paper_02, R.drawable.background2)
                currentBackground = 1
            }
            R.id.btnBackground3 -> {
                changeBackground(R.drawable.btn_paper_03, R.drawable.btn_paper_03)
                currentBackground = 2
            }
            R.id.btnBackground4 -> {
                changeBackground(R.drawable.btn_paper_04, R.drawable.background4)
                currentBackground = 3
            }
        }
    }

    fun Int.dp(): Int {
        return this * Resources.getSystem().displayMetrics.density.toInt()
    }

    // 弹出工具框
    fun layerToolPopupWindow(view: View) {
        val popBind = ItemToolsmenuBinding.inflate(LayoutInflater.from(this))
        // 弹出PopUpWindow
        popupwindow = PopupWindow(popBind.root,288.dp(),128.dp(),true)
        popupwindow.isOutsideTouchable=true
        popupwindow.showAsDropDown(view, (-352).dp(), (-160).dp())
    }

    // 本地删除图层
    fun deleteLayer(targetLayer: Int) {

    }

    fun changeBackground(background: Int, paper: Int) {
        binding.btnBackground.setImageResource(background)
        binding.drawingLayout.setBackgroundResource(paper)
        if (this::popupWindow.isInitialized) popupWindow.dismiss()
    }

    fun smallLayer(view: android.view.View) {

        binding.btnSmallLayer.setOnClickListener{
            when(binding.layerCard.visibility){
                View.VISIBLE->{
                    binding.layerCard.visibility = View.GONE
                    binding.btnSmallLayer.setImageResource(R.drawable.ic_expand_layer_card)
                }
                View.GONE->{
                    binding.layerCard.visibility = View.VISIBLE
                    binding.btnSmallLayer.setImageResource(R.drawable.ic_close_layer_card)
                }
            }
        }
    }
}
