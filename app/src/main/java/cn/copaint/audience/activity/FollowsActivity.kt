package cn.copaint.audience.activity

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.GetAuthingUsersInfoQuery
import cn.copaint.audience.GetFollowersListQuery
import cn.copaint.audience.adapter.FollowAdapter
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.ActivityFollowsBinding
import cn.copaint.audience.interfaces.RecyclerListener
import cn.copaint.audience.listener.swipeRefreshListener.setListener
import cn.copaint.audience.type.FollowerWhereInput
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FollowsActivity : AppCompatActivity() {

    var cursor: Any? = null
    var first = 20
    var hasNextPage = false
    lateinit var where: FollowerWhereInput
    lateinit var binding: ActivityFollowsBinding
    val followList = ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo>()
    val followAdapter = FollowAdapter(this)
    var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityFollowsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this

        //防止弹出软键盘时将屏幕顶上去
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        binding.swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#B5A0FD"))
        binding.followRecycle.layoutManager = LinearLayoutManager(this)
        binding.followRecycle.adapter = followAdapter
        binding.followRecycle.setListener(this, object : RecyclerListener {
            override fun loadMore() {
                if (hasNextPage) {
                    toast("加载更多...")
                    updateUiInfo()
                }
            }

            override fun refresh() {
                toast("刷新")
                cursor = null
                binding.swipeRefreshLayout.isRefreshing = false
                onResume()
            }
        })
        binding.searchEdit.setOnEditorActionListener { textview, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
                if (textview.text == "") {
                    onResume()
                } else {
                    filterUser(textview.text.toString())
                }
            }
            false;
        }

        currentUserId = intent.getStringExtra("userId")
        where = FollowerWhereInput(followerID = Optional.presentIfNotNull(currentUserId))
    }

    private fun filterUser(text: String) {
        followAdapter.followList.clear()
        if(text == ""){
            followAdapter.followList.addAll(followList)
        }else{
            followList.forEach {
                if (it.nickname?.contains(text) == true) {
                    followAdapter.followList.add(it)
                }
            }
        }
        followAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        followList.clear()
        binding.searchEdit.setText("")
        updateUiInfo()
    }

    fun updateUiInfo() {
        binding.animationView.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            // 获取关注列表
            val response = try {
                apolloClient(this@FollowsActivity).query(
                    GetFollowersListQuery(
                        after = Optional.presentIfNotNull(
                            cursor
                        ),
                        first = Optional.presentIfNotNull(first),
                        where = Optional.presentIfNotNull(where)
                    )
                )
                    .execute().data
            } catch (e: ApolloException) {
                Log.d("FollowsActivity", "Failure", e)
                return@launch
            }
            response?.followers?.pageInfo?.endCursor?.let { it -> cursor = it }
            response?.followers?.pageInfo?.hasNextPage.let { hasNextPage = it ?: false }
            Log.i("FollowsActivity", response.toString())

            // 获取全部关注对象的userid
            val userIdList = mutableListOf<String>()
            response?.followers?.edges?.forEach {
                it?.node?.userID?.let { it1 -> userIdList.add(it1) }
            }

            // 根据id调用authing接口获取用户信息
            try {
                apolloClient(this@FollowsActivity).query(GetAuthingUsersInfoQuery(userIdList))
                    .execute().data?.authingUsersInfo?.forEach {
                        followList.add(it)
                    }

            } catch (e: Exception) {
                toast(e.toString())
            }
            runOnUiThread {
                followAdapter.followList.clear()
                followAdapter.followList.addAll(followList)
                binding.fansCount.text = "关注 ${followAdapter.followList.size}"
                followAdapter.notifyDataSetChanged()
                binding.animationView.visibility = View.GONE
            }
        }
    }

    fun onBackPress(view: View) = onBackPressed()
}
