package cn.copaint.audience.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.FansActivity
import cn.copaint.audience.FollowsActivity
import cn.copaint.audience.databinding.ItemFollowBinding
import cn.copaint.audience.model.Follow
import com.bumptech.glide.Glide

class FansAdapter(private val activity: FansActivity) : RecyclerView.Adapter<FansAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFollowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.fansList[position], activity)
    }

    override fun getItemCount() = activity.fansList.size

    class ViewHolder(val itemBind: ItemFollowBinding) : RecyclerView.ViewHolder(itemBind.root) {
        fun bind(follow: Follow, activity: FansActivity) {
            itemBind.nicikname.text = follow.name
            Glide.with(activity).load(follow.avatar).into(itemBind.avatar)
        }
    }
}
