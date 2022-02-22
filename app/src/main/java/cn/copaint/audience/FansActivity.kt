package cn.copaint.audience

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.adapter.FansAdapter
import cn.copaint.audience.databinding.ActivityFansBinding
import cn.copaint.audience.databinding.ActivityFollowsBinding
import cn.copaint.audience.model.Follow
import cn.copaint.audience.type.FollowerWhereInput
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.ToastUtils.app
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FansActivity : AppCompatActivity() {
    lateinit var binding: ActivityFansBinding
    val fansList = ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo>()
    val fansAdapter = FansAdapter(this)
    var cursor = Optional.Absent
    var first = 10
    var where = FollowerWhereInput(
        userID = Optional.presentIfNotNull(
            AuthingUtils.user.id
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityFansBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        //防止弹出软键盘时将屏幕顶上去
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        binding.followRecycle.layoutManager = LinearLayoutManager(this)
        binding.followRecycle.adapter = fansAdapter
    }

    fun onBackPress(view: View) = onBackPressed()

    override fun onResume() {
        super.onResume()
        updateUiInfo()
    }

    fun updateUiInfo() {
        val apolloClient = ApolloClient.Builder()
            .serverUrl("http://120.78.173.15:20000/query")
            .addHttpHeader("Authorization", "Bearer " + AuthingUtils.user.token!!)
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient.query(
                    GetFollowersListQuery(
                        cursor,
                        first = Optional.presentIfNotNull(first),
                        where = Optional.presentIfNotNull(where)
                    )
                )
                    .execute().data
            } catch (e: ApolloException) {
                Log.d("PayActivity", "Failure", e)
                return@launch
            }

            Log.i("FollowActivity", response.toString())

            // 获取全部粉丝的userid
            val userIdList = mutableListOf<String>()
            response?.followers?.edges?.forEach {
                it?.node?.followerID?.let { followerId -> userIdList.add(followerId) }
            }

            fansList.clear()

            // 根据列表获取每一个粉丝的个人信息，然后添加到List去，最后notifyChange
            apolloClient.query(GetAuthingUsersInfoQuery(userIdList))
                .execute().data?.authingUsersInfo?.forEach {
                    fansList.add(it)
            }
            runOnUiThread {
                binding.fansCount.text = response?.followers?.totalCount.toString() + " 粉丝"
                fansAdapter.notifyDataSetChanged()
            }
        }
    }

}
