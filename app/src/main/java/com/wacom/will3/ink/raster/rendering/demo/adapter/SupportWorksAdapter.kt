package com.wacom.will3.ink.raster.rendering.demo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wacom.will3.ink.raster.rendering.demo.UserActivity
import com.wacom.will3.ink.raster.rendering.demo.databinding.ItemSupportWorksBinding
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.toast

class SupportWorksAdapter(private val activity : UserActivity) : RecyclerView.Adapter<SupportWorksAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupportWorksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewholder = ViewHolder(binding)
        return viewholder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.sponsorList[position], position, activity)
    }

    override fun getItemCount() = activity.sponsorList.size

    class ViewHolder(val binding : ItemSupportWorksBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(url:String, position: Int, activity: UserActivity) {
            Glide.with(activity)
                .load(url)
                .centerCrop()
                .into(binding.image)

            binding.image.setOnClickListener{
                toast("你点击了第${position+1}张图片")
            }
        }
    }
}