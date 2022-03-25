package cn.copaint.audience.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.R
import cn.copaint.audience.adapter.YuanbeiDetailAdapter
import cn.copaint.audience.databinding.ActivityPayBinding
import cn.copaint.audience.interfaces.RecyclerListener
import cn.copaint.audience.listener.swipeRefreshListener.setListener
import cn.copaint.audience.model.BalanceRecord
import cn.copaint.audience.model.PayResult
import cn.copaint.audience.repo.api
import cn.copaint.audience.utils.AuthingUtils.loginCheck
import cn.copaint.audience.utils.DialogUtils
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.utils.PayUtils
import cn.copaint.audience.utils.PayUtils.yuanbeiExchangeRate
import cn.copaint.audience.viewmodel.PayViewModel
import com.alipay.sdk.app.EnvUtils

class PayActivity : BaseActivity() {

    val SDK_PAY_FLAG = 1
    private val TAG = "PayActivity"
    lateinit var binding: ActivityPayBinding
    lateinit var ryAdpater: YuanbeiDetailAdapter
    val payViewModel : PayViewModel by lazy {
        ViewModelProvider(this)[PayViewModel::class.java]
    }
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
        if (loginCheck()) {
            payViewModel.cursor = null
            payViewModel.YuanbeiDetailList.value?.clear()
            payViewModel.askYuanBeiDetailsInfo()
        }
    }

    override fun initView() {
        binding.firstGear.background =  AppCompatResources.getDrawable(this,R.drawable.cardview_checked_paypage)
        ryAdpater = YuanbeiDetailAdapter(this)
        binding.YuanbeiDetailRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.YuanbeiDetailRecyclerview.adapter = ryAdpater
        binding.YuanbeiDetailRecyclerview.setListener(this, object : RecyclerListener {
            override fun loadMoreSilent() {
                if (payViewModel.hasNextPage) {
                    payViewModel.askYuanBeiDetailsInfo()
                }
            }

            override fun refresh() {
                toast("刷新")
                onResume()
            }
        })

        val spannableString = SpannableString("立即购买".plus(resources.getString(R.string.recharge_first).plus("元贝")))
        spannableString.setSpan(RelativeSizeSpan(0.7F),4,spannableString.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        binding.submitButton.text = spannableString

        val currentNumObserver = Observer<Double>{
            updateBtnText(it.times(PayUtils.yuanbeiExchangeRate).toString())
        }
        val remainYuanbeiObserver  = Observer<Double> {
            binding.remainYuanbei.text = "元贝余额：$it"
        }
        val yuanbeiDetailListObserver = Observer<MutableList<BalanceRecord>> {
            ryAdpater.notifyDataSetChanged()
        }
        payViewModel.currentNum.observe(this,currentNumObserver)
        payViewModel.remainYuanbei.observe(this,remainYuanbeiObserver)
        payViewModel.YuanbeiDetailList.observe(this,yuanbeiDetailListObserver)
    }

    fun aliPay(view: View) {
        payViewModel.currentNum.value?.let { PayUtils.aliPay(this, it, mHandler) }
    }



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
            findViewById<View>(id).background = AppCompatResources.getDrawable(this,R.drawable.cardview_unchecked_paypage)
        }
        view.background = AppCompatResources.getDrawable(this,R.drawable.cardview_checked_paypage)
        when (view.id) {
            R.id.firstGear -> {
                val str = resources.getString(R.string.recharge_first)
                updateBtnText(str)
                payViewModel.currentNum.value = str.toDouble()/yuanbeiExchangeRate
            }
            R.id.SecondGear -> {
                val str = resources.getString(R.string.recharge_second)
                updateBtnText(str)
                payViewModel.currentNum.value = str.toDouble()/yuanbeiExchangeRate
            }
            R.id.ThirdGear -> {
                val str = resources.getString(R.string.recharge_third)
                updateBtnText(str)
                payViewModel.currentNum.value = str.toDouble()/yuanbeiExchangeRate
            }
            R.id.fourthGear -> {
                val str = resources.getString(R.string.recharge_fourth)
                updateBtnText(str)
                payViewModel.currentNum.value = str.toDouble()/yuanbeiExchangeRate
            }
            R.id.fifthGear -> {
                val str = resources.getString(R.string.recharge_fifth)
                updateBtnText(str)
                payViewModel.currentNum.value = str.toDouble()/yuanbeiExchangeRate
            }
            else -> {
                updateBtnText(payViewModel.currentNum.value?.times(yuanbeiExchangeRate).toString())
                DialogUtils.onMoneyInputDialog(binding.root,this)
                binding.submitButton.text = "立即购买"
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
                    val payResult = PayResult(msg.obj as Map<*, *>)
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


    fun updateBtnText(str: String){
        val spannableString = SpannableString("立即购买".plus(str.plus("元贝")))
        spannableString.setSpan(RelativeSizeSpan(0.7F),4,spannableString.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        binding.submitButton.text = spannableString
    }
}
