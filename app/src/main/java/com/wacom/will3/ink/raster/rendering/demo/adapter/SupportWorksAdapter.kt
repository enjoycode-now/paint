package com.wacom.will3.ink.raster.rendering.demo.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wacom.will3.ink.raster.rendering.demo.UserActivity
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityUserBinding
import com.wacom.will3.ink.raster.rendering.demo.databinding.ItemSupportWorksBinding
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils

class SupportWorksAdapter(private val activity : UserActivity) : RecyclerView.Adapter<SupportWorksAdapter.ViewHolder>() {






    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupportWorksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewholder = ViewHolder(binding)
        return viewholder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.supportWorksList[position], position, activity)
    }

    override fun getItemCount() = activity.supportWorksList.size



    class ViewHolder(val binding : ItemSupportWorksBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bitmap: Bitmap, position: Int, activity: UserActivity) {
            binding.image.setImageBitmap(bitmap)


            binding.image.setOnClickListener{
                ToastUtils.toast("你点击了第${position+1}张图片")
            }
        }



    }


}