package cn.copaint.audience

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.adapter.LayerAdapter
import cn.copaint.audience.databinding.ActivityDrawBinding
import cn.copaint.audience.databinding.ItemToolsmenuBinding
import cn.copaint.audience.model.RoomLayer
import cn.copaint.audience.model.StepStack
import cn.copaint.audience.tools.raster.*
import cn.copaint.audience.utils.*
import cn.copaint.audience.utils.GrpcUtils.paintStub
import cn.copaint.audience.utils.GrpcUtils.setPaintId
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.bugsnag.android.Bugsnag
import com.wacom.ink.format.input.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import paint.v1.Paint.*
import top.defaults.colorpicker.ColorPickerPopup
import top.defaults.colorpicker.ColorPickerPopup.ColorPickerObserver
import java.util.*
import kotlin.math.ceil

@SuppressLint("NotifyDataSetChanged")
class DrawActivity : AppCompatActivity() {
    lateinit var layerDetailWindow: PopupWindow
    private var drawingColor: Int = Color.argb(255, 74, 74, 74)
    private var lastEvent: MotionEvent? = null
    var lineProtect = false
    val layerAdapter = LayerAdapter(this)
    lateinit var bind: ActivityDrawBinding

    // 多图层
    var smallLayers = mutableListOf<RoomLayer>()
    var layerPos = -1
    val stepStack = StepStack()
    lateinit var bufferDraw: Draw.Builder
    val sharedFlow = MutableSharedFlow<PaintMessage>(3, 12, BufferOverflow.DROP_OLDEST)
    var seq = -1
    val drawBuffer = ArrayDeque<Draw>()
    lateinit var job: Job

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        bind = ActivityDrawBinding.inflate(layoutInflater)
        setContentView(bind.root)
        app = this

        bind.layerRecycle.layoutManager = LinearLayoutManager(this).apply { reverseLayout = true }
        bind.layerRecycle.adapter = layerAdapter
        bind.rasterView.activity = this

        setPaintId("11115083807601270")
        job = collectLiveDraw()
        collectHistoryDraw()

        bind.rasterView.setOnTouchListener { _, event ->
            if (event.validate()) {
                lastEvent = MotionEvent.obtain(event)
                val drawBuilder = lastEvent!!.createDrawBuilder()
                bind.rasterView.surfaceTouch(drawBuilder.build(), event.action)
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> bufferDraw = drawBuilder
                    MotionEvent.ACTION_MOVE -> bufferDraw.addDraw(drawBuilder.build())
                    MotionEvent.ACTION_UP -> {
                        bufferDraw.addDraw(drawBuilder.build())
                        emitLiveDraw()
                        smallLayers[layerPos].bitmap =
                            bind.rasterView.strokesLayer[layerPos].toBitmap(bind.rasterView.inkCanvas)
                        layerAdapter.notifyDataSetChanged()
                    }
                }
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::job.isInitialized) job.cancel()
    }

    fun MotionEvent.validate(): Boolean {
        if (!smallLayers[layerPos].isShow) return false
        if (action > 2) return false
        if (action == MotionEvent.ACTION_DOWN) {
            lastEvent = null
            lineProtect = false
        }
        if (distance(this, lastEvent) > 65536) {
            lineProtect = true
            lastEvent!!.action = MotionEvent.ACTION_UP
            val draw = lastEvent!!.createDrawBuilder()
            lastEvent = null
            bind.rasterView.surfaceTouch(draw.build(), MotionEvent.ACTION_UP)
        }
        if (lineProtect) return false
        return true
    }

    fun collectHistoryDraw() = CoroutineScope(Dispatchers.IO).launch {
        paintStub.history(
            HistoryRequest.newBuilder()
                .setPaintingId(11115083807601270L)
                .build()
        ).onCompletion {
            drawBufferPoint(drawBuffer)
        }.collect {
            for (it in it.historiesList) {
                when (it.type) {
                    PaintType.PAINT_TYPE_DRAW -> {
                        val draw = Draw.parseFrom(it.payload)
                        runOnUiThread {
                            setColor(draw.color)
                            selectTool(draw.tool)
                            bind.rasterView.surfaceTouch(draw.Front, MotionEvent.ACTION_DOWN)
                            bind.rasterView.surfaceTouch(draw.Rear, MotionEvent.ACTION_UP)
                        }
                    }
                    PaintType.PAINT_TYPE_LAYER -> {
                        val layer = Layer.parseFrom(it.payload)
                        when (layer.action) {
                            LayerAction.LAYER_ACTION_HIDE -> hideLayer(layer.index)
                            LayerAction.LAYER_ACTION_LOCK -> { }
                            LayerAction.LAYER_ACTION_ALPHA -> { }
                            LayerAction.LAYER_ACTION_ADD -> runOnUiThread { add() }
                            LayerAction.LAYER_ACTION_DELETE -> runOnUiThread { deleteLayer(layer.index) }
                            LayerAction.LAYER_ACTION_CHANGE -> runOnUiThread { changeToLayer(layer.index) }
                            else -> { }
                        }
                    }
                    PaintType.PAINT_TYPE_UNDO -> undo()
                    PaintType.PAINT_TYPE_REDO -> redo()
                    PaintType.PAINT_TYPE_ACK_OK -> { }
                    PaintType.PAINT_TYPE_ACK_ERROR -> {
                        toast("服务器发生错误，正在退出...")
                        finish()
                    }
                    else -> { }
                }
            }
        }
    }

    fun emitLiveDraw() = CoroutineScope(Dispatchers.IO).launch {
        val payload = bufferDraw.build()
        val paintMessage = PaintMessage
            .newBuilder()
            .setType(PaintType.PAINT_TYPE_DRAW)
            .setSequence(seq++)
            .setPayload(payload.toByteString())
            .build()
        sharedFlow.tryEmit(paintMessage)
    }

    fun collectLiveDraw() = CoroutineScope(Dispatchers.IO).launch {
        paintStub.paint(sharedFlow.buffer(10, BufferOverflow.SUSPEND)).collect {
            when (it.type) {
                PaintType.PAINT_TYPE_DRAW -> drawBuffer.add(Draw.parseFrom(it.payload))
                PaintType.PAINT_TYPE_LAYER -> {
                    val layer = Layer.parseFrom(it.payload)
                    when (layer.action) {
                        LayerAction.LAYER_ACTION_HIDE -> hideLayer(layer.index)
                        LayerAction.LAYER_ACTION_LOCK -> { }
                        LayerAction.LAYER_ACTION_ALPHA -> { }
                        LayerAction.LAYER_ACTION_ADD -> add()
                        LayerAction.LAYER_ACTION_DELETE -> deleteLayer(layer.index)
                        LayerAction.LAYER_ACTION_CHANGE -> changeToLayer(layer.index)
                        else -> { }
                    }
                }
                PaintType.PAINT_TYPE_UNDO -> undo()
                PaintType.PAINT_TYPE_REDO -> redo()
                PaintType.PAINT_TYPE_ACK_OK -> { }
                PaintType.PAINT_TYPE_ACK_ERROR -> { }
                else -> { }
            }
        }
    }

    suspend fun drawBufferPoint(buffer: ArrayDeque<Draw>) {
        var draw: Draw?
        while (true) {
            delay(250)
            draw = buffer.poll()
            if (draw != null) runOnUiThread {
                while (draw != null) {
                    selectTool(draw!!.tool)
                    setColor(draw!!.color)
                    bind.rasterView.surfaceTouch(draw!!.Front, MotionEvent.ACTION_DOWN)
                    bind.rasterView.surfaceTouch(draw!!.Rear, MotionEvent.ACTION_UP)
                    draw = buffer.poll()
                }
                smallLayers[layerPos].bitmap = bind.rasterView.strokesLayer[layerPos].toBitmap(bind.rasterView.inkCanvas)
                layerAdapter.notifyDataSetChanged()
            }
        }
    }

    fun onSurfaceCreated() {
        selectTool(bind.btnPencil)
    }

    fun MotionEvent.createDrawBuilder(): Draw.Builder {
        val drawBuilder = Draw.newBuilder()
            .setTool(bind.rasterView.rasterTool.toolNumber)
            .setColor(drawingColor)
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

    fun hideLayer(pos: Int) {
        smallLayers[pos].isShow = !smallLayers[pos].isShow
        layerAdapter.notifyDataSetChanged()
        bind.rasterView.refreshView()
    }

    fun add(view: View) {
        if (smallLayers.size >= 0xF) return
        val payload = Layer.newBuilder()
            .setAction(LayerAction.LAYER_ACTION_ADD)
            .build()
        val paintMessage = PaintMessage.newBuilder()
            .setType(PaintType.PAINT_TYPE_LAYER)
            .setSequence(seq++)
            .setPayload(payload.toByteString())
            .build()
        sharedFlow.tryEmit(paintMessage)
        add()
    }

    fun add() {
        smallLayers.add(RoomLayer())
        bind.rasterView.addLayer()
        bind.rasterView.refreshView()
        bind.rasterView.invalidate()
        smallLayers.last().bitmap = bind.rasterView.toBitmap(smallLayers.lastIndex)
        changeToLayer(smallLayers.lastIndex)
        layerAdapter.notifyDataSetChanged()
    }

    // 响应界面撤回
    fun undo(view: View) {
        val paintMessage = PaintMessage.newBuilder()
            .setType(PaintType.PAINT_TYPE_UNDO)
            .setSequence(seq++)
            .build()
        sharedFlow.tryEmit(paintMessage)
        undo()
    }

    // 实际撤回
    fun undo() {
        val stepModel = stepStack.undo()
        if (stepModel == null) toast("无法继续撤回")
        else {
            bind.rasterView.setStepModel(stepModel)
            layerPos = stepModel.index
            smallLayers[stepModel.index].bitmap = bind.rasterView.toBitmap(stepModel.index)
            layerAdapter.notifyDataSetChanged()
        }
    }

    // 响应界面重做
    fun redo(view: View) {
        val paintMessage = PaintMessage.newBuilder()
            .setType(PaintType.PAINT_TYPE_REDO)
            .setSequence(seq++)
            .build()
        sharedFlow.tryEmit(paintMessage)
        redo()
    }

    // 实际重做
    fun redo() {
        val stepModel = stepStack.redo()
        if (stepModel == null) toast("无法继续重做")
        else {
            bind.rasterView.setStepModel(stepModel)
            layerPos = stepModel.index
            smallLayers[stepModel.index].bitmap = bind.rasterView.toBitmap(stepModel.index)
            layerAdapter.notifyDataSetChanged()
        }
    }

    fun changeToLayer(pos: Int) {
        if (pos == layerPos)return
        layerPos = pos
        stepStack.addStep(bind.rasterView.getStepModel())
        layerAdapter.notifyDataSetChanged()
    }

    fun onTextureReady() {
        add()
        changeToLayer(0)
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
            .show(object : ColorPickerObserver() {
                override fun onColorPicked(color: Int) {
                    this@DrawActivity.setColor(color)
                }
            })
    }

    fun setColor(color: Int) {
        drawingColor = color
        bind.btnColor.setColorFilter(drawingColor, PorterDuff.Mode.SRC_ATOP)
        bind.rasterView.setColor(drawingColor)
    }

    fun selectTool(view: View) {
        highlightTool(view)
        bind.rasterView.setTool(
            when (view.id) {
                R.id.btn_pencil -> 0
                R.id.btn_water_brush -> 1
                R.id.btn_ink_brush -> 2
                R.id.btn_crayon -> 3
                else -> 4
            }
        )
    }

    fun selectTool(tool: Int) {
        highlightTool(
            when (tool) {
                0 -> bind.btnPencil
                1 -> bind.btnWaterBrush
                2 -> bind.btnInkBrush
                3 -> bind.btnCrayon
                else -> bind.btnEraser
            }
        )
        bind.rasterView.setTool(tool)
    }

    fun highlightTool(view: View) {
        bind.btnPencil.isActivated = false
        bind.btnWaterBrush.isActivated = false
        bind.btnInkBrush.isActivated = false
        bind.btnCrayon.isActivated = false
        bind.btnEraser.isActivated = false
        view.isActivated = true
    }

    fun clear(view: View) {
        bind.rasterView.clear()
        bind.rasterView.refreshView()
        bind.rasterView.invalidate()
        stepStack.addStep(bind.rasterView.getStepModel())
        smallLayers[layerPos].bitmap =
            bind.rasterView.strokesLayer[layerPos].toBitmap(bind.rasterView.inkCanvas)
        layerAdapter.notifyDataSetChanged()
    }

    fun layerToolPopupWindow(view: View) {
        val popBind = ItemToolsmenuBinding.inflate(LayoutInflater.from(this))
        val progress = smallLayers[layerPos].alpha * 100 / 255f.toInt()
        popBind.alphaSeekbar.progress = progress
        popBind.alphaNum.text = "$progress%"

        // 弹出PopUpWindow
        layerDetailWindow = PopupWindow(popBind.root, 500.dp, 450.dp, true)
        layerDetailWindow.isOutsideTouchable = true

        // 设置弹窗时背景变暗
        var layoutParams = window.attributes
        layoutParams.alpha = 0.4f // 设置透明度
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams

        // 弹窗消失时背景恢复
        layerDetailWindow.setOnDismissListener {
            layoutParams = window.attributes
            layoutParams.alpha = 1f
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.attributes = layoutParams
        }

        layerDetailWindow.showAtLocation(bind.root, Gravity.CENTER, 0, 0)

        popBind.delete.setOnClickListener {
            if (smallLayers.lastIndex == 0) toast("最后一个图层无法删除")
            else {
                val payload = Layer.newBuilder()
                    .setAction(LayerAction.LAYER_ACTION_DELETE)
                    .setIndex(layerPos)
                    .build()
                val paintMessage = PaintMessage.newBuilder()
                    .setType(PaintType.PAINT_TYPE_LAYER)
                    .setSequence(seq++)
                    .setPayload(payload.toByteString())
                    .build()
                sharedFlow.tryEmit(paintMessage)
                deleteLayer(layerPos)
            }
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
                if (seekBar != null) smallLayers[layerPos].alpha =
                    ceil(seekBar.progress * 2.55).toInt()
                layerAdapter.notifyDataSetChanged()
            }
        })

        popBind.addAlphaBtn.setOnClickListener {
            popBind.alphaSeekbar.progress++
            popBind.alphaNum.text = "${popBind.alphaSeekbar.progress}%"
            smallLayers[layerPos].alpha = ceil(popBind.alphaSeekbar.progress * 2.55).toInt()
            layerAdapter.notifyDataSetChanged()
        }

        popBind.minusAlphaBtn.setOnClickListener {
            popBind.alphaSeekbar.progress--
            popBind.alphaNum.text = "${popBind.alphaSeekbar.progress}%"
            smallLayers[layerPos].alpha = ceil(popBind.alphaSeekbar.progress * 2.55).toInt()
            layerAdapter.notifyDataSetChanged()
        }
    }

    fun deleteLayer(layerIndex: Int) {
        changeToLayer(layerIndex)
        smallLayers.removeAt(layerPos)
        bind.rasterView.strokesLayer.removeAt(layerPos)
        bind.rasterView.currentFrameLayer.removeAt(layerPos)
        if (layerPos > 0) layerPos--
        bind.rasterView.refreshView()
        changeToLayer(layerPos)
    }

    fun hideLayerPop(view: View) {
        when (bind.layerCard.visibility) {
            View.VISIBLE -> {
                bind.layerCard.visibility = View.GONE
                bind.btnHideLayerPop.setImageResource(R.drawable.ic_expand_layer_card)
            }
            else -> {
                bind.layerCard.visibility = View.VISIBLE
                bind.btnHideLayerPop.setImageResource(R.drawable.ic_close_layer_card)
            }
        }
    }
}
