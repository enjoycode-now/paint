package cn.copaint.audience.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.DrawActivity
import cn.copaint.audience.R
import cn.copaint.audience.databinding.ItemLayerSmallBinding
import cn.copaint.audience.model.RoomLayer


class LayerAdapter(private val activity: DrawActivity) :
    RecyclerView.Adapter<LayerAdapter.ViewHolder>() {


    override fun getItemViewType(position: Int) =
        if (position == activity.layerPos) 0 else 1  //0:选中,1:未选中


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemLayerSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewholder = ViewHolder(binding)
        viewholder.viewType = viewType
        return viewholder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.smallLayerList[position], position, activity)
    }

    override fun getItemCount() = activity.smallLayerList.size

    class ViewHolder(val binding: ItemLayerSmallBinding) : RecyclerView.ViewHolder(binding.root) {
        var viewType: Int = 1
        @SuppressLint("SetTextI18n")
        fun bind(roomLayer: RoomLayer, position: Int, activity: DrawActivity) {
            binding.layerImage.setImageBitmap(roomLayer.bitmap)
            binding.layerNameText.text = "图层${position + 1}"
            binding.textRight.text = "${String.format("%.0f",roomLayer.alpha*100/255f)}%"


            binding.layerItem.setOnClickListener {
                if (activity.layerPos != position) {
                    activity.changeToLayer(position)
                } else {
                    activity.layerToolPopupWindow(binding.layerImage)
                }
            }
            if (viewType == 0)
                binding.layerItem.setBackgroundColor(Color.parseColor("#00BCD4"))  //蓝色

            binding.imageLeft.setImageResource(
                when (activity.smallLayerList[position].isShow) {
                    true -> R.drawable.ic_icon_xs
                    else -> R.drawable.ic_icon_yc
                }
            )

            binding.imageLeft.setOnClickListener {
                activity.smallLayerList[position].isShow = !activity.smallLayerList[position].isShow
                activity.layerAdapter.notifyItemChanged(position)
                activity.changeVisibilityOfSmallLayer()
            }

            binding.textRight.setOnClickListener {
                if (activity.layerPos != position) {
                    activity.changeToLayer(position)
                }else{
                    activity.layerToolPopupWindow(binding.textRight)
                }

            }

        }
    }
}

