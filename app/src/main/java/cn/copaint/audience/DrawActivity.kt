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
import cn.copaint.audience.utils.AuthingUtils.authenticationClient
import cn.copaint.audience.utils.AuthingUtils.update
import cn.copaint.audience.utils.GrpcUtils.paintStub
import cn.copaint.audience.utils.GrpcUtils.setPaintId
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.bugsnag.android.Bugsnag
import com.wacom.ink.format.input.*
import kotlinx.android.synthetic.main.activity_draw.*
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.item_layer_small.view.*
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

        CoroutineScope(Dispatchers.IO).launch {
            if (!authenticationClient.update()) {
                runOnUiThread {
                    startActivity(Intent(app, LoginActivity::class.java))
                    finish()
                }
            } else {
                setPaintId("10763227503800950")
                job = collectLiveDraw()
                collectHistoryDraw()
            }
        }

        bind.rasterView.setOnTouchListener { _, event ->
            if (event.validate()) {
                lastEvent = MotionEvent.obtain(event)
                val drawBuilder = lastEvent!!.createDrawBuilder()
                bind.rasterView.surfaceTouch(drawBuilder.build())
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
        if(this::job.isInitialized) job.cancel()
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
            bind.rasterView.surfaceTouch(draw.build())
        }
        if (lineProtect) return false
        return true
    }

    fun collectHistoryDraw() = CoroutineScope(Dispatchers.IO).launch {
        paintStub.history(
            HistoryRequest.newBuilder()
                .setPaintingId(10763227503800950L)
                .build()
        ).onCompletion {
            drawBufferPoint(drawBuffer)
        }.collect {
            for (history in it.historiesList) {
                val draw = Draw.parseFrom(history.payload)
                runOnUiThread {
                    selectTool(draw.tool)
                    bind.rasterView.surfaceTouch(draw.Front)
                    bind.rasterView.surfaceTouch(draw.Rear)
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
                PaintType.PAINT_TYPE_LAYER -> { }
                PaintType.PAINT_TYPE_ACK_OK -> { }
                PaintType.PAINT_TYPE_ACK_ERROR -> { }
                else -> {}
            }
        }
    }

    suspend fun drawBufferPoint(buffer: ArrayDeque<Draw>) {
        var draw: Draw?
        while (true) {
            delay(2000)
            draw = buffer.poll()
            if (draw != null) runOnUiThread {
                while (draw != null) {
                    selectTool(draw!!.tool)
                    setColor(draw!!.color)
                    bind.rasterView.surfaceTouch(draw!!.Front)
                    bind.rasterView.surfaceTouch(draw!!.Rear)
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
        if (smallLayers.size >= 0xF) return
        smallLayers.add(RoomLayer())
        bind.rasterView.addLayer()
        bind.rasterView.refreshView()
        bind.rasterView.invalidate()
        smallLayers.last().bitmap = bind.rasterView.toBitmap(smallLayers.lastIndex)
        changeToLayer(smallLayers.lastIndex)
        layerAdapter.notifyDataSetChanged()
    }

    fun undo(view: View) {
        val stepModel = stepStack.undo()
        if (stepModel == null) toast("无法继续撤回")
        else {
            bind.rasterView.setStepModel(stepModel)
            layerPos = stepModel.index
            smallLayers[stepModel.index].bitmap = bind.rasterView.toBitmap(stepModel.index)
            layerAdapter.notifyDataSetChanged()
        }
    }

    fun redo(view: View) {
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
        layerPos = pos
        stepStack.addStep(bind.rasterView.getStepModel())
        layerAdapter.notifyItemChanged(pos)
    }

    fun onTextureReady() {
        add(bind.addLayerButton)
        changeToLayer(0)
        smallLayers[0].bitmap =
            bind.rasterView.strokesLayer[0].toBitmap(bind.rasterView.inkCanvas)
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
        layerAdapter.notifyItemChanged(layerPos)
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
                if (seekBar != null) smallLayers[layerPos].alpha =
                    ceil(seekBar.progress * 2.55).toInt()
                layerAdapter.notifyItemChanged(layerPos)
            }
        })

        popBind.addAlphaBtn.setOnClickListener {
            popBind.alphaSeekbar.progress++
            popBind.alphaNum.text = "${popBind.alphaSeekbar.progress}%"
            smallLayers[layerPos].alpha = ceil(popBind.alphaSeekbar.progress * 2.55).toInt()
            layerAdapter.notifyItemChanged(layerPos)
        }

        popBind.minusAlphaBtn.setOnClickListener {
            popBind.alphaSeekbar.progress--
            popBind.alphaNum.text = "${popBind.alphaSeekbar.progress}%"
            smallLayers[layerPos].alpha = ceil(popBind.alphaSeekbar.progress * 2.55).toInt()
            layerAdapter.notifyItemChanged(layerPos)
        }
    }

    fun deleteLayer() {
        if (smallLayers.lastIndex == 0) toast("最后一个图层无法删除")
        else {
            smallLayers.removeAt(layerPos)
            bind.rasterView.strokesLayer.removeAt(layerPos)
            bind.rasterView.currentFrameLayer.removeAt(layerPos)
            if (layerPos > 0) layerPos--
            bind.rasterView.refreshView()
            changeToLayer(layerPos)
        }
    }

    fun smallLayer(view: View) {
        when (bind.layerCard.visibility) {
            View.VISIBLE -> {
                bind.layerCard.visibility = View.GONE
                bind.btnSmallLayer.setImageResource(R.drawable.ic_expand_layer_card)
            }
            else -> {
                bind.layerCard.visibility = View.VISIBLE
                bind.btnSmallLayer.setImageResource(R.drawable.ic_close_layer_card)
            }
        }
    }
}
