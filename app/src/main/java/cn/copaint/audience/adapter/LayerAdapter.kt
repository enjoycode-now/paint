package cn.copaint.audience.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.activity.DrawActivity
import cn.copaint.audience.R
import cn.copaint.audience.databinding.ItemLayerSmallBinding
import cn.copaint.audience.model.RoomLayer
import paint.v1.Paint

class LayerAdapter(private val activity: DrawActivity) : RecyclerView.Adapter<LayerAdapter.ViewHolder>() {

    override fun getItemViewType(position: Int) = if (position == activity.layerPos) 0 else 1 // 0:选中,1:未选中

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLayerSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding).apply { this.viewType = viewType }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.smallLayers[position], position, activity)
    }

    override fun getItemCount() = activity.smallLayers.size

    class ViewHolder(val itemBind: ItemLayerSmallBinding) : RecyclerView.ViewHolder(itemBind.root) {
        var viewType: Int = 1
        fun bind(roomLayer: RoomLayer, pos: Int, activity: DrawActivity) {
            itemBind.layerImage.setImageBitmap(roomLayer.bitmap)
            itemBind.layerNameText.text = "图层${pos + 1}"
            itemBind.textRight.text = "${String.format("%.0f",roomLayer.alpha * 100 / 255f)}%"
            with(activity) {
                if (viewType == 0) itemBind.layerItem.setBackgroundColor(resources.getColor(R.color.blue, null))
                itemBind.layerItem.setOnClickListener {
                    if (layerPos != pos) {
                        val payload = Paint.Layer.newBuilder()
                            .setAction(Paint.LayerAction.LAYER_ACTION_CHANGE)
                            .setIndex(pos)
                            .setAlpha(100)
                            .build()
                        val paintMessage = Paint.PaintMessage.newBuilder()
                            .setType(Paint.PaintType.PAINT_TYPE_LAYER)
                            .setSequence(seq++)
                            .setPayload(payload.toByteString())
                            .build()
                        sharedFlow.tryEmit(paintMessage)
                        changeToLayer(pos)
                    } else layerToolPopupWindow(it)
                }
                itemBind.imageLeft.setImageResource(
                    when (smallLayers[pos].isShow) {
                        true -> R.drawable.ic_icon_xs
                        else -> R.drawable.ic_icon_yc
                    }
                )
                itemBind.imageLeft.setOnClickListener {
                    val payload = Paint.Layer.newBuilder()
                        .setAction(Paint.LayerAction.LAYER_ACTION_ALPHA)
                        .setIndex(pos)
                        .setAlpha(100)
                        .build()
                    val paintMessage = Paint.PaintMessage.newBuilder()
                        .setType(Paint.PaintType.PAINT_TYPE_LAYER)
                        .setSequence(seq++)
                        .setPayload(payload.toByteString())
                        .build()
                    sharedFlow.tryEmit(paintMessage)
                    hideLayer(pos)
                }
            }
        }
    }
}
