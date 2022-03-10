package cn.copaint.audience.utils

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.CreateTopUpOrderMutation
import cn.copaint.audience.PayOrderActivity
import cn.copaint.audience.TopUpOrderPaymentQuery
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.repo.api
import cn.copaint.audience.type.TopUpOrderPaymentMethod
import com.alipay.sdk.app.PayTask
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object aliPayUtils {
    val SDK_PAY_FLAG = 1

    fun aliPay(activity: AppCompatActivity, currentNum: Double, mHandler: Handler) {
        //若支付金额不在0.1~10000元之间，不允许支付
        if (currentNum !in 0.1..10000.0) {
            return
        }
        var orderInfo = ""


        CoroutineScope(Dispatchers.IO).launch {
            // 发送充值金额，获取订单编号
            val topUpOrderId = try {
                apolloClient(activity)
                    .mutation(CreateTopUpOrderMutation(currentNum))
                    .execute().data?.createTopUpOrder?.id ?: ""
            } catch (e: ApolloException) {
                Log.d("PayActivity", "Failure", e)
                return@launch
            }

            // 获取orderInfo
            orderInfo = try {
                myApolloClient.apolloClient(activity).query(
                    TopUpOrderPaymentQuery(
                        topUpOrderId,
                        TopUpOrderPaymentMethod.ALIPAY
                    )
                ).execute().data?.topUpOrderPayment?.result.toString()
            } catch (e: ApolloException) {
                Log.d("PayActivity", "Failure", e)
                return@launch
            }

            Log.i("PayActivity1", "TopUpOrderId: $topUpOrderId\norderInfo: $orderInfo")
            // 异步调起支付
            val payRunnable = Runnable {
                kotlin.run {
                    api.isPay = false
                    val alipay = PayTask(activity)
                    val result = alipay.payV2(
                        orderInfo,
                        true
                    ) // 传入true表示用户在商户app内部点击付款，是否需要一个 loading 做为在钱包唤起之前的过渡，这个值设置为 true，将会在调用 pay 接口的时候直接唤起一个 loading，直到唤起H5支付页面或者唤起外部的钱包付款页面 loading 才消失
                    val msg = Message()
                    msg.what = SDK_PAY_FLAG
                    msg.obj = result
                    mHandler.sendMessage(msg)
                }
            }

            // 必须异步调用
            val payThread = Thread(payRunnable)
            Log.i("PayActivity2", "TopUpOrderId: $topUpOrderId\norderInfo: $orderInfo")

            payThread.start()
        }
    }
}