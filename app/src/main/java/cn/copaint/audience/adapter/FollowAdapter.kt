package cn.copaint.audience.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.*
import cn.copaint.audience.activity.FollowsActivity
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.ItemFollowBinding
import cn.copaint.audience.databinding.ItemUserpageEmptyViewBinding
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.GlideEngine
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.views.MyPhotoView
import com.bumptech.glide.Glide
import com.wanglu.photoviewerlibrary.PhotoViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FollowAdapter(private val activity: FollowsActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val EMPTY_TYPE = 0
    val NORMAL_TYPE = 1
    var followList: ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            NORMAL_TYPE -> {
                val binding =
                    ItemFollowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding)
            }
            else -> {
                val binding = ItemUserpageEmptyViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return EmptyViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FollowAdapter.ViewHolder) {
            holder.bind(followList[position], activity)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (followList.size == 0)
            EMPTY_TYPE
        else
            NORMAL_TYPE
    }

    override fun getItemCount(): Int {
        return if (followList.size == 0)
            1
        else
            followList.size
    }

    inner class ViewHolder(val itemBind: ItemFollowBinding) :
        RecyclerView.ViewHolder(itemBind.root) {
        fun bind(follow: GetAuthingUsersInfoQuery.AuthingUsersInfo, activity: FollowsActivity) {
            itemBind.nicikname.text = follow.nickname ?: "此用户未命名"
            if (follow.photo == "" || follow.photo?.endsWith("svg") == true) {
                Glide.with(activity).load(R.drawable.avatar_sample).into(itemBind.avatar)
            } else {
                Glide.with(activity).load(follow.photo).into(itemBind.avatar)
            }

            itemBind.unsubscribeTouchHelpView.setOnClickListener {
                if (itemBind.unsubscribe.text.equals("已关注")) {
                    unFollowUser(follow.id, itemBind)
                } else {
                    followUser(follow.id, itemBind)
                }
            }
            itemBind.avatar.setOnClickListener {
                follow.photo.let {
                    PhotoViewer
                        .setClickSingleImg(
                        it ?: "",
                        itemBind.avatar
                    )   //因为本框架不参与加载图片，所以还是要写回调方法
                        .setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
                            override fun show(iv: ImageView, url: String) {
                                GlideEngine.loadImage(activity, url, iv)
                            }
                        })
                        .start(activity)
                }
            }
        }
    }

    inner class EmptyViewHolder(val itemBind: ItemUserpageEmptyViewBinding) :
        RecyclerView.ViewHolder(itemBind.root) {}

    /**
     * 关注
     * @param 目标用户id
     */
    fun followUser(userid: String, itemBind: ItemFollowBinding) {
        if (userid == AuthingUtils.user.id) {
            toast("不能关注自己")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient(activity).mutation(
                    FollowUserMutation(userid)
                ).execute()
            } catch (e: Exception) {
                toast(e.toString())
                return@launch
            }

            if (response.data != null) {
                activity.runOnUiThread {
                    toast("关注成功")
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
            toast("不能关注自己")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient(activity).mutation(
                    UnfollowUserMutation(userid)
                ).execute()
            } catch (e: Exception) {
                toast(e.toString())
                return@launch
            }

            if (response.data != null) {
                activity.runOnUiThread {
                    toast("已取消关注")
                    itemBind.unsubscribe.text = "关注"
                    itemBind.unsubscribe.setTextColor(Color.parseColor("#8767E2"))
                    itemBind.unsubscribe.background =
                        AppCompatResources.getDrawable(activity, R.drawable.btn_edit)
                }
            }
        }
    }


}
