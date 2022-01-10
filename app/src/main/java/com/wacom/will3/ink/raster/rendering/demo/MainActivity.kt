/*
 * Copyright (C) 2020 Wacom.
 * Use of this source code is governed by the MIT License that can be found in the LICENSE file.
 */
package com.wacom.will3.ink.raster.rendering.demo

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.wacom.ink.format.InkModel
import com.wacom.ink.format.input.*
import com.wacom.ink.format.tree.groups.StrokeGroupNode
import com.wacom.ink.model.Identifier
import com.wacom.will3.ink.raster.rendering.demo.adapter.LayerAdapter
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityMainBinding
import com.wacom.will3.ink.raster.rendering.demo.model.RoomLayer
import com.wacom.will3.ink.raster.rendering.demo.raster.RasterView
import com.wacom.will3.ink.raster.rendering.demo.serialization.InkEnvironmentModel
import com.wacom.will3.ink.raster.rendering.demo.tools.raster.*
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.toast
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
    var lineProtect=false

    private var currentBackground = 3

    private lateinit var binding : ActivityMainBinding



//    多图层
    var layerListRecyclerCache = mutableListOf<RoomLayer>()

    lateinit var popupwindow: PopupWindow


    //   上一个图层
    fun add(view:View){
        rasterDrawingSurface.nextLayer()

    }

    //   下一个图层
    fun minus(view:View){
        rasterDrawingSurface.lastLayer()

    }

    @SuppressLint("ClickableViewAccessibility")
    //   跳转到指定图层
    fun changeToLayer(position : Int){
        rasterDrawingSurface.changeToLayer(position)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        resetInkModel()



        inkEnvironmentModel = InkEnvironmentModel(this) // Initializes the environment data for serialization

        setColor(drawingColor) //set default color

        rasterDrawingSurface.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_DOWN)lineProtect = false

            val distanceX = abs(event.x-(lastEvent?.x?:event.x))
            val distanceY = abs(event.y-(lastEvent?.y?:event.y))
            val distance = distanceX*distanceX+distanceY*distanceY
            if (distance>65536){
                lineProtect = true
                lastEvent?.action=MotionEvent.ACTION_UP
                rasterDrawingSurface.surfaceTouch(lastEvent!!)
                lastEvent=null
            }
            if (lineProtect) return@setOnTouchListener true

            if (
                (event.action == MotionEvent.ACTION_DOWN) ||
                (event.action == MotionEvent.ACTION_MOVE) ||
                (event.action == MotionEvent.ACTION_UP)
            ) {
                lastEvent = MotionEvent.obtain(event)
                rasterDrawingSurface.surfaceTouch(lastEvent!!)
                if (event.action == MotionEvent.ACTION_UP) lastEvent = null
            }
            true
        }

        rasterDrawingSurface.inkEnvironmentModel = inkEnvironmentModel
        rasterDrawingSurface.listener = this

        selectPaper(currentBackground)

        //设置adpater
        val layoutManager = LinearLayoutManager(this)
        binding.layerRecycle.layoutManager = layoutManager
        val adapter = LayerAdapter(this)
        binding.layerRecycle.adapter = adapter
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
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY,screenPos[0],screenPos[1]+navbar_container.height)
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

    // 弹出工具框
    fun layerToolPopupWindow(view: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.item_toolsmenu, null)
        popupwindow = PopupWindow(popupView, 450, 150, true)
        popupwindow.showAsDropDown(view, -400, -200)
        val popupDelete: ImageView = popupView.findViewById(R.id.delete)
        if (rasterDrawingSurface.layerPos > 1) {
            popupDelete.setOnClickListener {
                runOnUiThread {
                    AlertDialog.Builder(this)
                        .setTitle("确定要删除该图层吗？")
                        .setMessage("")
                        .setCancelable(true)
                        .setPositiveButton("确定") { _: DialogInterface?, _: Int ->
                            deleteLayer(rasterDrawingSurface.layerPos)
                            toast("删除成功")
                        }
                        .setNegativeButton("取消") { _: DialogInterface?, _: Int -> }
                        .create()
                        .show()
                }
                popupwindow.dismiss()
            }
        } else {
            popupDelete.visibility = View.GONE
        }
        val popupFlower: ImageView = popupView.findViewById(R.id.flower)
        val popupEraser: ImageView = popupView.findViewById(R.id.eraser)
        popupwindow.isOutsideTouchable = true
        popupwindow.isFocusable = false
        if (rasterDrawingSurface.layerPos > 0) {
            popupFlower.setOnClickListener {
                toast("flower")
                popupwindow.dismiss()
            }
            popupEraser.setOnClickListener {
                runOnUiThread {
                    AlertDialog.Builder(this)
                        .setTitle("确定要清空该图层吗？")
                        .setMessage("")
                        .setCancelable(true)
                        .setPositiveButton("确定") { _: DialogInterface?, _: Int ->

                            binding.rasterDrawingSurface.clear()
                            toast("清空图层")
                        }
                        .setNegativeButton("取消") { _: DialogInterface?, _: Int -> }
                        .create()
                        .show()
                }
                popupwindow.dismiss()
            }
        }
    }

    // 本地删除图层
    fun deleteLayer(targetLayer: Int) {

    }

    fun changeBackground(background: Int, paper: Int) {
        btnBackground.setImageResource(background)
        drawingLayout.setBackgroundResource(paper)
        if(this::popupWindow.isInitialized)popupWindow.dismiss()
    }
}
