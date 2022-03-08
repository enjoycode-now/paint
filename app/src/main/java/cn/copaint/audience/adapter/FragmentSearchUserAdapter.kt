package cn.copaint.audience.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.*
import cn.copaint.audience.databinding.FragmentItemSearchUsersBinding
import cn.copaint.audience.databinding.FragmentItemSearchWorkBinding
import cn.copaint.audience.databinding.ItemFollowBinding
import cn.copaint.audience.fragment.SearchUsersFragment
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.ToastUtils
import com.apollographql.apollo3.ApolloClient
import com.bumptech.glide.Glide
import com.luck.picture.lib.thread.PictureThreadUtils
import com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    inner class ViewHolder(val itemBind: ItemFollowBinding) : RecyclerView.ViewHolder(itemBind.root) {
        fun bind(userInfo: SearchUsersFragment.searchUserInfo, fragment: SearchUsersFragment) {
            itemBind.nicikname.text = userInfo.nickName ?: "此用户未命名"
            if (userInfo.isFollow){
                itemBind.unsubscribe.text = "已关注"
                itemBind.unsubscribe.setTextColor(Color.parseColor("#A9A9A9"))
                itemBind.unsubscribe.background = null
            }else{
                itemBind.unsubscribe.text = "关注"
                itemBind.unsubscribe.setTextColor(Color.parseColor("#8767E2"))
                itemBind.unsubscribe.background = fragment.activity?.getDrawable(R.drawable.btn_edit)
            }

            if (userInfo.avatar == "" || userInfo.avatar?.endsWith("svg") == true) {
                Glide.with(fragment).load(R.drawable.avatar_sample).into(itemBind.avatar)
            } else {
                Glide.with(fragment).load(userInfo.avatar).into(itemBind.avatar)
            }

            itemBind.unsubscribeTouchHelpView.setOnClickListener{
                AuthingUtils.loginCheck()
                if (itemBind.unsubscribe.text.equals("已关注")) {
                    unFollowUser(userInfo.id,itemBind)
                }else{
                    followUser(userInfo.id,itemBind)
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
        val apolloClient = ApolloClient.Builder()
            .serverUrl("http://120.78.173.15:20000/query")
            .addHttpHeader("Authorization", "Bearer " + AuthingUtils.user.token)
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient.mutation(
                    FollowUserMutation(userid)
                ).execute()
            } catch (e: Exception) {
                ToastUtils.toast(e.toString())
                return@launch
            }

            if (response.data != null) {
                runOnUiThread {
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
        val apolloClient = ApolloClient.Builder()
            .serverUrl("http://120.78.173.15:20000/query")
            .addHttpHeader("Authorization", "Bearer " + AuthingUtils.user.token)
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient.mutation(
                    UnfollowUserMutation(userid)
                ).execute()
            } catch (e: Exception) {
                ToastUtils.toast(e.toString())
                return@launch
            }

            if (response.data != null) {
                runOnUiThread {
                    itemBind.unsubscribe.text = "关注"
                    itemBind.unsubscribe.setTextColor(Color.parseColor("#8767E2"))
                    itemBind.unsubscribe.background = fragment.activity?.getDrawable(R.drawable.btn_edit)
                }
            }
        }
    }
}