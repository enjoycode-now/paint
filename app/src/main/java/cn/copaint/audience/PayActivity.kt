package cn.copaint.audience

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Message
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.adapter.YuanbeiDetailAdapter
import cn.copaint.audience.databinding.ActivityPayBinding
import cn.copaint.audience.utils.ToastUtils.app
import com.alipay.sdk.app.PayTask

class PayActivity : AppCompatActivity() {

//    lateinit var mHandler: Handler
    val SDK_PAY_FLAG = 1
    lateinit var binding: ActivityPayBinding
    lateinit var ryAdpater: YuanbeiDetailAdapter
    val YuanbeiDetailList = mutableListOf<Int>(-100, -23, -344, 200, 34545, 234, 999)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        initView()
    }

    private fun initView() {
        binding.firstGear.background = getDrawable(R.drawable.cardview_checked_paypage)
        ryAdpater = YuanbeiDetailAdapter(this)
        binding.YuanbeiDetailRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.YuanbeiDetailRecyclerview.adapter = ryAdpater
    }

    fun aliPay(view: android.view.View) {
        var orderInfo = "2015052600090779&biz_content=%7B%22timeout_express%22%3A%2230m%22%2C%22seller_id%22%3A%22%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%2C%22total_amount%22%3A%220.02%22%2C%22subject%22%3A%221%22%2C%22body%22%3A%22%E6%88%91%E6%98%AF%E6%B5%8B%E8%AF%95%E6%95%B0%E6%8D%AE%22%2C%22out_trade_no%22%3A%22314VYGIAGG7ZOYY%22%7D&charset=utf-8&method=alipay.trade.app.pay&sign_type=RSA2&timestamp=2016-08-15%2012%3A12%3A15&version=1.0&sign=MsbylYkCzlfYLy9PeRwUUIg9nZPeN9SfXPNavUCroGKR5Kqvx0nEnd3eRmKxJuthNUx4ERCXe552EV9PfwexqW%2B1wbKOdYtDIb4%2B7PL3Pc94RZL0zKaWcaY3tSL89%2FuAVUsQuFqEJdhIukuKygrXucvejOUgTCfoUdwTi7z%2BZzQ%3D"

        // 异步调起支付
        val payRunnable = Runnable {
            kotlin.run {
                var alipay = PayTask(this)
                var result = alipay.payV2(orderInfo, true) // 传入true表示用户在商户app内部点击付款，是否需要一个 loading 做为在钱包唤起之前的过渡，这个值设置为 true，将会在调用 pay 接口的时候直接唤起一个 loading，直到唤起H5支付页面或者唤起外部的钱包付款页面 loading 才消失
                val msg = Message()
                msg.what = SDK_PAY_FLAG
                msg.obj = result
//                mHandler.sendMessage(msg)
            }
        }

        // 必须异步调用
        val payThread = Thread(payRunnable)
        payThread.start()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun onSelectNum(view: View) {
        val cardviewId = mutableListOf<Int>(R.id.firstGear, R.id.SecondGear, R.id.ThirdGear, R.id.fourthGear, R.id.fifthGear, R.id.LastGear)
        for (id in cardviewId) {
            findViewById<View>(id).background = getDrawable(R.drawable.cardview_unchecked_paypage)
        }
        view.background = getDrawable(R.drawable.cardview_checked_paypage)
        when (view.id) {
            R.id.firstGear -> { binding.submitButton.setText("立即购买" + "100元贝") }
            R.id.SecondGear -> { binding.submitButton.setText("立即购买" + "600元贝") }
            R.id.ThirdGear -> { binding.submitButton.setText("立即购买" + "300元贝") }
            R.id.fourthGear -> { binding.submitButton.setText("立即购买" + "9800元贝") }
            R.id.fifthGear -> { binding.submitButton.setText("立即购买" + "9800元贝") }
            else -> { binding.submitButton.setText("立即购买") }
        }
    }

    fun onBackBtn(view: View) {
        finish()
    }

//    @SuppressLint("HandlerLeak")
//    private val mHandler = object : Handler() {
//        override fun handleMessage(msg: Message) {
//            when (msg.what) {
//                SDK_PAY_FLAG -> {//支付回调
//                    val payResult = PayResult(msg.obj as Map<String, String>)
//                    val resultStatus = payResult.resultStatus
//                    // 判断resultStatus 为9000则代表支付成功
//                    when {
//                        TextUtils.equals(resultStatus, "9000") -> {
//                            toast("付款成功")
//                            Constants.isPay = true//全局支付成功状态
//                            startActivity<PayActivity>()
//                            finish()
//
//                        }
//                        TextUtils.equals(resultStatus, "6001") -> toast("您未完成付款") //放弃付款
//                        else -> toast("付款失败")
//                    }
//                }
//            }
//        }
//    }
}

class PayResult(map: Map<String, String>) {
    var resultStatus = ""
}
