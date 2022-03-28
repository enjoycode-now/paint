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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.FindIsFollowQuery
import cn.copaint.audience.GetAuthingUsersInfoQuery
import cn.copaint.audience.GetFollowersListQuery
import cn.copaint.audience.adapter.FansAdapter
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.ActivityFansBinding
import cn.copaint.audience.interfaces.RecyclerListener
import cn.copaint.audience.listener.swipeRefreshListener.setListener
import cn.copaint.audience.type.FollowerWhereInput
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.viewmodel.FansViewModel
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class FansActivity : BaseActivity() {
    lateinit var binding: ActivityFansBinding
    val fansAdapter = FansAdapter(this)
    val fansViewModel: FansViewModel by lazy {
        ViewModelProvider(this)[FansViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityFansBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this
        initView()
        fansViewModel.askData()
    }

    override fun initView() {
        //防止弹出软键盘时将屏幕顶上去
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        binding.swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#B5A0FD"))
        binding.followRecycle.layoutManager = LinearLayoutManager(this)
        binding.followRecycle.adapter = fansAdapter
        binding.followRecycle.setListener(this, object : RecyclerListener {
            override fun loadMore() {
                if (fansViewModel.hasNextPage) {
                    toast("加载更多...")
                    binding.animationView.visibility = View.VISIBLE
                    fansViewModel.askData()
                }
            }

            override fun refresh() {
                toast("刷新")
                fansViewModel.cursor = null
                binding.swipeRefreshLayout.isRefreshing = false
                fansViewModel.fansList.value?.clear()
                fansViewModel.isFollowList.clear()
                binding.searchEdit.setText("")
                binding.animationView.visibility = View.VISIBLE
                fansViewModel.askData()
            }
        })
        binding.searchEdit.setOnEditorActionListener { textview, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
                if (textview.text == "") {

                } else {
                    filterUser(textview.text.toString())
                }
            }
            false;
        }
        fansViewModel.currentUserID = intent.getStringExtra("currentUserID") ?: ""
        fansViewModel.where = FollowerWhereInput(
            userID = Optional.presentIfNotNull(
                fansViewModel.currentUserID
            )
        )

        val fansListObserver = Observer<ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo>> {
            fansAdapter.fansList.clear()
            fansAdapter.isFollowList.clear()
            fansAdapter.fansList.addAll(it)
            fansAdapter.isFollowList.addAll(fansViewModel.isFollowList)
            binding.fansCount.text = "${fansAdapter.fansList.size} 粉丝"
            fansAdapter.notifyDataSetChanged()
            binding.animationView.visibility = View.GONE
        }
        fansViewModel.fansList.observe(this, fansListObserver)
    }

    fun onBackPress(view: View) = onBackPressed()


    private fun filterUser(text: String) {
        fansAdapter.fansList.clear()
        fansAdapter.isFollowList.clear()
        if (text == "") {
            fansAdapter.fansList.addAll(fansViewModel.fansList.value!!)
            fansAdapter.isFollowList.addAll(fansViewModel.isFollowList)
        } else {
            fansViewModel.fansList.value?.forEachIndexed { index, authingUsersInfo ->
                if (authingUsersInfo.nickname?.contains(text) == true) {
                    fansAdapter.fansList.add(authingUsersInfo)
                    fansAdapter.isFollowList.add(fansViewModel.isFollowList[index])
                }
            }
        }
        fansAdapter.notifyDataSetChanged()
    }
}
