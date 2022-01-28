package cn.copaint.audience.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.UserActivity
import cn.copaint.audience.databinding.ItemSupportWorksBinding
import cn.copaint.audience.utils.ToastUtils.toast
import com.bumptech.glide.Glide

class SupportWorksAdapter(private val activity: UserActivity) : RecyclerView.Adapter<SupportWorksAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSupportWorksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.sponsorList[position], position, activity)
    }

    override fun getItemCount() = activity.sponsorList.size

    class ViewHolder(val binding: ItemSupportWorksBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String, position: Int, activity: UserActivity) {
            Glide.with(activity)
                .load(url)
                .centerCrop()
                .into(binding.image)

            binding.image.setOnClickListener {
                toast("你点击了第${position + 1}张图片")
            }
        }
    }
}
