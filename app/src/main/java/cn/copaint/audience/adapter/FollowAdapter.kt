package cn.copaint.audience.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.*
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.ItemFollowBinding
import cn.copaint.audience.model.Follow
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.ToastUtils
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    inner class ViewHolder(val itemBind: ItemFollowBinding) : RecyclerView.ViewHolder(itemBind.root) {
        fun bind(follow: GetAuthingUsersInfoQuery.AuthingUsersInfo, activity: FollowsActivity) {
            itemBind.nicikname.text = follow.nickname ?: "此用户未命名"
            if (follow.photo == "" || follow.photo?.endsWith("svg") == true) {
                Glide.with(activity).load(R.drawable.avatar_sample).into(itemBind.avatar)
            } else {
                Glide.with(activity).load(follow.photo).into(itemBind.avatar)
            }

            itemBind.unsubscribeTouchHelpView.setOnClickListener{
                if (itemBind.unsubscribe.text.equals("已关注")) {
                    unFollowUser(follow.id,itemBind)
                }else{
                    followUser(follow.id,itemBind)
                }
            }
        }
    }

    /**
     * 关注
     * @param 目标用户id
     */
    fun followUser(userid: String, itemBind: ItemFollowBinding) {
        if (userid == AuthingUtils.user.id){
            ToastUtils.toast("不能关注自己")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient(activity).mutation(
                    FollowUserMutation(userid)
                ).execute()
            } catch (e: Exception) {
                ToastUtils.toast(e.toString())
                return@launch
            }

            if (response.data != null) {
                activity.runOnUiThread {
                    itemBind.unsubscribe.text = "已关注"
                    itemBind.unsubscribe.setTextColor(Color.parseColor("#A9A9A9"))
                    itemBind.unsubscribe.background = null
                }
            }
        }
    }


    /**
     * 取消关注
     * @param 目标用户id
     */
    fun unFollowUser(userid: String, itemBind: ItemFollowBinding) {
        if (userid == AuthingUtils.user.id){
            ToastUtils.toast("不能关注自己")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient(activity).mutation(
                    UnfollowUserMutation(userid)
                ).execute()
            } catch (e: Exception) {
                ToastUtils.toast(e.toString())
                return@launch
            }

            if (response.data != null) {
                activity.runOnUiThread {
                    itemBind.unsubscribe.text = "关注"
                    itemBind.unsubscribe.setTextColor(Color.parseColor("#8767E2"))
                    itemBind.unsubscribe.background =
                        activity?.getDrawable(R.drawable.btn_edit)
                }
            }
        }
    }


}
