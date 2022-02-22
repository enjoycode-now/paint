package cn.copaint.audience.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.FansActivity
import cn.copaint.audience.FollowsActivity
import cn.copaint.audience.GetAuthingUsersInfoQuery
import cn.copaint.audience.R
import cn.copaint.audience.databinding.ItemFansBinding
import cn.copaint.audience.databinding.ItemFollowBinding
import cn.copaint.audience.model.Follow
import com.bumptech.glide.Glide

class FansAdapter(private val activity: FansActivity) : RecyclerView.Adapter<FansAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFansBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.fansList[position], activity)
    }

    override fun getItemCount() = activity.fansList.size

    class ViewHolder(val itemBind: ItemFansBinding) : RecyclerView.ViewHolder(itemBind.root) {
        fun bind(fans: GetAuthingUsersInfoQuery.AuthingUsersInfo, activity: FansActivity) {
            itemBind.nicikname.text = fans.nickname.takeIf { it == null }.let { "此用户未命名" }
            if (fans.photo == "" || fans.photo?.endsWith("svg") == true) {
                Glide.with(activity).load(R.drawable.avatar_sample).into(itemBind.avatar)
            } else {
                Glide.with(activity).load(fans.photo).into(itemBind.avatar)
            }
        }
    }
}
