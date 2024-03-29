package cn.copaint.audience.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import cn.copaint.audience.*
import cn.copaint.audience.activity.UserPageCreatorActivity
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.*
import cn.copaint.audience.type.FollowerWhereInput
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.BitmapUtils.picQueue
import cn.copaint.audience.utils.ToastUtils.toast
import com.apollographql.apollo3.api.Optional
import com.bugsnag.android.Bugsnag
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.luck.picture.lib.animators.AnimationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

class ItemRecommendFragment : Fragment() {
    lateinit var binding: FragmentItemRecommendBinding

    //这个页面的画师的id（目前先写死）
    val creatorId = "61e6083930d0d5c19dcfa947"

    //关注状态位
    var followStatus = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentItemRecommendBinding.inflate(layoutInflater, container, false)
        context?.let { Bugsnag.start(it) }
        binding.toolbar.likeBtn.setOnClickListener {
            toast("点赞")
        }

        binding.toolbar.authorAvatar.setOnClickListener {
            val intent = Intent(context, UserPageCreatorActivity::class.java)
            intent.putExtra("creatorId", creatorId)
            startActivity(intent)
        }

        binding.toolbar.followBtn.setOnClickListener {
            if (AuthingUtils.loginCheck()) {
                if (!followStatus) {
                    followUser(creatorId)
                } else {
                    unFollowUser(creatorId)
                }
            }
        }

        binding.toolbar.sponsorBtn.setOnClickListener {
            toast("应援")
        }

        binding.toolbar.shareBtn.setOnClickListener{ activity?.let { it -> popupShareDialog(it.window) } }

        CoroutineScope(Dispatchers.Default).launch {
            var url = picQueue.removeLastOrNull()
            while (url.isNullOrEmpty()) {
                delay(150)
                url = picQueue.removeLastOrNull()
            }
            try {
                picQueue.add("https://api.ghser.com/random/pe.php")
            }catch (e: Exception){
                toast(e.toString())
            }



            activity?.runOnUiThread {
                Glide.with(this@ItemRecommendFragment)
                    .load(url)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .into(binding.image)
            }
        }
        return binding.root
    }

    private fun popupShareDialog(window: Window) {
        val popBind = DialogSharepageMoreBinding.inflate(LayoutInflater.from(activity))
        // 弹出PopUpWindow
        val layerDetailWindow = PopupWindow(popBind.root, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true)
        layerDetailWindow.isOutsideTouchable = true

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
        layerDetailWindow.showAtLocation(binding.root, Gravity.BOTTOM, 0, 0)
    }


    fun followUser(userid: String) {
        if (userid == AuthingUtils.user.id){
            toast("不能关注自己")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                context?.let {
                    apolloClient(it).mutation(
                        FollowUserMutation(userid)
                    ).execute()
                }
            } catch (e: Exception) {
                toast(e.toString())
                return@launch
            }

            if (response != null) {
                if (response.data != null) {
                    activity?.runOnUiThread {
                        binding.toolbar.followBtn.setImageDrawable(context?.getDrawable(R.drawable.ic_unfollow))
                        followStatus = !followStatus
                    }
                }
            }
        }
    }

    fun unFollowUser(userid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                context?.let {
                    apolloClient(it).mutation(
                        UnfollowUserMutation(userid)
                    ).execute()
                }
            } catch (e: Exception) {
                toast(e.toString())
                return@launch
            }
            activity?.runOnUiThread {
                if (response != null) {
                    if (response.data != null) {
                        binding.toolbar.followBtn.setImageDrawable(context?.getDrawable(R.mipmap.ic_follow))
                        followStatus = !followStatus
                    } else {
                        toast(response.errors?.get(0)?.message.toString())
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateInfo()
    }

    fun updateInfo() {
        if (AuthingUtils.user.id != "") {
            CoroutineScope(Dispatchers.Default).launch {
                val response = try {
                    context?.let {
                        apolloClient(it).query(
                            FindIsFollowQuery(
                                where = Optional.presentIfNotNull(
                                    FollowerWhereInput(
                                        userID = Optional.presentIfNotNull(
                                            creatorId
                                        ), followerID = Optional.presentIfNotNull(AuthingUtils.user.id)
                                    )
                                )
                            )
                        ).execute()
                    }
                } catch (e: Exception) {
                    toast(e.toString())
                    return@launch
                }

                activity?.runOnUiThread {
                    if (response != null) {
                        followStatus = if (response.data?.followers?.totalCount != 0) {
                            binding.toolbar.followBtn.setImageDrawable(context?.getDrawable(R.drawable.ic_unfollow))
                            true
                        } else {
                            binding.toolbar.followBtn.setImageDrawable(context?.getDrawable(R.mipmap.ic_follow))
                            false
                        }
                    }
                }
            }
        }
    }
}
