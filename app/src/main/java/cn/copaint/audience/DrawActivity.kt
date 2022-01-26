/*
 * Copyright (C) 2020 Wacom.
 * Use of this source code is governed by the MIT License that can be found in the LICENSE file.
 */
package cn.copaint.audience

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.wacom.ink.format.InkModel
import com.wacom.ink.format.input.*
import com.wacom.ink.format.tree.groups.StrokeGroupNode
import com.wacom.ink.model.Identifier
import cn.copaint.audience.adapter.LayerAdapter
import cn.copaint.audience.databinding.ActivityDrawBinding
import cn.copaint.audience.databinding.ItemToolsmenuBinding
import cn.copaint.audience.model.RoomLayer
import cn.copaint.audience.model.StepStack
import cn.copaint.audience.views.RasterView
import cn.copaint.audience.serialization.InkEnvironmentModel
import cn.copaint.audience.tools.raster.*
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import kotlinx.android.synthetic.main.item_layer_small.view.*
import top.defaults.colorpicker.ColorPickerPopup
import top.defaults.colorpicker.ColorPickerPopup.ColorPickerObserver
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import android.view.WindowManager
import cn.copaint.audience.utils.dp
import kotlinx.android.synthetic.main.activity_draw.*
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.coroutines.*

class DrawActivity : AppCompatActivity(), RasterView.InkingSurfaceListener {

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
    private lateinit var binding: ActivityDrawBinding

    // 多图层
    var smallLayerList = mutableListOf<RoomLayer>()

    lateinit var popupwindow: PopupWindow
    var layerPos = -1
    val stepStack = StepStack()
    val drawQueue = ArrayDeque<Draw>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawBinding.inflate(layoutInflater)
        setContentView(binding.root)
        resetInkModel()
        app = this
        inkEnvironmentModel = InkEnvironmentModel(this) // Initializes the environment data for serialization

        setColor(drawingColor) //set default color

        binding.rasterDrawingSurface.activity = this

        binding.rasterDrawingSurface.setOnTouchListener { _, event ->
            if (!smallLayerList[layerPos].isShow) return@setOnTouchListener true
            if (event.action == MotionEvent.ACTION_DOWN) lineProtect = false
            val distanceX = abs(event.x - (lastEvent?.x ?: event.x))
            val distanceY = abs(event.y - (lastEvent?.y ?: event.y))
            val distance = distanceX * distanceX + distanceY * distanceY
            if (distance > 65536) {
                lineProtect = true
                lastEvent?.action = MotionEvent.ACTION_UP
                binding.rasterDrawingSurface.surfaceTouch(lastEvent!!)
                lastEvent = null
            }
            if (lineProtect) return@setOnTouchListener true

            if (
                (event.action == MotionEvent.ACTION_DOWN) ||
                (event.action == MotionEvent.ACTION_MOVE) ||
                (event.action == MotionEvent.ACTION_UP)
            ) {
                //到达此处说明该点有效
                lastEvent = MotionEvent.obtain(event)
                drawQueue.add(createDraw(event))
                binding.rasterDrawingSurface.surfaceTouch(lastEvent!!)
                if (event.action == MotionEvent.ACTION_UP) {
                    smallLayerList[layerPos].bitmap = binding.rasterDrawingSurface.strokesLayer[layerPos].toBitmap(binding.rasterDrawingSurface.inkCanvas)
                    layerAdapter.notifyDataSetChanged()
                    lastEvent = null
                }
            }
            true
        }

        CoroutineScope(Dispatchers.Default).launch {
            while (isActive){
                val draw = drawQueue.lastOrNull()
                if (draw!=null)rasterDrawingSurface.surfaceIO(draw)
                delay(200)
            }
        }

        binding.rasterDrawingSurface.inkEnvironmentModel = inkEnvironmentModel
        binding.rasterDrawingSurface.listener = this

        selectPaper(currentBackground)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        binding.layerRecycle.layoutManager = layoutManager
        binding.layerRecycle.adapter = layerAdapter
    }

    override fun onSurfaceCreated() {
        if (drawingTool != null) binding.rasterDrawingSurface.setTool(drawingTool!!)
        else selectTool(defaultDrawingTool) // set default tool
    }

    fun createDraw(event: MotionEvent):Draw{
        val lineBuilder = Line.newBuilder()
        for(i in 0 until event.historySize) {
            lineBuilder.addPoints(
                Point.newBuilder()
                    .setX(event.getHistoricalX(i))
                    .setY(event.getHistoricalY(i)+100)
                    .setPressure(event.getHistoricalPressure(i))
            )
        }
        lineBuilder.addPoints(
            Point.newBuilder()
                .setX(event.x)
                .setY(event.y+100)
                .setPressure(event.pressure)
        )
        return Draw.newBuilder()
            .setTool(event.getToolType(0))
            .setColor(drawingColor)
            .setPhase(event.action)
            .setThickness(1f)
            .setLine(lineBuilder.build())
            .build()
    }

    // 加一个图层
    fun add(view: View) {
        if (smallLayerList.size >= 0xF) return
        smallLayerList.add(RoomLayer())
        binding.rasterDrawingSurface.addLayer()
        binding.rasterDrawingSurface.refreshView()
        binding.rasterDrawingSurface.invalidate()
        smallLayerList.last().bitmap = binding.rasterDrawingSurface.strokesLayer[smallLayerList.lastIndex].toBitmap(binding.rasterDrawingSurface.inkCanvas)
        changeToLayer(smallLayerList.lastIndex)
        layerAdapter.notifyDataSetChanged()
    }

    fun undo(view:View){
        val stepModel = stepStack.undo()
        if (stepModel == null) toast("无法继续撤回")
        else {
            binding.rasterDrawingSurface.setStepModel(stepModel)
            layerPos = stepModel.index
            smallLayerList[stepModel.index].bitmap = binding.rasterDrawingSurface.strokesLayer[stepModel.index].toBitmap(binding.rasterDrawingSurface.inkCanvas)
            layerAdapter.notifyDataSetChanged()
        }
    }

    fun redo(view:View){
        val stepModel = stepStack.redo()
        if (stepModel == null) toast("无法继续重做")
        else {
            binding.rasterDrawingSurface.setStepModel(stepModel)
            layerPos = stepModel.index
            smallLayerList[stepModel.index].bitmap = binding.rasterDrawingSurface.strokesLayer[stepModel.index].toBitmap(binding.rasterDrawingSurface.inkCanvas)
            layerAdapter.notifyDataSetChanged()
        }
    }

    //   跳转到指定图层
    fun changeToLayer(pos: Int) {
        layerPos = pos
        stepStack.addStep(binding.rasterDrawingSurface.getStepModel())
        layerAdapter.notifyDataSetChanged()
    }

    fun onTextureReady() {
        add(binding.addLayerButton)
        changeToLayer(0)
        smallLayerList[0].bitmap = binding.rasterDrawingSurface.strokesLayer[0].toBitmap(binding.rasterDrawingSurface.inkCanvas)
        layerAdapter.notifyDataSetChanged()
    }

    fun selectColor(view: View) {
        ColorPickerPopup.Builder(this)
            .initialColor(drawingColor) // Set initial color
            .enableBrightness(true) // Enable brightness slider or not
            .enableAlpha(true) // Enable alpha slider or not
            .okTitle("Choose")
            .cancelTitle("Cancel")
            .showIndicator(true)
            .showValue(true)
            .build()
            .show(view, object : ColorPickerObserver() {
                override fun onColorPicked(color: Int) {
                    this@DrawActivity.setColor(color)
                }
            })
    }

    fun setColor(color: Int) {
        drawingColor = color
        binding.btnColor.setColorFilter(drawingColor, PorterDuff.Mode.SRC_ATOP)
        binding.rasterDrawingSurface.setColor(drawingColor)
    }

    fun selectTool(uri: String) {
        selectTool(when (uri) {
            WaterbrushTool.uri -> binding.btnWaterBrush
            CrayonTool.uri -> binding.btnCrayon
            EraserRasterTool.uri -> binding.btnEraser
            else -> binding.btnPencil
        })
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
        binding.rasterDrawingSurface.setTool(dt)
        highlightTool(view)
    }

    fun highlightTool(view: View) {
        binding.btnPencil.isActivated = false
        binding.btnInkBrush.isActivated = false
        binding.btnCrayon.isActivated = false
        binding.btnEraser.isActivated = false
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

    fun clear(view: View) {
        resetInkModel()
        binding.rasterDrawingSurface.clear()
        binding.rasterDrawingSurface.refreshView()
        binding.rasterDrawingSurface.invalidate()
        stepStack.addStep(binding.rasterDrawingSurface.getStepModel())
        smallLayerList[layerPos].bitmap = binding.rasterDrawingSurface.strokesLayer[layerPos].toBitmap(binding.rasterDrawingSurface.inkCanvas)
        layerAdapter.notifyDataSetChanged()
    }

    fun changeVisibilityOfSmallLayer() {
        resetInkModel()
        binding.rasterDrawingSurface.refreshView()
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
            screenPos[1] + binding.navbarContainer.height
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

    // 弹出工具框
    fun layerToolPopupWindow(view: View) {
        val popBind = ItemToolsmenuBinding.inflate(LayoutInflater.from(this))
        val progress = smallLayerList[layerPos].alpha * 100 / 255f.toInt()
        popBind.alphaSeekbar.progress = progress
        popBind.alphaNum.text = "${progress}%"

        // 弹出PopUpWindow
        popupwindow = PopupWindow(popBind.root, 500.dp, 450.dp, true)
        popupwindow.isOutsideTouchable = true

        // 设置弹窗时背景变暗
        var layoutParams = window.attributes
        layoutParams.alpha = 0.4f //设置透明度
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams

        // 弹窗消失时背景恢复
        popupwindow.setOnDismissListener {
            layoutParams = window.attributes
            layoutParams.alpha = 1f
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.attributes = layoutParams
        }

        popupwindow.showAtLocation(binding.root,Gravity.CENTER,0,0)

        popBind.delete.setOnClickListener {
            deleteLayer()
        }

        popBind.eraser.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog
                .setTitle("你是否要清空图层")
                .setCancelable(true)
                .setPositiveButton(
                    "确定",
                    DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                        this@DrawActivity.clear(popBind.eraser)
                        dialog.dismiss()
                        toast("图层清空")
                    })
                .setNegativeButton("取消", null)
                .create()
                .show()
        }

        popBind.alphaSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                popBind.alphaNum.text = "${progress}%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    // 向上取整函数ceil()
                    smallLayerList[layerPos].alpha = ceil(seekBar.progress*2.55).toInt()
                }
                layerAdapter.notifyDataSetChanged()
            }
        })

        popBind.addAlphaBtn.setOnClickListener{
            popBind.alphaSeekbar.progress++
            popBind.alphaNum.text = "${popBind.alphaSeekbar.progress}%"
            smallLayerList[layerPos].alpha = ceil(popBind.alphaSeekbar.progress*2.55).toInt()
            layerAdapter.notifyDataSetChanged()
        }


        popBind.minusAlphaBtn.setOnClickListener{
            popBind.alphaSeekbar.progress--
            popBind.alphaNum.text = "${popBind.alphaSeekbar.progress}%"
            smallLayerList[layerPos].alpha = ceil(popBind.alphaSeekbar.progress*2.55).toInt()
            layerAdapter.notifyDataSetChanged()
        }

    }

    // 删除当前图层
    fun deleteLayer() {
        if (smallLayerList.size == 1) {
            toast("默认图层禁止删除")
            return
        }
        smallLayerList.removeAt(layerPos)
        binding.rasterDrawingSurface.strokesLayer.removeAt(layerPos)
        binding.rasterDrawingSurface.currentFrameLayer.removeAt(layerPos)
        if (layerPos > 0) {
            layerPos--
        }
        binding.rasterDrawingSurface.refreshView()
        layerAdapter.notifyDataSetChanged()
    }

    fun changeBackground(background: Int, paper: Int) {
        binding.btnBackground.setImageResource(background)
        binding.drawingLayout.setBackgroundResource(paper)
        if (this::popupWindow.isInitialized) popupWindow.dismiss()
    }

    // expandAndCloseSmallLayer
    fun smallLayer(view: View) {
        when (binding.layerCard.visibility) {
            View.VISIBLE -> {
                binding.layerCard.visibility = View.GONE
                binding.btnSmallLayer.setImageResource(R.drawable.ic_expand_layer_card)
            }
            else -> {
                binding.layerCard.visibility = View.VISIBLE
                binding.btnSmallLayer.setImageResource(R.drawable.ic_close_layer_card)
            }
        }
    }
}