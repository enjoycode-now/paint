package cn.copaint.audience.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.FollowsActivity
import cn.copaint.audience.GetAuthingUsersInfoQuery
import cn.copaint.audience.R
import cn.copaint.audience.databinding.ItemFollowBinding
import cn.copaint.audience.fragment.SearchUsersFragment
import com.bumptech.glide.Glide

class FragmentSearchUserAdapter(private val fragment: SearchUsersFragment) :
    RecyclerView.Adapter<FragmentSearchUserAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFollowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(fragment.userList[position], fragment)
    }

    override fun getItemCount() = fragment.userList.size

    class ViewHolder(val itemBind: ItemFollowBinding) : RecyclerView.ViewHolder(itemBind.root) {
        fun bind(follow: GetAuthingUsersInfoQuery.AuthingUsersInfo, fragment: SearchUsersFragment) {
            itemBind.nicikname.text = follow.nickname ?: "此用户未命名"
            if (follow.photo == "" || follow.photo?.endsWith("svg") == true) {
                Glide.with(fragment).load(R.drawable.avatar_sample).into(itemBind.avatar)
            } else {
                Glide.with(fragment).load(follow.photo).into(itemBind.avatar)
            }

            itemBind.unsubscribeTouchHelpView.setOnClickListener{
                if (itemBind.unsubscribe.text.equals("已关注")) {
                    itemBind.unsubscribe.text = "关注"
                    itemBind.unsubscribe.setTextColor(Color.parseColor("#8767E2"))
                    itemBind.unsubscribe.background = fragment.activity?.getDrawable(R.drawable.btn_edit)
                }else{
                    itemBind.unsubscribe.text = "已关注"
                    itemBind.unsubscribe.setTextColor(Color.parseColor("#A9A9A9"))
                    itemBind.unsubscribe.background = null
                }
            }
        }
    }
}