package cn.copaint.audience.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import cn.copaint.audience.FindProposalsQuery
import cn.copaint.audience.GetAuthingUsersInfoQuery
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.model.Proposal
import cn.copaint.audience.type.OrderDirection
import cn.copaint.audience.type.ProposalOrder
import cn.copaint.audience.type.ProposalType
import cn.copaint.audience.type.ProposalWhereInput
import cn.copaint.audience.utils.DateUtils
import cn.copaint.audience.utils.ToastUtils
import cn.copaint.audience.utils.ToastUtils.app
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.*
import java.lang.Exception

object SquareViewModel : BaseViewModel() {
    val first = 20
    var cursor: Any? = null
    var hasNextPage = true
    val APPEND = 0
    val RELOAD = 1
    lateinit var job: Job
    val dataList: MutableLiveData<ArrayList<Proposal>> = MutableLiveData(ArrayList<Proposal>())

    fun askProposalsInfo(type: Int) {
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentRcfDateStr = DateUtils.getCurrentRcfDateStr()
                // 获取到约稿信息
                val response = myApolloClient.apolloClient(app).query(
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
                hasNextPage = response.data?.proposals?.pageInfo?.hasNextPage ?: false
                Log.i(app.packageName, response.data.toString())
                val tempDataList: ArrayList<Proposal> = arrayListOf()
                if (type == APPEND) dataList.value?.let { tempDataList.addAll(it) }
                // 批量查询用户昵称和头像
                response.data?.proposals?.edges?.forEach {
                    var tempInfo = it?.let { it ->

                        val creatorInfoResponse =
                            myApolloClient.apolloClient(app).query(
                                GetAuthingUsersInfoQuery(
                                    listOf(
                                        it.node?.creator ?: ""
                                    )
                                )
                            ).execute()

                        return@let Proposal(
                            it.node?.id,
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
                // 更新数据源
                dataList.postValue(tempDataList)

            } catch (e: ApolloException) {
                ToastUtils.toastNetError()
                Log.e("SearchUsersFragment", "Failure", e)
            } catch (e: CancellationException) {
                // job was cancel
            } catch (e: Exception) {
                ToastUtils.toastNetError()
                Log.e("SearchUsersFragment", "Failure", e)
            }
        }
    }
}