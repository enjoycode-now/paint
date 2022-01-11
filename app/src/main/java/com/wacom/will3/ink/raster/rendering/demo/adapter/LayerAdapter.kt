package com.wacom.will3.ink.raster.rendering.demo.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wacom.will3.ink.raster.rendering.demo.MainActivity
import com.wacom.will3.ink.raster.rendering.demo.R
import com.wacom.will3.ink.raster.rendering.demo.databinding.ItemLayerSmallBinding
import com.wacom.will3.ink.raster.rendering.demo.model.RoomLayer
import kotlinx.android.synthetic.main.activity_main.*


class LayerAdapter(private val activity: MainActivity) :
    RecyclerView.Adapter<LayerAdapter.ViewHolder>() {


    override fun getItemViewType(position: Int) =
        if (position == activity.rasterDrawingSurface.layerPos) 0 else 1  //0:选中,1:未选中


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
        fun bind(roomLayer: RoomLayer, position: Int, activity: MainActivity) {
            binding.layerImage.setImageBitmap(roomLayer.bitmap)
            binding.layerNameText.text = "图层${position+1}"


            binding.layerItem.setOnClickListener {

                if (activity.rasterDrawingSurface.layerPos != position) {
                    activity.changeToLayer(position)
                } else {
                    activity.layerToolPopupWindow(binding.layerImage)
                }
                activity.onTextureReady()
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
                activity.smallLayerList[position].isShow=!activity.smallLayerList[position].isShow
                activity.layerAdapter.notifyItemChanged(position)
            }
        }
    }
}

