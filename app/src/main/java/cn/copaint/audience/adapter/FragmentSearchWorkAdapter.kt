package cn.copaint.audience.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.GetAuthingUsersInfoQuery
import cn.copaint.audience.R
import cn.copaint.audience.databinding.FragmentItemSearchWorkBinding
import cn.copaint.audience.databinding.ItemFollowBinding
import cn.copaint.audience.fragment.SearchUsersFragment
import cn.copaint.audience.fragment.SearchWorksFragment
import com.bumptech.glide.Glide

class FragmentSearchWorkAdapter(private val fragment: SearchWorksFragment) :
    RecyclerView.Adapter<FragmentSearchWorkAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentItemSearchWorkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(fragment.workList[position], fragment)
    }

    override fun getItemCount() = fragment.workList.size

    class ViewHolder(val itemBind: FragmentItemSearchWorkBinding) : RecyclerView.ViewHolder(itemBind.root) {
        fun bind(follow: String, fragment: SearchWorksFragment) {
//            itemBind.userName.text = follow.nickname ?: "此用户未命名"
            Glide.with(fragment).load(follow).into(itemBind.userAvatar)

            itemBind.followBtn.setOnClickListener{
//                if (itemBind.unsubscribe.text.equals("已关注")) {
//                    itemBind.unsubscribe.text = "关注"
//                    itemBind.unsubscribe.setTextColor(Color.parseColor("#8767E2"))
//                    itemBind.unsubscribe.background = fragment.activity?.getDrawable(R.drawable.btn_edit)
//                }else{
//                    itemBind.unsubscribe.text = "已关注"
//                    itemBind.unsubscribe.setTextColor(Color.parseColor("#A9A9A9"))
//                    itemBind.unsubscribe.background = null
//                }
            }

//            itemBind.personalWorksRecyclerView.adapter
        }
    }
}