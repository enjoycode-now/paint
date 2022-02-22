package cn.copaint.audience

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.adapter.FollowAdapter
import cn.copaint.audience.databinding.ActivityFollowsBinding
import cn.copaint.audience.type.FollowerWhereInput
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils.app
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FollowsActivity : AppCompatActivity() {

    var cursor = Optional.Absent
    var first = 10
    var where : FollowerWhereInput = FollowerWhereInput(followerID = Optional.presentIfNotNull(user.id))
    lateinit var binding: ActivityFollowsBinding
    val followList = ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo>()
    val followAdapter = FollowAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityFollowsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        //防止弹出软键盘时将屏幕顶上去
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        binding.followRecycle.layoutManager = LinearLayoutManager(this)
        binding.followRecycle.adapter = followAdapter
    }

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
                apolloClient.query(GetFollowersListQuery(cursor,first = Optional.presentIfNotNull(10), where = Optional.presentIfNotNull(where)))
                    .execute().data
            } catch (e: ApolloException) {
                Log.d("PayActivity", "Failure", e)
                return@launch
            }

            Log.i("FollowActivity", response.toString())

            // 获取全部关注对象的userid
            val userIdList = mutableListOf<String>()
            response?.followers?.edges?.forEach {
                it?.node?.userID?.let { it1 -> userIdList.add(it1) }
            }
            followList.clear()
            apolloClient.query(GetAuthingUsersInfoQuery(userIdList)).execute().data?.authingUsersInfo?.forEach{
                followList.add(it)
            }
            runOnUiThread{
                binding.fansCount.text = "关注 "+response?.followers?.totalCount.toString()
                followAdapter.notifyDataSetChanged()
            }
        }
    }

    fun onBackPress(view: View) = onBackPressed()
}
