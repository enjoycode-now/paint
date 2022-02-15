package cn.copaint.audience.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.PayActivity
import cn.copaint.audience.UserActivity
import cn.copaint.audience.databinding.ItemYuanbeiDetailBinding
import cn.copaint.audience.utils.ToastUtils

class YuanbeiDetailAdapter(private var activity: PayActivity) : RecyclerView.Adapter<YuanbeiDetailAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemYuanbeiDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.detailNum.setText(activity.YuanbeiDetailList[position].toString())
    }

    override fun getItemCount(): Int {
        return activity.YuanbeiDetailList.size
    }

    class ViewHolder(val binding: ItemYuanbeiDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String, position: Int, activity: UserActivity) {
            binding.cardview.setOnClickListener {
                ToastUtils.toast("你点击了第${position + 1}条账单")
            }
        }
    }
}
