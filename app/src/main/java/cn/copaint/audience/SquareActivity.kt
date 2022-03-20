package cn.copaint.audience

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.adapter.SquareAppointmentAdapter
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.ActivitySquareBinding
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
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.bugsnag.android.Bugsnag
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import java.lang.Exception

class SquareActivity : AppCompatActivity() {

    lateinit var binding: ActivitySquareBinding
    var lastBackPressedTimeMillis = 0L
    var lastReloadTimeMillis = 0L
    val dataExpiredTimeMillis = 120000L // 设置2分钟缓存有效时间
    val dataList: ArrayList<Proposal> = arrayListOf()
    val first = 20
    var cursor: Any? = null
    var hasNextPage = true
    lateinit var job: Job
    val APPEND = 0
    val RELOAD = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivitySquareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this



        lastReloadTimeMillis = 0L
        binding.proposalList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )
        binding.proposalList.adapter = SquareAppointmentAdapter(this)
        GlideEngine.loadGridImage(this, user.photo ?: "", binding.userAvatar)

        binding.swipeRefreshLayout.setProgressViewOffset(true, -50, 50)
        binding.swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#B5A0FD"))
        binding.proposalList.setListener(this, object : RecyclerListener {
            override fun loadMore() {
                if (hasNextPage) {
                    toast("加载更多...")
                    updateUiInfo(APPEND)
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
        if (System.currentTimeMillis() - lastReloadTimeMillis > dataExpiredTimeMillis) {
            cursor = null
            updateUiInfo(RELOAD)
            lastReloadTimeMillis = System.currentTimeMillis()
        }
    }

    private fun updateUiInfo(type: Int) {
        binding.animationView.visibility = View.VISIBLE
        val currentRcfDateStr = getCurrentRcfDateStr()


        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                // 获取到约稿信息
                val response = apolloClient(this@SquareActivity).query(
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
                val tempDataList: ArrayList<Proposal> = arrayListOf()
                // 批量查询用户昵称和头像
                response.data?.proposals?.edges?.forEach {
                    var tempInfo = it?.let { it ->

                        val creatorInfoResponse = apolloClient(this@SquareActivity).query(
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
                        tempDataList.add(tempInfo)
                    }
                }
                runOnUiThread {
                    when (type) {
                        RELOAD -> dataList.clear()
                        else -> {}
                    }
                    dataList.addAll(tempDataList)
                    binding.proposalList.adapter?.notifyDataSetChanged()
                    binding.animationView.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                }

            } catch (e: ApolloException) {
                toastNetError()
                Log.e("SearchUsersFragment", "Failure", e)
            }catch (e: CancellationException){
                // job was cancel
            }catch (e: Exception) {
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
        if (this::job.isInitialized) job.cancel()
        super.onDestroy()
    }
}
