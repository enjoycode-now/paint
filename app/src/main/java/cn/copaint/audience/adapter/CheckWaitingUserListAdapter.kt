package cn.copaint.audience.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.*
import cn.copaint.audience.activity.FansActivity
import cn.copaint.audience.activity.UserPageCreatorActivity
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.databinding.DialogRemoveFanBinding
import cn.copaint.audience.databinding.ItemCheckWaitingUserListBinding
import cn.copaint.audience.databinding.ItemFansBinding
import cn.copaint.audience.databinding.ItemFollowBinding
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.GlideEngine
import cn.copaint.audience.utils.ToastUtils
import cn.copaint.audience.utils.dp
import com.bumptech.glide.Glide
import com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread
import com.wanglu.photoviewerlibrary.PhotoViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class CheckWaitingUserListAdapter(private val userList: ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo>, private val isFollowList : ArrayList<Boolean>,val context: Context) :
    RecyclerView.Adapter<CheckWaitingUserListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCheckWaitingUserListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userList[position], isFollowList[position],context)
    }

    override fun getItemCount() = userList.size

    inner class ViewHolder(private val itemBind: ItemCheckWaitingUserListBinding) : RecyclerView.ViewHolder(itemBind.root) {
        fun bind(
            userinfo: GetAuthingUsersInfoQuery.AuthingUsersInfo,
            isFollow: Boolean,
            context: Context
        ) {
            itemBind.nicikname.text = userinfo.nickname ?: context.getText(R.string.un_give_name)
            itemBind.biography.text = userinfo.biography ?: context.getText(R.string.un_give_biography)
            if (userinfo.photo == "" || userinfo.photo?.endsWith("svg") == true) {
                Glide.with(context).load(R.drawable.avatar_sample).into(itemBind.avatar)
            } else {
                Glide.with(context).load(userinfo.photo).into(itemBind.avatar)
            }

            itemBind.root.setOnClickListener {
                context.startActivity(Intent(context, UserPageCreatorActivity::class.java).putExtra("creatorId",userinfo.id))
            }
            if(AuthingUtils.user.id != userinfo.id){

                    itemBind.unsubscribe.visibility = View.VISIBLE

                    if (isFollow) {
                        itemBind.unsubscribe.text = "已关注"
                        itemBind.unsubscribe.setTextColor(Color.parseColor("#A9A9A9"))
                        itemBind.unsubscribe.background = null
                    } else {
                        itemBind.unsubscribe.text = "关注"
                        itemBind.unsubscribe.setTextColor(Color.parseColor("#8767E2"))
                        itemBind.unsubscribe.background =
                            AppCompatResources.getDrawable(context, R.drawable.btn_edit)
                    }
                    itemBind.unsubscribe.setOnClickListener {
                        if (itemBind.unsubscribe.text == "已关注") {
                            unFollowUser(userinfo.id, itemBind)
                        } else {
                            followUser(userinfo.id, itemBind)
                        }
                    }

            }else{
                itemBind.unsubscribe.visibility = View.GONE
            }


        }
    }

    /**
     * 关注
     * @param 目标用户id
     */
    fun followUser(userid: String, itemBind: ItemCheckWaitingUserListBinding) {
        if (userid == AuthingUtils.user.id) {
            ToastUtils.toast("不能关注自己")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                myApolloClient.apolloClient(context).mutation(
                    FollowUserMutation(userid)
                ).execute()
            } catch (e: Exception) {
                ToastUtils.toast(e.toString())
                return@launch
            }

            if (response.data != null) {
                runOnUiThread {
                    ToastUtils.toast("关注成功")
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
    fun unFollowUser(userid: String, itemBind: ItemCheckWaitingUserListBinding) {
        if (userid == AuthingUtils.user.id) {
            ToastUtils.toast("不能关注自己")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                myApolloClient.apolloClient(context).mutation(
                    UnfollowUserMutation(userid)
                ).execute()
            } catch (e: Exception) {
                ToastUtils.toast(e.toString())
                return@launch
            }

            if (response.data != null) {
                runOnUiThread {
                    ToastUtils.toast("已取消关注")
                    itemBind.unsubscribe.text = "关注"
                    itemBind.unsubscribe.setTextColor(Color.parseColor("#8767E2"))
                    itemBind.unsubscribe.background =
                        AppCompatResources.getDrawable(context, R.drawable.btn_edit)
                }
            }
        }
    }


}