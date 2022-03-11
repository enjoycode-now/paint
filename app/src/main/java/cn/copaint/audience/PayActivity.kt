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
import cn.copaint.audience.utils.aliPayUtils
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

    fun aliPay(view: View) {
        aliPayUtils.aliPay(this, currentNum, mHandler)
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
            CoroutineScope(Dispatchers.IO).launch {
                val response = try {
                    apolloClient(this@PayActivity).query(
                        PayActivityInitQuery(
                            Optional.presentIfNotNull(
                                BalanceRecordOrder(OrderDirection.DESC)
                            )
                        )
                    ).execute()
                } catch (e: ApolloException) {
                    Log.d("PayActivity", "Failure", e)
                    return@launch
                }
                val yuanbeiCount = (response.data?.wallet?.balance ?: 0.0)*aliPayUtils.yuanbeiExchangeRate
                binding.remainYuanbei.text = "元贝余额：$yuanbeiCount"
                YuanbeiDetailList.clear()
                var balanceRecord: BalanceRecord
                var i = 0
                while (i < response.data?.balanceRecords?.totalCount!!) {
                    var node = response.data?.balanceRecords?.edges?.get(i)
                    balanceRecord = BalanceRecord(
                        node?.node?.id!!,
                        node.node?.balance!! * aliPayUtils.yuanbeiExchangeRate,
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
