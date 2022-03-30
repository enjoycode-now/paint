package cn.copaint.audience.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.FindProposalsQuery
import cn.copaint.audience.GetAuthingUsersInfoQuery
import cn.copaint.audience.R
import cn.copaint.audience.adapter.CommonItemAdapter
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.databinding.FragmentCommonItemBinding
import cn.copaint.audience.type.OrderDirection
import cn.copaint.audience.type.ProposalOrder
import cn.copaint.audience.type.ProposalStatus
import cn.copaint.audience.type.ProposalWhereInput
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils.app
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.userAgent
import java.lang.Exception

class CommonItemFragment(val parentFragmentName: String, val type: Int) : Fragment() {
    lateinit var binding: FragmentCommonItemBinding
    val dataList: ArrayList<SearchAppointmentFragment.searchAppointmentInfo> = arrayListOf()
    val first = 20
    var cursor: Any? = null
    var hasNextPage = false
    val conditionsList = arrayListOf<ProposalWhereInput>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommonItemBinding.inflate(layoutInflater)
        binding.swipeRefreshLayout.setProgressViewOffset(false, -50, 200)
        binding.swipeRefreshLayout.setDistanceToTriggerSync(1000)
        binding.recycler.layoutManager = LinearLayoutManager(context)
        binding.recycler.adapter = CommonItemAdapter(this, type)
        addConditionsList()
        askData()
        binding.swipeRefreshLayout.setOnRefreshListener{
            askData()
        }
        return binding.root
    }

    fun addConditionsList() {
        when (type) {
            0 -> {
                conditionsList.add(
                    ProposalWhereInput(
                        proposalStatus = Optional.presentIfNotNull(
                            ProposalStatus.WAITING
                        )
                    )
                )
            }
            1 -> {
                conditionsList.add(
                    ProposalWhereInput(
                        proposalStatus = Optional.presentIfNotNull(
                            ProposalStatus.IN_PROGRESS
                        )
                    )
                )
            }
            2 -> {
                conditionsList.add(
                    ProposalWhereInput(
                        proposalStatus = Optional.presentIfNotNull(
                            ProposalStatus.SUCCESS
                        )
                    )
                )
            }
            3 -> {
                conditionsList.add(
                    ProposalWhereInput(
                        proposalStatus = Optional.presentIfNotNull(
                            ProposalStatus.FAILED
                        )
                    )
                )
            }
            else -> {}
        }
    }

    fun askData() {
        if (parentFragmentName == "MyProposals") {
            conditionsList.add(
                ProposalWhereInput(
                    proposalUserID = Optional.presentIfNotNull(
                        user.id
                    )
                )
            )
            startThreadForData()
        } else {

        }
    }

    fun startThreadForData() {
        binding.swipeRefreshLayout.isRefreshing = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 获取到约稿信息
                val response = myApolloClient.apolloClient(app).query(
                    FindProposalsQuery(
                        after = Optional.presentIfNotNull(cursor),
                        first = Optional.presentIfNotNull(first),
                        orderBy = Optional.presentIfNotNull(ProposalOrder(OrderDirection.DESC)),
                        Optional.presentIfNotNull(
                            ProposalWhereInput(
                                or = Optional.presentIfNotNull(
                                    conditionsList
                                )
                            )
                        )
                    )
                ).execute()
                hasNextPage = response.data?.proposals?.pageInfo?.hasNextPage == true
                cursor = response.data?.proposals?.pageInfo?.endCursor
                val tempDataList: ArrayList<SearchAppointmentFragment.searchAppointmentInfo> =
                    arrayListOf()
                // 批量查询用户昵称和头像
                response.data?.proposals?.edges?.forEach {
                    var tempInfo = it?.let { it ->

                        val creatorInfoResponse = myApolloClient.apolloClient(app).query(
                            GetAuthingUsersInfoQuery(
                                listOf(
                                    it.node?.creator ?: ""
                                )
                            )
                        ).execute()

                        return@let SearchAppointmentFragment.searchAppointmentInfo(
                            it.node?.id,
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
                runOnUiThread {
                    dataList.addAll(tempDataList)
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.recycler.adapter?.notifyDataSetChanged()
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
}