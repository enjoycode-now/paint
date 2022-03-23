package cn.copaint.audience.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.adapter.SquareAppointmentAdapter
import cn.copaint.audience.databinding.ActivitySquareBinding
import cn.copaint.audience.interfaces.RecyclerListener
import cn.copaint.audience.listener.swipeRefreshListener.setListener
import cn.copaint.audience.model.Proposal
import cn.copaint.audience.utils.*
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.viewmodel.SquareViewModel
import com.bugsnag.android.Bugsnag
import kotlin.collections.ArrayList

class SquareActivity : BaseActivity() {

    lateinit var binding: ActivitySquareBinding
    var lastBackPressedTimeMillis = 0L
    var lastReloadTimeMillis = 0L
    val dataExpiredTimeMillis = 120000L // 设置2分钟缓存有效时间
    val first = 10
    var cursor: Any? = null
    var hasNextPage = true
    val APPEND = 0
    val RELOAD = 1
    val squareViewModel: SquareViewModel by lazy {
        ViewModelProvider(this)[SquareViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivitySquareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)

        GlideEngine.config(this)
        app = this
        initView()
    }

    override fun initView() {

        lastReloadTimeMillis = 0L
        binding.proposalList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )
        GlideEngine.loadGridImage(this, user.photo ?: "", binding.userAvatar)
        binding.proposalList.adapter = SquareAppointmentAdapter(this)
        binding.swipeRefreshLayout.setProgressViewOffset(true, -50, 50)
        binding.swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#B5A0FD"))
        binding.proposalList.setListener(this, object : RecyclerListener {
            override fun loadMore() {
                if (squareViewModel.hasNextPage) {
                    toast("加载更多...")
                    binding.animationView.visibility = View.VISIBLE
                    squareViewModel.askProposalsInfo(APPEND)
                }
            }

            override fun loadMoreSilent() {
                if (squareViewModel.hasNextPage)  squareViewModel.askProposalsInfo(APPEND)
            }

            override fun refresh() {
                toast("刷新")
                binding.swipeRefreshLayout.isRefreshing = false
                lastReloadTimeMillis = 0L
                onResume()
            }
        })

        val dataListObserver = Observer<ArrayList<Proposal>> {
            binding.proposalList.adapter?.notifyDataSetChanged()
            binding.animationView.visibility = View.GONE
            binding.swipeRefreshLayout.isRefreshing = false
        }
        squareViewModel.dataList.observe(this, dataListObserver)
    }


    fun onUserPage(view: View) {
        if (AuthingUtils.loginCheck()) {
            startActivity(Intent(this, UserActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (System.currentTimeMillis() - lastReloadTimeMillis > dataExpiredTimeMillis) {
            squareViewModel.cursor = null
            binding.animationView.visibility = View.VISIBLE
            squareViewModel.askProposalsInfo(RELOAD)
            lastReloadTimeMillis = System.currentTimeMillis()
        }
    }


    fun onMessage(view: View) {
        startActivity(Intent(this, UserActivity::class.java))
    }

    fun onHomePage(view: View) {
        startActivity(Intent(this, HomePageActivity::class.java))
        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - lastBackPressedTimeMillis < 2000) {
            super.onBackPressed()
        } else {
            toast("再按一次退出")
            lastBackPressedTimeMillis = System.currentTimeMillis()
        }
    }

    fun onSquare(view: View) {
        onResume()
    }

    fun onAddDialog(view: View) {
        DialogUtils.onAddDialog(binding.root, this)
    }

    override fun onDestroy() {
        if (squareViewModel.job.isActive) squareViewModel.job.cancel()
        super.onDestroy()
    }
}
