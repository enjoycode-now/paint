package cn.copaint.audience

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.adapter.SquareAppointmentAdapter
import cn.copaint.audience.databinding.ActivitySquareBinding
import cn.copaint.audience.fragment.SearchAppointmentFragment
import cn.copaint.audience.listener.swipeRefreshListener.setListener
import cn.copaint.audience.interfaces.RecyclerListener
import cn.copaint.audience.model.Proposal
import cn.copaint.audience.type.OrderDirection
import cn.copaint.audience.type.ProposalOrder
import cn.copaint.audience.type.ProposalType
import cn.copaint.audience.type.ProposalWhereInput
import cn.copaint.audience.utils.*
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.DateUtils.getCurrentRcfDateStr
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.utils.ToastUtils.toastNetError
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.*
import java.lang.Exception
import java.time.Instant

class SquareActivity : AppCompatActivity() {

    lateinit var binding: ActivitySquareBinding
    var lastBackPressedTimeMillis = 0L
    var lastReloadTimeMillis = 0L
    val dataExpiredTimeMillis = 12L
    val dataList: ArrayList<Proposal> = arrayListOf()
    val first = 10
    var cursor: Any? = null
    var hasNextPage = true
    lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivitySquareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this


        binding.proposalList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )
        binding.proposalList.adapter = SquareAppointmentAdapter(this)
        GlideEngine.loadGridImage(this, user.photo ?: "", binding.userAvatar)

        binding.swipeRefreshLayout.setProgressViewOffset(true,-50,50)
        binding.proposalList.setListener(this,object : RecyclerListener {
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
                binding.swipeRefreshLayout.isRefreshing = false
                lastReloadTimeMillis = 0L
                onResume()
            }
        })
    }


    fun onUserPage(view: View) {
        if (AuthingUtils.loginCheck()) {
            startActivity(Intent(this, UserActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if ( System.currentTimeMillis() - lastReloadTimeMillis > dataExpiredTimeMillis){
            dataList.clear()
            cursor = null
            binding.proposalList.adapter?.notifyDataSetChanged()
            updateUiInfo()
            lastReloadTimeMillis = System.currentTimeMillis()
        }
    }

    private fun updateUiInfo() {
        binding.progressBar.visibility = View.VISIBLE
        val currentRcfDateStr = getCurrentRcfDateStr()
        val apolloClient = ApolloClient.Builder()
            .serverUrl("http://120.78.173.15:20000/query")
            .addHttpHeader("Authorization", "Bearer ${user.token}")
            .build()

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                // 获取到约稿信息
                val response = apolloClient.query(
                    FindProposalsQuery(
                        after = Optional.presentIfNotNull(cursor),
                        first = Optional.presentIfNotNull(first),
                        orderBy = Optional.presentIfNotNull(ProposalOrder(OrderDirection.DESC)),
                        where = Optional.presentIfNotNull(
                            ProposalWhereInput(
                                expiredAtGT = Optional.presentIfNotNull(currentRcfDateStr),
                                proposalType = Optional.presentIfNotNull(ProposalType.PUBLIC)
                            )
                        )
                    )
                ).execute()
                cursor = response.data?.proposals?.pageInfo?.endCursor
                hasNextPage = response.data?.proposals?.pageInfo?.hasNextPage ?: true
                Log.i(packageName, response.data.toString())
                // 批量查询用户昵称和头像
                response.data?.proposals?.edges?.forEach {
                    var tempInfo = it?.let { it ->

                        val creatorInfoResponse = apolloClient.query(
                            GetAuthingUsersInfoQuery(
                                listOf(
                                    it.node?.creator ?: ""
                                )
                            )
                        ).execute()

                        return@let Proposal(
                            it.node?.title,
                            it.node?.description,
                            it.node?.colorModel,
                            it.node?.expiredAt.toString(),
                            it.node?.createdAt.toString(),
                            "",
                            it.node?.colorModel,
                            "",
                            "",
                            "",
                            it.node?.balance,
                            it.node?.stock,
                            it.node?.examples,
                            it.node?.creator,
                            creatorInfoResponse.data?.authingUsersInfo?.get(0)?.nickname,
                            creatorInfoResponse.data?.authingUsersInfo?.get(0)?.photo
                        )
                    }
                    if (tempInfo != null) {
                        dataList.add(tempInfo)
                        runOnUiThread{
                            binding.proposalList.adapter?.notifyItemChanged(dataList.lastIndex)
                        }
                    }
                }
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                }

            } catch (e: ApolloException) {
                toastNetError()
                Log.e("SearchUsersFragment", "Failure", e)
            } catch (e: Exception) {
                toastNetError()
                Log.e("SearchUsersFragment", "Failure", e)
            }
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
        super.onDestroy()
        if (this::job.isInitialized) job.cancel()
    }
}