package cn.copaint.audience.utils

import android.app.Dialog
import android.os.Handler
import android.os.Message
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.CreateTopUpOrderMutation
import cn.copaint.audience.TopUpOrderPaymentQuery
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.repo.api
import cn.copaint.audience.type.TopUpOrderPaymentMethod
import cn.copaint.audience.utils.ToastUtils.toast
import com.alipay.sdk.app.PayTask
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object PayUtils {
    val SDK_PAY_FLAG = 1
    const val yuanbeiExchangeRate = 10  // 10 元贝 = 1 人民币

    fun aliPay(activity: AppCompatActivity, currentNum: Double, mHandler: Handler) {
        //若支付金额不在0.1~10000元之间，不允许支付
        if (currentNum !in 0.1..10000.0) {
            toast("充值金额不得低于0.1元，不高于10000元")
            return
        }
        var orderInfo = ""
        lateinit var dialog:Dialog
        activity.runOnUiThread{
            dialog = DialogUtils.getLoadingDialog(activity, false, "正在申请订单...")
            dialog.show()
        }


        CoroutineScope(Dispatchers.IO).launch {

            // 发送充值金额，获取订单编号
            val topUpOrderId = try {
                apolloClient(activity)
                    .mutation(CreateTopUpOrderMutation(currentNum))
                    .execute().data?.createTopUpOrder?.id ?: ""
            } catch (e: ApolloException) {
                Log.d("PayActivity", "Failure", e)
                if (dialog.isShowing) dialog.dismiss()
                return@launch
            }

            // 获取orderInfo
            orderInfo = try {
                apolloClient(activity).query(
                    TopUpOrderPaymentQuery(
                        topUpOrderId,
                        TopUpOrderPaymentMethod.ALIPAY
                    )
                ).execute().data?.topUpOrderPayment?.result.toString()
            } catch (e: ApolloException) {
                Log.d("PayActivity", "Failure", e)
                if (dialog.isShowing) dialog.dismiss()
                return@launch
            }
            if (dialog.isShowing)
                dialog.dismiss()

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