package cn.copaint.audience.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.copaint.audience.*
import cn.copaint.audience.databinding.FragmentItemRecommendBinding
import cn.copaint.audience.type.FollowInfoInput
import cn.copaint.audience.type.FollowerWhereInput
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.BitmapUtils.picQueue
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.like.LikeButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ItemRecommendFragment : Fragment() {
    lateinit var binding: FragmentItemRecommendBinding

    //这个页面的画师的id
    val creatorId = "61e6083930d0d5c19dcfa947"

    //关注状态位
    var followStatus = false

    lateinit var apolloClient: ApolloClient


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentItemRecommendBinding.inflate(layoutInflater, container, false)

        binding.toolbar.likeBtn.setOnClickListener {
            (it as LikeButton).isLiked = !binding.toolbar.likeBtn.isLiked
        }

        binding.toolbar.authorAvator.setOnClickListener {
            val intent = Intent(context, UserPageCreatorActivity::class.java)
            intent.putExtra("creatorId", creatorId)
            startActivity(intent)
        }

        binding.toolbar.followBtn.setOnClickListener {
            if (AuthingUtils.loginCheck() && !followStatus) {
                followUser(creatorId)
            } else {
                unFollowUser(creatorId)
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            var url = picQueue.removeLastOrNull()
            while (url.isNullOrEmpty()) {
                delay(150)
                url = picQueue.removeLastOrNull()
            }
            picQueue.add("https://api.ghser.com/random/pe.php")


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


    fun followUser(userid: String) {
        val apolloClient = ApolloClient.Builder()
            .serverUrl("http://120.78.173.15:20000/query")
            .addHttpHeader("Authorization", "Bearer " + AuthingUtils.user.token!!)
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            val response = apolloClient.mutation(
                FollowUserMutation(userid)
            ).execute()

            if (response?.data != null) {
                activity?.runOnUiThread {
                    binding.toolbar.followBtn.setImageDrawable(context?.getDrawable(R.drawable.ic_unfollow))
                    followStatus = !followStatus
                }
            }
        }
    }

    fun unFollowUser(userid: String) {
        val apolloClient = ApolloClient.Builder()
            .serverUrl("http://120.78.173.15:20000/query")
            .addHttpHeader("Authorization", "Bearer " + AuthingUtils.user.token!!)
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            val response = apolloClient.mutation(
                UnfollowUserMutation(userid)
            ).execute()

            if (response?.data != null) {
                activity?.runOnUiThread {
                    binding.toolbar.followBtn.setImageDrawable(context?.getDrawable(R.mipmap.ic_follow))
                    followStatus = !followStatus
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateInfo()
    }

    fun updateInfo() {
        if(AuthingUtils.user.id != "") {
            val apolloClient = ApolloClient.Builder()
                .serverUrl("http://120.78.173.15:20000/query")
                .addHttpHeader("Authorization", "Bearer " + AuthingUtils.user.token)
                .build()

            CoroutineScope(Dispatchers.Default).launch {
                val response = apolloClient.query(
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

                activity?.runOnUiThread {
                    followStatus = if (response?.data?.followers?.totalCount != 0) {
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
