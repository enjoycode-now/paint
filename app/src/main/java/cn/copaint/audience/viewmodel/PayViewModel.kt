package cn.copaint.audience.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import cn.copaint.audience.PayActivityInitQuery
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.model.BalanceRecord
import cn.copaint.audience.type.BalanceRecordOrder
import cn.copaint.audience.type.OrderDirection
import cn.copaint.audience.utils.PayUtils
import cn.copaint.audience.utils.ToastUtils.app
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object PayViewModel : BaseViewModel() {

    val first = 25
    var cursor: Any? = null
    var hasNextPage = true
    lateinit var job: Job
    var currentNum: MutableLiveData<Double> = MutableLiveData(1.0) // 充值金额
    val YuanbeiDetailList = MutableLiveData<ArrayList<BalanceRecord>>()
    val remainYuanbei: MutableLiveData<Double> = MutableLiveData(0.0)


    fun askYuanBeiDetailsInfo() {
        job = CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient(app).query(
                    PayActivityInitQuery(
                        orderBy = Optional.presentIfNotNull(
                            BalanceRecordOrder(OrderDirection.DESC)
                        ),
                        after = Optional.presentIfNotNull(cursor),
                        first = Optional.presentIfNotNull(first)
                    )
                ).execute().data
            } catch (e: ApolloException) {
                Log.d("PayActivity", "Failure", e)
                return@launch
            }
            val yuanbeiCount =
                (response?.wallet?.balance ?: 0.0) * PayUtils.yuanbeiExchangeRate
            remainYuanbei.postValue(yuanbeiCount)
            hasNextPage = response?.balanceRecords?.pageInfo?.hasNextPage ?: true
            cursor = response?.balanceRecords?.pageInfo?.endCursor
            val tempList = ArrayList<BalanceRecord>()
            YuanbeiDetailList.value?.let { tempList.addAll(YuanbeiDetailList.value!!) }
            var balanceRecord: BalanceRecord
            response?.balanceRecords?.edges?.forEach {
                balanceRecord = BalanceRecord(
                    it?.node?.id!!,
                    it.node.balance,
                    it.node.balanceRecordAction,
                    it.node.balanceRecordType,
                    it.node.createdAt.toString()
                )
                tempList.add(balanceRecord)
            }
            if(tempList !== YuanbeiDetailList.value)
                YuanbeiDetailList.postValue(tempList)
        }
    }
}