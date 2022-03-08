package cn.copaint.audience

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.copaint.audience.adapter.FragmentSearchAppointmentsAdapter
import cn.copaint.audience.adapter.SquareAppointmentAdapter
import cn.copaint.audience.databinding.ActivitySquareBinding
import cn.copaint.audience.databinding.DialogHomepageAddBinding
import cn.copaint.audience.fragment.SearchAppointmentFragment
import cn.copaint.audience.listener.swipeRefreshListener.setListener
import cn.copaint.audience.myinterface.RecyclerListener
import cn.copaint.audience.type.OrderDirection
import cn.copaint.audience.type.ProposalOrder
import cn.copaint.audience.type.ProposalWhereInput
import cn.copaint.audience.utils.*
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.*
import java.lang.Exception

class SquareActivity : AppCompatActivity() {

    lateinit var binding: ActivitySquareBinding
    var lastBackPressedTimeMillis = 0L
    val dataList: ArrayList<SearchAppointmentFragment.searchAppointmentInfo> = arrayListOf()
    val first = 10
    var cursor: Any? = null
    var hasNextPage = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySquareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this


        binding.proposalList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )
        binding.proposalList.adapter = SquareAppointmentAdapter(this)
        GlideEngine.loadGridImage(this,user.photo?:"",binding.userAvatar)

        binding.proposalList.setListener(object :RecyclerListener{
            override fun loadMore() {
                if(hasNextPage){
                    toast("加载更多...")
                    updateUiInfo()
                }else{
                    toast("拉到底了，客官哎...")
                }

            }

            override fun refresh() {
                toast("刷新")
                dataList.clear()
                cursor = null
                updateUiInfo()
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
        dataList.clear()
        cursor = null
        updateUiInfo()
    }

    private fun updateUiInfo(){
        binding.swipeRefreshLayout.isRefreshing = true
        val apolloclient = ApolloClient.Builder()
            .serverUrl("http://120.78.173.15:20000/query")
            .addHttpHeader("Authorization", "Bearer ${AuthingUtils.user.token}")
            .build()

        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                // 获取到约稿信息
                val response = apolloclient.query(
                    FindProposalsQuery(
                        after = Optional.presentIfNotNull(cursor),
                        first = Optional.presentIfNotNull(first),
                        orderBy = Optional.presentIfNotNull(ProposalOrder(OrderDirection.DESC)),
                        where = Optional.Absent
                    )
                ).execute()
                cursor = response.data?.proposals?.pageInfo?.endCursor
                hasNextPage = response.data?.proposals?.pageInfo?.hasNextPage ?: true
                Log.i(packageName, response.data.toString())
                // 批量查询用户昵称和头像
                response.data?.proposals?.edges?.forEach {
                    var tempInfo = it?.let { it ->

                        val creatorInfoResponse = apolloclient.query(
                            GetAuthingUsersInfoQuery(
                                listOf(
                                    it.node?.creator ?: ""
                                )
                            )
                        ).execute()

                        return@let SearchAppointmentFragment.searchAppointmentInfo(
                            it.node?.title,
                            it.node?.description,
                            it.node?.colorModel,
                            "",
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
                    }
                }
            } catch (e: ApolloException) {
                Log.e("SearchUsersFragment", "Failure", e)
                return@launch
            } catch (e: Exception) {
                Log.e("SearchUsersFragment", "Failure", e)
                return@launch
            }


            runOnUiThread {
                binding.proposalList.adapter?.notifyDataSetChanged()
                binding.swipeRefreshLayout.isRefreshing = false
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
            ToastUtils.toast("再按一次退出")
            lastBackPressedTimeMillis = System.currentTimeMillis()
        }
    }

    fun onSquare(view: View) {
        onResume()
    }

    fun onAddDialog(view: View) {
        DialogUtils.onAddDialog(binding.root, this)
    }
}