package cn.copaint.audience.adapter

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.FollowUserMutation
import cn.copaint.audience.R
import cn.copaint.audience.UnfollowUserMutation
import cn.copaint.audience.activity.UserPageCreatorActivity
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.FragmentItemSearchWorkBinding
import cn.copaint.audience.databinding.ItemUserpageEmptyViewBinding
import cn.copaint.audience.fragment.SearchWorksFragment
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.GlideEngine
import cn.copaint.audience.utils.ToastUtils
import com.apollographql.apollo3.ApolloClient
import com.bumptech.glide.Glide
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener
import com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread
import com.wanglu.photoviewerlibrary.PhotoViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentSearchWorkAdapter(private val fragment: SearchWorksFragment) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val EMPTY_TYPE = 0
    val NORMAL_TYPE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            NORMAL_TYPE -> {
                val binding = FragmentItemSearchWorkBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
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
        if (holder is ViewHolder) {
            holder.bind(fragment.workList[position], fragment)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (fragment.workList.size == 0)
            EMPTY_TYPE
        else
            NORMAL_TYPE
    }

    override fun getItemCount() =
        if (fragment.binding.animationView.isVisible) 0 else if (fragment.workList.size == 0) 1 else fragment.workList.size

    inner class ViewHolder(private val itemBind: FragmentItemSearchWorkBinding) :
        RecyclerView.ViewHolder(itemBind.root) {
        fun bind(workInfo: SearchWorksFragment.searchWorkInfo, fragment: SearchWorksFragment) {
            itemBind.userName.text =
                workInfo.userName ?: fragment.activity.resources.getString(R.string.un_give_name)
            itemBind.biography.text = workInfo.work.node?.description
            itemBind.fansCount.text = "粉丝: ${workInfo.fansCount ?: "加载出错"}"
            Glide.with(fragment).load(workInfo.avatar ?: "").into(itemBind.userAvatar)
            val imageUri =
                fragment.resources.getString(R.string.PicUrlPrefix) + workInfo.work.node?.image?.key
            Glide.with(fragment).load(imageUri).error(R.drawable.loading_failed)
                .into(itemBind.workImage)
            if (workInfo.isFollow) {
                itemBind.followBtn.text = "已关注"
                itemBind.followBtn.setTextColor(Color.parseColor("#A9A9A9"))
                itemBind.followBtn.background = null
            } else {
                itemBind.followBtn.text = "关注"
                itemBind.followBtn.setTextColor(Color.parseColor("#8767E2"))
                itemBind.followBtn.background =
                    ResourcesCompat.getDrawable(fragment.resources, R.drawable.btn_edit, null)
            }
            itemBind.userName.setOnClickListener {
                fragment.activity.startActivity(
                    Intent(
                        fragment.activity,
                        UserPageCreatorActivity::class.java
                    ).putExtra("creatorId", workInfo.work.node?.creator)
                )
            }
            itemBind.userAvatar.setOnClickListener {
                workInfo.avatar?.let {
                    PhotoViewer.setClickSingleImg(
                        it,
                        itemBind.userAvatar
                    )   //因为本框架不参与加载图片，所以还是要写回调方法
                        .setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
                            override fun show(iv: ImageView, url: String) {
                                GlideEngine.loadImage(fragment.activity, url, iv)
                            }
                        })
                        .start(fragment.activity)
                }
            }

            itemBind.workImage.setOnClickListener {
                imageUri.let {
                    PhotoViewer.setClickSingleImg(
                        it,
                        itemBind.workImage
                    )   //因为本框架不参与加载图片，所以还是要写回调方法
                        .setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
                            override fun show(iv: ImageView, url: String) {
                                GlideEngine.loadImage(fragment.activity, url, iv)
                            }
                        })
                        .start(fragment.activity)
                }
            }
            itemBind.followBtn.setOnClickListener {
                AuthingUtils.loginCheck()
                if (itemBind.followBtn.text.equals("已关注")) {
                    unFollowUser(workInfo.work.node?.creator ?: "", itemBind)
                } else {
                    followUser(workInfo.work.node?.creator ?: "", itemBind)
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
    fun followUser(userid: String, itemBind: FragmentItemSearchWorkBinding) {
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
                    itemBind.followBtn.text = "关注"
                    itemBind.followBtn.setTextColor(Color.parseColor("#8767E2"))
                    itemBind.followBtn.background =
                        ResourcesCompat.getDrawable(fragment.resources, R.drawable.btn_edit, null)
                }
            }
        }
    }


    /**
     * 取消关注
     * @param 目标用户id
     */
    fun unFollowUser(userid: String, itemBind: FragmentItemSearchWorkBinding) {
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
                    itemBind.followBtn.text = "已关注"
                    itemBind.followBtn.setTextColor(Color.parseColor("#A9A9A9"))
                    itemBind.followBtn.background = null
                }
            }
        }
    }

}