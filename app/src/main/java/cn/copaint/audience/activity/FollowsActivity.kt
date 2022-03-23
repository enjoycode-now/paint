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
import cn.copaint.audience.viewmodel.FollowsViewModel
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FollowsActivity : BaseActivity() {
    lateinit var binding: ActivityFollowsBinding
    val followAdapter = FollowAdapter(this)
    val followsViewModel : FollowsViewModel by lazy{
        ViewModelProvider(this)[FollowsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityFollowsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this
        initView()
    }

    override fun initView() {
        //防止弹出软键盘时将屏幕顶上去
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        binding.swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#B5A0FD"))
        binding.followRecycle.layoutManager = LinearLayoutManager(this)
        binding.followRecycle.adapter = followAdapter
        binding.followRecycle.setListener(this, object : RecyclerListener {
            override fun loadMore() {
                if (followsViewModel.hasNextPage) {
                    toast("加载更多...")
                    binding.animationView.visibility = View.VISIBLE
                    followsViewModel.askData()
                }
            }

            override fun refresh() {
                toast("刷新")
                followsViewModel.cursor = null
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

        followsViewModel.currentUserId = intent.getStringExtra("userId")
        followsViewModel.where = FollowerWhereInput(followerID = Optional.presentIfNotNull(followsViewModel.currentUserId))


        val followsListObserver = Observer<ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo>>{
            followAdapter.followList.clear()
            followAdapter.followList.addAll(it)
            binding.fansCount.text = "关注 ${followAdapter.followList.size}"
            followAdapter.notifyDataSetChanged()
            binding.animationView.visibility = View.GONE
        }
        followsViewModel.followList.observe(this,followsListObserver)
    }

    private fun filterUser(text: String) {
        followAdapter.followList.clear()
        if(text == ""){
            followAdapter.followList.addAll(followsViewModel.followList.value!!)
        }else{
            followsViewModel.followList.value?.forEach {
                if (it.nickname?.contains(text) == true) {
                    followAdapter.followList.add(it)
                }
            }
        }
        followAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        followsViewModel.followList.value?.clear()
        binding.searchEdit.setText("")
        binding.animationView.visibility = View.VISIBLE
        followsViewModel.askData()
    }


    fun onBackPress(view: View) = onBackPressed()
}
