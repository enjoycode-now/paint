package cn.copaint.audience.adapter

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.*
import cn.copaint.audience.activity.UserPageCreatorActivity
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.*
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
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val EMPTY_TYPE = 0
    val NORMAL_TYPE = 1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            NORMAL_TYPE -> {
                val binding =
                    ItemFollowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolder(binding)
            }
            else -> {
                val binding = ItemUserpageEmptyViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                EmptyViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(fragment.userList[position], fragment)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (fragment.userList.size == 0)
            EMPTY_TYPE
        else
            NORMAL_TYPE
    }

    override fun getItemCount() =
        if (fragment.binding.animationView.isVisible) 0 else if (fragment.userList.size == 0) 1 else fragment.userList.size

    inner class ViewHolder(private val itemBind: ItemFollowBinding) :
        RecyclerView.ViewHolder(itemBind.root) {
        fun bind(userInfo: SearchUsersFragment.searchUserInfo, fragment: SearchUsersFragment) {
            itemBind.nicikname.text =
                userInfo.nickName ?: fragment.resources.getString(R.string.un_give_name)
            if (userInfo.isFollow) {
                itemBind.unsubscribe.text = "已关注"
                itemBind.unsubscribe.setTextColor(Color.parseColor("#A9A9A9"))
                itemBind.unsubscribe.background = null
            } else {
                itemBind.unsubscribe.text = "关注"
                itemBind.unsubscribe.setTextColor(Color.parseColor("#8767E2"))
                itemBind.unsubscribe.background =
                    AppCompatResources.getDrawable(fragment.activity, R.drawable.btn_edit)
            }

            if (userInfo.avatar == "" || userInfo.avatar?.endsWith("svg") == true) {
                Glide.with(fragment).load(R.drawable.avatar_sample).into(itemBind.avatar)
            } else {
                Glide.with(fragment).load(userInfo.avatar).into(itemBind.avatar)
            }
            itemBind.root.setOnClickListener {
                fragment.activity.startActivity(
                    Intent(
                        fragment.activity,
                        UserPageCreatorActivity::class.java
                    ).putExtra("creatorId", userInfo.id)
                )
            }
            itemBind.unsubscribeTouchHelpView.setOnClickListener {
                AuthingUtils.loginCheck()
                if (itemBind.unsubscribe.text.equals("已关注")) {
                    unFollowUser(userInfo.id, itemBind)
                } else {
                    followUser(userInfo.id, itemBind)
                }
            }
        }
    }

    inner class EmptyViewHolder(itemBind: ItemUserpageEmptyViewBinding) :
        RecyclerView.ViewHolder(itemBind.root) {}

    /**
     * 关注
     * @param 目标用户id
     */
    fun followUser(userid: String, itemBind: ItemFollowBinding) {
        if (userid == AuthingUtils.user.id) {
            ToastUtils.toast("不能关注自己")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient(fragment.activity).mutation(
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
        if (userid == AuthingUtils.user.id) {
            ToastUtils.toast("不能关注自己")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient(fragment.activity).mutation(
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
                    itemBind.unsubscribe.background =
                        AppCompatResources.getDrawable(fragment.activity, R.drawable.btn_edit)
                }
            }
        }
    }
}