package cn.copaint.audience.adapter

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.*
import cn.copaint.audience.activity.FansActivity
import cn.copaint.audience.activity.UserPageCreatorActivity
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.DialogRemoveFanBinding
import cn.copaint.audience.databinding.ItemFansBinding
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.GlideEngine
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.utils.dp
import cn.copaint.audience.views.MyPhotoView
import com.bumptech.glide.Glide
import com.wanglu.photoviewerlibrary.PhotoViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class FansAdapter(private val activity: FansActivity) :
    RecyclerView.Adapter<FansAdapter.ViewHolder>() {
    val fansList: ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo> = arrayListOf()
    val isFollowList = ArrayList<Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFansBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(fansList[position], isFollowList[position], activity, this)
    }

    override fun getItemCount() = if (activity.binding.animationView.isVisible) 0 else activity.fansViewModel.fansList.value?.size ?: 0

    inner class ViewHolder(val itemBind: ItemFansBinding) : RecyclerView.ViewHolder(itemBind.root) {
        fun bind(
            fans: GetAuthingUsersInfoQuery.AuthingUsersInfo,
            isFollow: Boolean,
            activity: FansActivity,
            adapter: FansAdapter
        ) {
            itemBind.nicikname.text = fans.nickname ?: "此用户未命名"
            if (fans.photo == "" || fans.photo?.endsWith("svg") == true) {
                Glide.with(activity).load(R.drawable.avatar_sample).into(itemBind.avatar)
            } else {
                Glide.with(activity).load(fans.photo).into(itemBind.avatar)
            }

            itemBind.root.setOnClickListener {
                activity.startActivity(Intent(activity, UserPageCreatorActivity::class.java).putExtra("creatorId",fans.id))
            }
            if(user.id == activity.fansViewModel.currentUserID){
                if (user.id != fans.id) {
                    itemBind.status.visibility = View.VISIBLE
                    itemBind.unsubscribe.visibility = View.VISIBLE

                    if (isFollow) {
                        itemBind.status.text = "互相关注"
                        itemBind.status.setTextColor(Color.parseColor("#A9A9A9"))
                    } else {
                        itemBind.status.text = "回关"
                        itemBind.status.setTextColor(Color.parseColor("#8767E2"))
                    }
                    itemBind.status.setOnClickListener {
                        if (itemBind.status.text == "互相关注") {
                            unFollowUser(fans.id, itemBind)
                        } else {
                            followUser(fans.id, itemBind)
                        }
                    }
                    itemBind.avatar.setOnClickListener {
                        fans.photo.let {
                            PhotoViewer.setClickSingleImg(
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
                    itemBind.unsubscribe.setOnClickListener {
                        onConfirmRemoveDialog(activity, fans.id, fans.photo)
                    }
                } else {
                    itemBind.status.visibility = View.GONE
                    itemBind.unsubscribe.visibility = View.GONE
                }
            }else{
                itemBind.status.visibility = View.GONE
                itemBind.unsubscribe.visibility = View.GONE
            }


        }
    }

    /**
     * 互相关注
     * @param 目标用户id
     */
    fun followUser(userid: String, itemBind: ItemFansBinding) {

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
                    toast("互相关注成功")
                    itemBind.status.text = "互相关注"
                    itemBind.status.setTextColor(Color.parseColor("#A9A9A9"))
                }
            }
        }
    }


    /**
     * 取消互相关注
     * @param 目标用户id
     */
    fun unFollowUser(userid: String, itemBind: ItemFansBinding) {

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
                    toast("已取消互相关注")
                    itemBind.status.text = "回关"
                    itemBind.status.setTextColor(Color.parseColor("#8767E2"))
                }
            }
        }
    }
}

fun onConfirmRemoveDialog(activity: FansActivity, id: String, photo: String?) {
    var popBind = DialogRemoveFanBinding.inflate(LayoutInflater.from(activity))
    if (photo == "" || photo?.endsWith("svg") == true) {
        Glide.with(activity).load(R.drawable.avatar_sample).into(popBind.avatar)
    } else {
        Glide.with(activity).load(photo).into(popBind.avatar)
    }

    // 弹出PopUpWindow
    var layerDetailWindow = PopupWindow(popBind.root, 300.dp, 240.dp, true)
    layerDetailWindow.isOutsideTouchable = true
    val window = activity.window

    // 设置弹窗时背景变暗
    var layoutParams = window.attributes
    layoutParams.alpha = 0.4f // 设置透明度
    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    window.attributes = layoutParams

    // 弹窗消失时背景恢复
    layerDetailWindow.setOnDismissListener {
        layoutParams = window.attributes
        layoutParams.alpha = 1f
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams
    }

    layerDetailWindow.showAtLocation(activity.binding.root, Gravity.CENTER, 0, 0)

    popBind.cancelBtn.setOnClickListener {
        layerDetailWindow.dismiss()
    }
    popBind.closeBtn.setOnClickListener {
        layerDetailWindow.dismiss()
    }
    popBind.confirmBtn.setOnClickListener {

        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient(activity).mutation(RemoveFollowerMutation(id)).execute()
            } catch (e: Exception) {
                Log.e("FansAdapter", e.toString())
                return@launch
            }
            if (response.data?.removeFollower == 1) {
                activity.runOnUiThread{
                    activity.binding.animationView.visibility = View.VISIBLE
                    activity.fansViewModel.askData()
                    toast("删除成功")
                }
            }
            Log.i("adpater", id + "\n" + response.data?.removeFollower)
        }
        layerDetailWindow.dismiss()
    }


}
