package cn.copaint.audience.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.FollowsActivity
import cn.copaint.audience.GetAuthingUsersInfoQuery
import cn.copaint.audience.R
import cn.copaint.audience.databinding.ItemFollowBinding
import cn.copaint.audience.model.Follow
import com.bumptech.glide.Glide

class FollowAdapter(private val activity: FollowsActivity) :
    RecyclerView.Adapter<FollowAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFollowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.followList[position], activity)
    }

    override fun getItemCount() = activity.followList.size

    class ViewHolder(val itemBind: ItemFollowBinding) : RecyclerView.ViewHolder(itemBind.root) {
        fun bind(follow: GetAuthingUsersInfoQuery.AuthingUsersInfo, activity: FollowsActivity) {
            itemBind.nicikname.text = follow.nickname.takeIf { it == null }.let { "此用户未命名" }
            if (follow.photo == "" || follow.photo?.endsWith("svg") == true) {
                Glide.with(activity).load(R.drawable.avatar_sample).into(itemBind.avatar)
            } else {
                Glide.with(activity).load(follow.photo).into(itemBind.avatar)
            }
            itemBind.unsubscribe.setOnClickListener {
                if ((it as TextView).text.equals("已关注")) {
                    it.text = "关注"
                    it.setTextColor(Color.parseColor("#bb3d34"))
                }else{
                    it.text = "已关注"
                    it.setTextColor(Color.parseColor("#8767E2"))
                }
            }
        }
    }
}
