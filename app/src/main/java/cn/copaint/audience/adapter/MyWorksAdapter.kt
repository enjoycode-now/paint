package cn.copaint.audience.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.activity.MyWorksActivity
import cn.copaint.audience.databinding.ItemSupportWorksBinding
import cn.copaint.audience.utils.ToastUtils
import com.bumptech.glide.Glide

class MyWorksAdapter(private val activity: MyWorksActivity) : RecyclerView.Adapter<MyWorksAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSupportWorksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.myWorksList[position], position, activity)
    }

    override fun getItemCount() = activity.myWorksList.size

    class ViewHolder(val binding: ItemSupportWorksBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String, position: Int, activity: MyWorksActivity) {
            Glide.with(activity)
                .load(url)
                .centerCrop()
                .into(binding.image)

            binding.image.setOnClickListener {
                ToastUtils.toast("你点击了第${position + 1}张图片")
            }
        }
    }
}
