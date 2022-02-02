package cn.copaint.audience.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.DrawActivity
import cn.copaint.audience.R
import cn.copaint.audience.databinding.ItemLayerSmallBinding
import cn.copaint.audience.model.RoomLayer

class LayerAdapter(private val activity: DrawActivity) :
    RecyclerView.Adapter<LayerAdapter.ViewHolder>() {

    override fun getItemViewType(position: Int) = if (position == activity.layerPos) 0 else 1 // 0:选中,1:未选中

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLayerSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding).apply { this.viewType = viewType }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.smallLayers[position], position, activity)
    }

    override fun getItemCount() = activity.smallLayers.size

    class ViewHolder(val bind: ItemLayerSmallBinding) : RecyclerView.ViewHolder(bind.root) {
        var viewType: Int = 1
        @SuppressLint("SetTextI18n")
        fun bind(roomLayer: RoomLayer, pos: Int, activity: DrawActivity) {
            bind.layerImage.setImageBitmap(roomLayer.bitmap)
            bind.layerNameText.text = "图层${pos + 1}"
            bind.textRight.text = "${String.format("%.0f",roomLayer.alpha * 100 / 255f)}%"

            bind.layerItem.setOnClickListener {
                with(activity) {
                    if (layerPos != pos) changeToLayer(pos) else layerToolPopupWindow(it)
                }
            }
            if (viewType == 0) bind.layerItem.setBackgroundColor(activity.resources.getColor(R.color.blue, null))

            bind.imageLeft.setImageResource(
                when (activity.smallLayers[pos].isShow) {
                    true -> R.drawable.ic_icon_xs
                    else -> R.drawable.ic_icon_yc
                }
            )

            bind.imageLeft.setOnClickListener {
                with(activity) {
                    smallLayers[pos].isShow = !smallLayers[pos].isShow
                    layerAdapter.notifyItemChanged(pos)
                    bind.rasterView.refreshView()
                }
            }

            bind.textRight.setOnClickListener {
                with(activity) {
                    if (layerPos != pos) changeToLayer(pos) else layerToolPopupWindow(it)
                }
            }
        }
    }
}
