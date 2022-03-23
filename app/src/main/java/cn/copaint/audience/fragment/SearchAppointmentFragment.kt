package cn.copaint.audience.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.*
import cn.copaint.audience.activity.SearchResultActivity
import cn.copaint.audience.adapter.FragmentSearchAppointmentsAdapter
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.FragmentSearchAppointmentsBinding
import cn.copaint.audience.interfaces.RecyclerListener
import cn.copaint.audience.listener.swipeRefreshListener.setListener
import cn.copaint.audience.type.*
import cn.copaint.audience.utils.ToastUtils
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class SearchAppointmentFragment(val activity: SearchResultActivity) : Fragment() {

    lateinit var binding: FragmentSearchAppointmentsBinding
    var searchText: String = ""
    val dataList: ArrayList<searchAppointmentInfo> = arrayListOf()
    val first = 20
    var cursor: Any? = null
    var hasNextPage = false
    lateinit var adapter: FragmentSearchAppointmentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSearchAppointmentsBinding.inflate(inflater, container, false)
        binding.appointmentsRecyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
        binding.swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#B5A0FD"))
        adapter = FragmentSearchAppointmentsAdapter(this)
        binding.appointmentsRecyclerView.adapter = adapter
        binding.appointmentsRecyclerView.setListener(activity, object : RecyclerListener {
            override fun loadMore() {
                if (hasNextPage) {
                    ToastUtils.toast("加载更多...")
                    updateUiInfo()
                }
            }

            override fun refresh() {
                ToastUtils.toast("刷新")
                binding.swipeRefreshLayout.isRefreshing = false
                onResume()
            }
        })



        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dataList.clear()
        cursor = null
        adapter.notifyDataSetChanged()
        updateUiInfo()
    }

    private fun updateUiInfo() {

        searchText = activity.binding.searchEdit.text.toString()
         if (searchText == ""){
            binding.animationView.visibility = View.GONE
            return
        }

        binding.animationView.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 获取到约稿信息
                val response = apolloClient(activity).query(
                    FindProposalsQuery(
                        after = Optional.presentIfNotNull(cursor),
                        first = Optional.presentIfNotNull(first),
                        orderBy = Optional.presentIfNotNull(ProposalOrder(OrderDirection.DESC)),
                        Optional.presentIfNotNull(
                            ProposalWhereInput(
                                or = Optional.presentIfNotNull(
                                    listOf(
                                        ProposalWhereInput(
                                            titleContains = Optional.presentIfNotNull(searchText)
                                        ),

                                        ProposalWhereInput(
                                            colorModel = Optional.presentIfNotNull(
                                                searchText
                                            )
                                        ),
                                        ProposalWhereInput(
                                            descriptionContains = Optional.presentIfNotNull(
                                                searchText
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                ).execute()
                hasNextPage = response.data?.proposals?.pageInfo?.hasNextPage == true
                cursor = response.data?.proposals?.pageInfo?.endCursor
                val tempDataList: ArrayList<searchAppointmentInfo> = arrayListOf()
                // 批量查询用户昵称和头像
                response.data?.proposals?.edges?.forEach {
                    var tempInfo = it?.let { it ->

                        val creatorInfoResponse = apolloClient(activity).query(
                            GetAuthingUsersInfoQuery(
                                listOf(
                                    it.node?.creator ?: ""
                                )
                            )
                        ).execute()

                        return@let searchAppointmentInfo(
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
                        tempDataList.add(tempInfo)
                    }
                }
                activity.runOnUiThread {
                    dataList.addAll(tempDataList)
                    binding.animationView.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                }


            } catch (e: ApolloException) {
                Log.e("SearchUsersFragment", "Failure", e)
                return@launch
            } catch (e: Exception) {
                Log.e("SearchUsersFragment", "Failure", e)
                return@launch
            }
        }
    }


    data class searchAppointmentInfo(
        val title: String? = "",
        val description: String? = "",
        var requirementType: String? = "", //需求类型
        var dealLine: String? = "",// 截稿日期
        var createAt: String? = "",
        var workStyle: String? = "", // 作品风格
        var colorMode: String? = "", // 颜色模式
        var dimensions: String? = "", // 尺寸规格
        var wordFormat: String? = "", // 稿件格式
        var acceptancePhase: String? = "",
        var balance: Int? = 0,
        val stock: Int? = 0,
        val example: List<FindProposalsQuery.Example>?,
        var creatorId: String? = "",
        var nickname: String? = "",
        var avatar: String? = ""
    )


}