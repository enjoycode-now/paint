/*
 * Copyright (C) 2020 Wacom.
 * Use of this source code is governed by the MIT License that can be found in the LICENSE file.
 */
package cn.copaint.audience

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import cn.authing.core.graphql.GraphQLException
import cn.copaint.audience.adapter.LayerAdapter
import cn.copaint.audience.databinding.ActivityDrawBinding
import cn.copaint.audience.databinding.ItemToolsmenuBinding
import cn.copaint.audience.model.RoomLayer
import cn.copaint.audience.model.StepStack
import cn.copaint.audience.serialization.InkEnvironmentModel
import cn.copaint.audience.tools.raster.*
import cn.copaint.audience.utils.AuthingUtils.authenticationClient
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.GrpcUtils.paintStub
import cn.copaint.audience.utils.GrpcUtils.setPaintId
import cn.copaint.audience.utils.GrpcUtils.setToken
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.utils.distance
import cn.copaint.audience.utils.dp
import cn.copaint.audience.utils.toBitmap
import com.bugsnag.android.Bugsnag
import com.wacom.ink.format.InkModel
import com.wacom.ink.format.input.*
import com.wacom.ink.format.tree.groups.StrokeGroupNode
import com.wacom.ink.model.Identifier
import kotlinx.android.synthetic.main.activity_draw.*
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.item_layer_small.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import paint.v1.Paint.*
import top.defaults.colorpicker.ColorPickerPopup
import top.defaults.colorpicker.ColorPickerPopup.ColorPickerObserver
import java.io.IOException
import java.util.*
import kotlin.math.ceil

class DrawActivity : AppCompatActivity() {

    // -- Variables For serialisation
    private lateinit var mainGroup: StrokeGroupNode // This is a list of StrokeNode.

    // A StrokeNode contains information about an Stroke
    private lateinit var inkModel: InkModel // This is the main serializable class, what we save and

    // Environment information to save in the ink model
    private lateinit var inkEnvironmentModel: InkEnvironmentModel

    private lateinit var popupWindow: PopupWindow

    private var drawingColor: Int = Color.argb(255, 74, 74, 74)
    private var lastEvent: MotionEvent? = null
    var lineProtect = false
    val layerAdapter = LayerAdapter(this)
    private lateinit var binding: ActivityDrawBinding

    // 多图层
    var smallLayerList = mutableListOf<RoomLayer>()

    lateinit var popUpWindow: PopupWindow
    var layerPos = -1
    val stepStack = StepStack()
    lateinit var bufferDraw: Draw.Builder
    val sharedFlow = MutableSharedFlow<PaintMessage>(3, 12, BufferOverflow.DROP_OLDEST)
    var seq = 0
    val actionBuffer = ArrayDeque<Draw>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityDrawBinding.inflate(layoutInflater)
        setContentView(binding.root)
        resetInkModel()
        app = this

        inkEnvironmentModel = InkEnvironmentModel(this) // Initializes the environment data for serialization
        binding.rasterDrawingSurface.activity = this
        binding.rasterDrawingSurface.inkEnvironmentModel = inkEnvironmentModel
        val pair = inkEnvironmentModel.createSensorData(2)
        binding.rasterDrawingSurface.sensorData = pair.first
        binding.rasterDrawingSurface.channelList = pair.second

        changeBackground(R.drawable.btn_paper_04, R.drawable.background4)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        binding.layerRecycle.layoutManager = layoutManager
        binding.layerRecycle.adapter = layerAdapter

        CoroutineScope(Dispatchers.IO).launch {
            val sharedPref = app.getSharedPreferences("Authing", Context.MODE_PRIVATE) ?: return@launch
            authenticationClient.token = sharedPref.getString("token", "") ?: ""
            try {
                user = authenticationClient.getCurrentUser().execute()
                setToken(user.token ?: "")
                setPaintId("10338064278762102")
            } catch (e: GraphQLException) {
                runOnUiThread { startActivity(Intent(app, LoginActivity::class.java)) }
                return@launch
            } catch (e: IOException) {
                toast("用户信息获取失败")
            }
//            paintStub.history(
//                HistoryRequest.newBuilder()
//                    .setPaintingId(10338064278762102)
//                    .build()
//            ).collect {
//                for (history in it.historiesList) {
//                    actionBuffer.add(Draw.parseFrom(history.payload))
//                }
//            }
            paintStub.paint(sharedFlow.buffer(10, BufferOverflow.SUSPEND)).collect {
                when (it.type) {
                    PaintType.PAINT_TYPE_DRAW -> {
                        actionBuffer.add(Draw.parseFrom(it.payload))
                    }
                    PaintType.PAINT_TYPE_LAYER -> { }
                    else -> {}
                }
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            var draw: Draw?
            while (true) {
                delay(2000)
                draw = actionBuffer.poll()
                runOnUiThread {
                    while (draw != null) {
                        binding.rasterDrawingSurface.surfaceTouch(draw!!)
                        draw = actionBuffer.poll()
                    }
                }
            }
        }

        binding.rasterDrawingSurface.setOnTouchListener { _, event ->
            if (!smallLayerList[layerPos].isShow) return@setOnTouchListener true
            if (event.action == MotionEvent.ACTION_DOWN) lineProtect = false
            if (distance(event, lastEvent) > 65536) {
                lineProtect = true
                lastEvent!!.action = MotionEvent.ACTION_UP
                val draw = lastEvent!!.createDrawBuilder()
                lastEvent = null
                binding.rasterDrawingSurface.surfaceTouch(draw.build())
            }
            if (lineProtect) return@setOnTouchListener true

            if (
                (event.action == MotionEvent.ACTION_DOWN) ||
                (event.action == MotionEvent.ACTION_MOVE) ||
                (event.action == MotionEvent.ACTION_UP)
            ) {
                // 到达此处说明该点有效
                lastEvent = MotionEvent.obtain(event)
                val drawBuilder = lastEvent!!.createDrawBuilder()
                binding.rasterDrawingSurface.surfaceTouch(drawBuilder.build())
                CoroutineScope(Dispatchers.IO).launch {
                    val payload = drawBuilder.build()
                    val paintMessage = PaintMessage
                        .newBuilder()
                        .setType(PaintType.PAINT_TYPE_DRAW)
                        .setSequence(seq++)
                        .setPayload(payload.toByteString())
                        .build()
                    sharedFlow.tryEmit(paintMessage)
                }
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> bufferDraw = drawBuilder
                    MotionEvent.ACTION_MOVE -> bufferDraw.addDraw(drawBuilder.build())
                    MotionEvent.ACTION_UP -> {
                        lastEvent = null
                        smallLayerList[layerPos].bitmap = binding.rasterDrawingSurface.strokesLayer[layerPos].toBitmap(binding.rasterDrawingSurface.inkCanvas)
                        layerAdapter.notifyItemChanged(layerPos)
                    }
                }
            }
            true
        }
    }

    fun onSurfaceCreated() {
        setTool(btn_pencil, PencilTool(this))
    }

    fun MotionEvent.createDrawBuilder(): Draw.Builder {
        val drawBuilder = Draw.newBuilder()
            .setTool(2)
            .setColor(drawingColor)
            .setPhase(action)
            .setThickness(1f)

        for (i in 0 until historySize) {
            if (i % 3 != 0) continue
            drawBuilder.addPoints(
                Point.newBuilder()
                    .setX(getHistoricalX(i))
                    .setY(getHistoricalY(i))
                    .setPressure(getHistoricalPressure(i))
            )
        }

        drawBuilder.addPoints(
            Point.newBuilder()
                .setX(x)
                .setY(y)
                .setPressure(pressure)
        )
        return drawBuilder
    }

    fun Draw.Builder.addDraw(newDraw: Draw): Draw.Builder {
        addAllPoints(newDraw.pointsList)
        return this
    }

    fun add(view: View) {
        if (smallLayerList.size >= 0xF) return
        smallLayerList.add(RoomLayer())
        binding.rasterDrawingSurface.addLayer()
        binding.rasterDrawingSurface.refreshView()
        binding.rasterDrawingSurface.invalidate()
        smallLayerList.last().bitmap =
            binding.rasterDrawingSurface.strokesLayer[smallLayerList.lastIndex].toBitmap(binding.rasterDrawingSurface.inkCanvas)
        changeToLayer(smallLayerList.lastIndex)
        layerAdapter.notifyDataSetChanged()
    }

    fun undo(view: View) {
        val stepModel = stepStack.undo()
        if (stepModel == null) toast("无法继续撤回")
        else {
            binding.rasterDrawingSurface.setStepModel(stepModel)
            layerPos = stepModel.index
            smallLayerList[stepModel.index].bitmap =
                binding.rasterDrawingSurface.strokesLayer[stepModel.index].toBitmap(binding.rasterDrawingSurface.inkCanvas)
            layerAdapter.notifyDataSetChanged()
        }
    }

    fun redo(view: View) {
        val stepModel = stepStack.redo()
        if (stepModel == null) toast("无法继续重做")
        else {
            binding.rasterDrawingSurface.setStepModel(stepModel)
            layerPos = stepModel.index
            smallLayerList[stepModel.index].bitmap = binding.rasterDrawingSurface.strokesLayer[stepModel.index].toBitmap(binding.rasterDrawingSurface.inkCanvas)
            layerAdapter.notifyDataSetChanged()
        }
    }

    fun changeToLayer(pos: Int) {
        layerPos = pos
        stepStack.addStep(binding.rasterDrawingSurface.getStepModel())
        layerAdapter.notifyItemChanged(pos)
    }

    fun onTextureReady() {
        add(binding.addLayerButton)
        changeToLayer(0)
        smallLayerList[0].bitmap =
            binding.rasterDrawingSurface.strokesLayer[0].toBitmap(binding.rasterDrawingSurface.inkCanvas)
        layerAdapter.notifyItemChanged(0)
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
            .show(
                view,
                object : ColorPickerObserver() {
                    override fun onColorPicked(color: Int) {
                        this@DrawActivity.setColor(color)
                    }
                }
            )
    }

    fun setColor(color: Int) {
        drawingColor = color
        binding.btnColor.setColorFilter(drawingColor, PorterDuff.Mode.SRC_ATOP)
        binding.rasterDrawingSurface.setColor(drawingColor)
    }

    fun selectTool(view: View) {
        when (view.id) {
            R.id.btn_pencil -> setTool(view, PencilTool(this))
            R.id.btn_water_brush -> setTool(view, WaterbrushTool(this))
            R.id.btn_ink_brush -> setTool(view, InkBrushTool(this))
            R.id.btn_crayon -> setTool(view, CrayonTool(this))
            R.id.btn_eraser -> setTool(view, EraserRasterTool(this))
        }
    }

    fun setTool(view: View, tool: RasterTool) {
        binding.rasterDrawingSurface.setTool(tool)
        highlightTool(view)
    }

    fun highlightTool(view: View) {
        binding.btnPencil.isActivated = false
        binding.btnWaterBrush.isActivated = false
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
    }

    fun clear(view: View) {
        resetInkModel()
        binding.rasterDrawingSurface.clear()
        binding.rasterDrawingSurface.refreshView()
        binding.rasterDrawingSurface.invalidate()
        stepStack.addStep(binding.rasterDrawingSurface.getStepModel())
        smallLayerList[layerPos].bitmap =
            binding.rasterDrawingSurface.strokesLayer[layerPos].toBitmap(binding.rasterDrawingSurface.inkCanvas)
        layerAdapter.notifyItemChanged(layerPos)
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
        // which view you pass in doesn't matter, it is only used for the window token
        val screenPos = IntArray(2)
        view.getLocationOnScreen(screenPos)
        popupWindow.showAtLocation(
            view,
            Gravity.NO_GRAVITY,
            screenPos[0],
            screenPos[1] + binding.navbarContainer.height
        )
    }

    fun selectPaper(view: View) {
        when (view.id) {
            R.id.btnBackground1 -> changeBackground(R.drawable.btn_paper_01, R.drawable.background1)
            R.id.btnBackground2 -> changeBackground(R.drawable.btn_paper_02, R.drawable.background2)
            R.id.btnBackground3 -> changeBackground(R.drawable.btn_paper_03, R.drawable.btn_paper_03)
            R.id.btnBackground4 -> changeBackground(R.drawable.btn_paper_04, R.drawable.background4)
        }
    }

    fun layerToolPopupWindow(view: View) {
        val popBind = ItemToolsmenuBinding.inflate(LayoutInflater.from(this))
        val progress = smallLayerList[layerPos].alpha * 100 / 255f.toInt()
        popBind.alphaSeekbar.progress = progress
        popBind.alphaNum.text = "$progress%"

        // 弹出PopUpWindow
        popUpWindow = PopupWindow(popBind.root, 500.dp, 450.dp, true)
        popUpWindow.isOutsideTouchable = true

        // 设置弹窗时背景变暗
        var layoutParams = window.attributes
        layoutParams.alpha = 0.4f // 设置透明度
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams

        // 弹窗消失时背景恢复
        popUpWindow.setOnDismissListener {
            layoutParams = window.attributes
            layoutParams.alpha = 1f
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.attributes = layoutParams
        }

        popUpWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)

        popBind.delete.setOnClickListener {
            deleteLayer()
        }

        popBind.eraser.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog
                .setTitle("你是否要清空图层")
                .setCancelable(true)
                .setPositiveButton("确定") { dialog: DialogInterface, _: Int ->
                    this@DrawActivity.clear(popBind.eraser)
                    dialog.dismiss()
                    toast("图层清空")
                }
                .setNegativeButton("取消", null)
                .create()
                .show()
        }

        popBind.alphaSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                popBind.alphaNum.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // 向上取整函ceil()
                if (seekBar != null) smallLayerList[layerPos].alpha =
                    ceil(seekBar.progress * 2.55).toInt()
                layerAdapter.notifyItemChanged(layerPos)
            }
        })

        popBind.addAlphaBtn.setOnClickListener {
            popBind.alphaSeekbar.progress++
            popBind.alphaNum.text = "${popBind.alphaSeekbar.progress}%"
            smallLayerList[layerPos].alpha = ceil(popBind.alphaSeekbar.progress * 2.55).toInt()
            layerAdapter.notifyItemChanged(layerPos)
        }

        popBind.minusAlphaBtn.setOnClickListener {
            popBind.alphaSeekbar.progress--
            popBind.alphaNum.text = "${popBind.alphaSeekbar.progress}%"
            smallLayerList[layerPos].alpha = ceil(popBind.alphaSeekbar.progress * 2.55).toInt()
            layerAdapter.notifyItemChanged(layerPos)
        }
    }

    fun deleteLayer() {
        if (smallLayerList.lastIndex == 0) {
            toast("最后一个图层无法删除")
            return
        }
        smallLayerList.removeAt(layerPos)
        binding.rasterDrawingSurface.strokesLayer.removeAt(layerPos)
        binding.rasterDrawingSurface.currentFrameLayer.removeAt(layerPos)
        if (layerPos > 0) layerPos--
        binding.rasterDrawingSurface.refreshView()
        changeToLayer(layerPos)
    }

    fun changeBackground(background: Int, paper: Int) {
        binding.btnBackground.setImageResource(background)
        binding.drawingLayout.setBackgroundResource(paper)
        if (this::popupWindow.isInitialized) popupWindow.dismiss()
    }

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
