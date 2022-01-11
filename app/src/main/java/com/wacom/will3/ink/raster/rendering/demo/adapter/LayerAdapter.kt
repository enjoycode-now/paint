package com.wacom.will3.ink.raster.rendering.demo.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
        holder.bind(activity.layerListRecyclerCache[position], position, activity)
    }

    override fun getItemCount() = activity.layerListRecyclerCache.size

    class ViewHolder(binding: ItemLayerSmallBinding) : RecyclerView.ViewHolder(binding.root) {
        val layerItem: View = binding.layerItem
        val layerImage: ImageView = binding.layerImage
        val imageLeft: ImageView = binding.imageLeft
        val layerNameText: TextView = binding.layerNameText
        val layerPercent: TextView = binding.textRight
        var viewType: Int = 1

        @SuppressLint("SetTextI18n")
        fun bind(roomLayer: RoomLayer, position: Int, activity: MainActivity) {
            layerImage.setImageBitmap(roomLayer.bitmap)
            layerNameText.text = "图层${position}"


            layerItem.setOnClickListener {

                if (activity.rasterDrawingSurface.layerPos != position) {
                    activity.changeToLayer(position)
                } else {
                    activity.layerToolPopupWindow(layerImage)
                }
                activity.onTextureReady()
            }
            if (viewType == 0)
                layerItem.setBackgroundColor(Color.parseColor("#00BCD4"))  //蓝色

            imageLeft.setImageResource(
                when (activity.layerListRecyclerCache[position].isShow) {
                    true -> R.drawable.ic_icon_xs
                    else -> R.drawable.ic_icon_yc
                }
            )

            imageLeft.setOnClickListener {
                when (activity.layerListRecyclerCache[position].isShow) {
                    true -> imageLeft.setImageResource(R.drawable.ic_icon_yc)
                    else -> imageLeft.setImageResource(R.drawable.ic_icon_xs)
                }

                // ToDo 本地隐藏/显示图层
//                activity.changeHideLayer(position)
            }


        }
    }

}

