package cn.copaint.audience

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.adapter.YuanbeiDetailAdapter
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.ActivityPayBinding
import cn.copaint.audience.model.BalanceRecord
import cn.copaint.audience.model.PayResult
import cn.copaint.audience.repo.api
import cn.copaint.audience.type.BalanceRecordOrder
import cn.copaint.audience.type.OrderDirection
import cn.copaint.audience.type.TopUpOrderPaymentMethod
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.AuthingUtils.loginCheck
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.alipay.sdk.app.EnvUtils
import com.alipay.sdk.app.PayTask
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PayActivity : AppCompatActivity() {

    //    lateinit var mHandler: Handler
    val SDK_PAY_FLAG = 1
    private val TAG = "PayActivity"
    lateinit var topUpOrderId: String
    lateinit var binding: ActivityPayBinding
    lateinit var ryAdpater: YuanbeiDetailAdapter
    var currentNum: Double = 1.0
    val YuanbeiDetailList = mutableListOf<BalanceRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX)
        super.onCreate(savedInstanceState)
        binding = ActivityPayBinding.inflate(layoutInflater)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        setContentView(binding.root)
        app = this
        initView()
    }

    override fun onResume() {
        super.onResume()
        updateUiInfo()
    }

    private fun initView() {
        binding.firstGear.background = getDrawable(R.drawable.cardview_checked_paypage)
        ryAdpater = YuanbeiDetailAdapter(this)
        binding.YuanbeiDetailRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.YuanbeiDetailRecyclerview.adapter = ryAdpater
    }

    fun aliPay(view: android.view.View) {
        //若支付金额不在0.1~10000元之间，不允许支付
        if (currentNum !in 0.1..10000.0) {
            return
        }
        var orderInfo = ""


        CoroutineScope(Dispatchers.IO).launch {
            // 发送充值金额，获取订单编号
            topUpOrderId = try {
                apolloClient(this@PayActivity).mutation(CreateTopUpOrderMutation(currentNum))
                    .execute().data?.createTopUpOrder?.id ?: ""
            } catch (e: ApolloException) {
                Log.d("PayActivity", "Failure", e)
                return@launch
            }

            // 获取orderInfo
            orderInfo = try {
                apolloClient(this@PayActivity).query(
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
                    val alipay = PayTask(this@PayActivity)
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

    @SuppressLint("UseCompatLoadingForDrawables")
    fun onSelectNum(view: View) {
        val cardviewId = mutableListOf<Int>(
            R.id.firstGear,
            R.id.SecondGear,
            R.id.ThirdGear,
            R.id.fourthGear,
            R.id.fifthGear,
            R.id.LastGear
        )
        for (id in cardviewId) {
            findViewById<View>(id).background = getDrawable(R.drawable.cardview_unchecked_paypage)
        }
        view.background = getDrawable(R.drawable.cardview_checked_paypage)
        when (view.id) {
            R.id.firstGear -> {
                binding.submitButton.setText("立即购买" + "10元贝")
                currentNum = 1.0
            }
            R.id.SecondGear -> {
                binding.submitButton.setText("立即购买" + "60元贝")
                currentNum = 6.0
            }
            R.id.ThirdGear -> {
                binding.submitButton.setText("立即购买" + "300元贝")
                currentNum = 30.0
            }
            R.id.fourthGear -> {
                binding.submitButton.setText("立即购买" + "980元贝")
                currentNum = 98.0
            }
            R.id.fifthGear -> {
                binding.submitButton.setText("立即购买" + "9800元贝")
                currentNum = 98.0
            }
            else -> {
                binding.submitButton.setText("立即购买")
            }
        }
    }

    fun onBackBtn(view: View) {
        finish()
    }

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            Log.i(TAG, msg.toString())
            when (msg.what) {
                SDK_PAY_FLAG -> { // 支付回调
                    Log.i(TAG, "aliPay: $msg")
                    val payResult = PayResult(msg.obj as Map<String, String>)
                    val resultStatus = payResult.resultStatus
                    // 判断resultStatus 为9000则代表支付成功
                    when {
                        TextUtils.equals(resultStatus, "9000") -> {
                            toast("付款成功")
                            api.isPay = true // 全局支付成功状态
//                            startActivity( Intent(this@PayActivity,SureInfoActivity::class.java))//可以跳转到一个支付成功的页面
//                            finish()
                        }
                        TextUtils.equals(
                            resultStatus,
                            "6001"
                        ) -> toast("您未完成付款" + ":" + payResult.memo) // 放弃付款
                        else -> toast("付款失败" + ":" + payResult.memo)
                    }
                }
            }
        }
    }

    fun updateUiInfo() {
        if (loginCheck()) {
            val apolloclient = ApolloClient.Builder()
                .serverUrl("http://120.78.173.15:20000/query")
                .addHttpHeader("Authorization", "Bearer " + AuthingUtils.user.token!!)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                val response = try {
                    apolloclient.query(
                        PayACtivity_InitQuery(
                            Optional.presentIfNotNull(
                                BalanceRecordOrder(OrderDirection.DESC)
                            )
                        )
                    ).execute()
                } catch (e: ApolloException) {
                    Log.d("PayActivity", "Failure", e)
                    return@launch
                }
                binding.remainYuanbei.text = "元贝余额：" + (response.data?.wallet?.balance ?: 0)
                YuanbeiDetailList.clear()
                var balanceRecord: BalanceRecord
                var i = 0
                while (i < response.data?.balanceRecords?.totalCount!!) {
                    var node = response.data?.balanceRecords?.edges?.get(i)
                    balanceRecord = BalanceRecord(
                        node?.node?.id!!,
                        node.node?.balance!!,
                        node.node?.balanceRecordAction!!,
                        node.node?.balanceRecordType!!,
                        node.node?.createdAt.toString()
                    )
                    YuanbeiDetailList.add(balanceRecord)
                    i++
                }

                runOnUiThread {
                    ryAdpater.notifyDataSetChanged()
                    Log.i("PayActivity2", YuanbeiDetailList.size.toString())
                }
            }

        }
    }
}
