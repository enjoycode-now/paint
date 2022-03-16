package cn.copaint.audience

import android.accounts.NetworkErrorException
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.adapter.FansAdapter
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.ActivityFansBinding
import cn.copaint.audience.databinding.ActivityFollowsBinding
import cn.copaint.audience.databinding.DialogCreatorMoreBinding
import cn.copaint.audience.databinding.DialogRemoveFanBinding
import cn.copaint.audience.interfaces.RecyclerListener
import cn.copaint.audience.listener.swipeRefreshListener.setListener
import cn.copaint.audience.model.Follow
import cn.copaint.audience.type.FollowerWhereInput
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.utils.dp
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.bugsnag.android.Bugsnag
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class FansActivity : AppCompatActivity() {
    lateinit var binding: ActivityFansBinding
    val fansList = ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo>()
    // 对粉丝的关注状态 true-互相关注  false-单方面被关注
    val isFollowList = ArrayList<Boolean>()
    val fansAdapter = FansAdapter(this)
    var cursor :Any? = null
    var first = 20
    lateinit var currentUserID : String
    var hasNextPage = false
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
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this

        //防止弹出软键盘时将屏幕顶上去
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        binding.swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#B5A0FD"))
        binding.followRecycle.layoutManager = LinearLayoutManager(this)
        binding.followRecycle.adapter = fansAdapter
        binding.followRecycle.setListener(this, object : RecyclerListener {
            override fun loadMore() {
                if (hasNextPage) {
                    toast("加载更多...")
                    updateUiInfo()
                } else {
                    toast("拉到底了，客官哎...")
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
        currentUserID = intent.getStringExtra("currentUserID")?:""
    }

    fun onBackPress(view: View) = onBackPressed()

    override fun onResume() {
        super.onResume()
        fansList.clear()
        isFollowList.clear()
        binding.searchEdit.setText("")
        updateUiInfo()
    }


    fun updateUiInfo() {
        binding.animationView.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient(this@FansActivity).query(
                    GetFollowersListQuery(
                        after = Optional.presentIfNotNull(cursor),
                        first = Optional.presentIfNotNull(first),
                        where = Optional.presentIfNotNull(where)
                    )
                )
                    .execute().data
            } catch (e: ApolloException) {
                Log.d("FansActivity", "Failure", e)
                return@launch
            } catch (e: Exception){
                toast(e.toString())
                return@launch
            }
            response?.followers?.pageInfo?.endCursor?.let { it -> cursor = it }
            response?.followers?.pageInfo?.hasNextPage.let { hasNextPage = it ?: false }
            Log.i("FollowActivity", response.toString())

            // 获取全部粉丝的userid
            val userIdList = mutableListOf<String>()
            response?.followers?.edges?.forEach {
                it?.node?.followerID?.let { followerId -> userIdList.add(followerId) }
            }

            // 根据列表获取每一个粉丝的个人信息，然后添加到List去，最后notifyChange
            apolloClient(this@FansActivity).query(GetAuthingUsersInfoQuery(userIdList))
                .execute().data?.authingUsersInfo?.forEach {
                    fansList.add(it)
            }

            userIdList.forEach{
                val followResponse = apolloClient(this@FansActivity).query(FindIsFollowQuery(where = Optional.presentIfNotNull(
                    FollowerWhereInput(userID = Optional.presentIfNotNull(it),followerID = Optional.presentIfNotNull(currentUserID))))
                ).execute().data
                if (followResponse?.followers?.totalCount == 1){
                    isFollowList.add(true)
                }else{
                    isFollowList.add(false)
                }
            }


            runOnUiThread {
                fansAdapter.fansList.clear()
                fansAdapter.isFollowList.clear()
                fansAdapter.fansList.addAll(fansList)
                fansAdapter.isFollowList.addAll(isFollowList)
                binding.fansCount.text = "${fansAdapter.fansList.size} 粉丝"
                fansAdapter.notifyDataSetChanged()
                binding.animationView.visibility = View.GONE
            }
        }
    }
    private fun filterUser(text: String) {
        fansAdapter.fansList.clear()
        fansAdapter.isFollowList.clear()
        if(text == ""){
            fansAdapter.fansList.addAll(fansList)
            fansAdapter.isFollowList.addAll(isFollowList)
        }else{
            fansList.forEachIndexed { index, authingUsersInfo ->
                if (authingUsersInfo.nickname?.contains(text) == true) {
                    fansAdapter.fansList.add(authingUsersInfo)
                    fansAdapter.isFollowList.add(isFollowList[index])
                }
            }
        }
        fansAdapter.notifyDataSetChanged()
    }
}
