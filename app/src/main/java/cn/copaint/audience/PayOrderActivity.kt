package cn.copaint.audience

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Chronometer
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.ActivityPayOrderBinding
import cn.copaint.audience.type.AttachmentKeysInput
import cn.copaint.audience.type.CreateProposalInput
import cn.copaint.audience.type.ProposalType
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.utils.ToastUtils.toastNetError
import com.apollographql.apollo3.api.Optional
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.*
import java.lang.Exception


class PayOrderActivity : AppCompatActivity() {
    lateinit var binding: ActivityPayOrderBinding
    var balance:Float = 0F
    var payOrderCount:Float = 0F
    var stock: Int = 0
    var perPrice: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayOrderBinding.inflate(layoutInflater)
        Bugsnag.start(this)
        setContentView(binding.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this


        stock = intent.getIntExtra("stock",0)
        perPrice = intent.getIntExtra("perPrice",0)
        payOrderCount = (stock*perPrice).toFloat()
        binding.yuanbeiText.text = "$payOrderCount"
        binding.chronometer.setOnChronometerTickListener {
            if (it.base - SystemClock.elapsedRealtime() <= 0){
                it.stop()
                toast("停止")
            }else if (it.base - SystemClock.elapsedRealtime() <= 5999){
                it.setTextColor(Color.parseColor("#c12c1f"))
            }
        }
        binding.yuanbeiConstraint.setOnClickListener { binding.checkbox1.isChecked = !binding.checkbox1.isChecked }
        binding.chronometer.base = SystemClock.elapsedRealtime()+300000
        binding.chronometer.start()

    }


    override fun onResume() {
        super.onResume()

        try {
            CoroutineScope(Dispatchers.IO).launch {
                val response = apolloClient(this@PayOrderActivity).query(
                    GetWalletQuery()
                ).execute()

                runOnUiThread{
                    if(response.data?.wallet?.balance != null){
                        balance = response.data?.wallet?.balance!!.toFloat()
                        binding.balance.text = "$balance"
                    }else{
                        toastNetError()
                        finish()
                    }
                }
            }
        }catch (e: Exception){
            toast(e.toString())
        }


    }

    fun onBackPress(view: View) {
        finish()
    }

    fun onPay(view: View) {
        // 遍历复选框，选择支付方式
        when (true){
            binding.checkbox1.isChecked ->{
                if (balance < payOrderCount){
                    toast("余额不足，请充值后操作")
                    return
                }
            }
            else ->{
                toast("你还没选择支付方式")
                return
            }
        }

        // 缺少端口，目前默认直接元贝支付成功

        val proposalTitle = intent.getStringExtra("proposalTitle") ?: ""
        val proposalDescription = intent.getStringExtra("proposalDescription") ?: ""
        val example = intent.getStringArrayListExtra("example")

        val job = CoroutineScope(Dispatchers.IO).async {
            val response = try {
                apolloClient(this@PayOrderActivity).mutation(
                    CreateProposalMutation(
                        CreateProposalInput(
                            proposalType = ProposalType.PUBLIC,
                            title = proposalTitle,
                            description = proposalDescription,
                            colorModel = "暖色调",
                            size = "1920x1080",
                            balance = perPrice,
                            stock = stock,
                            inviteUserID = Optional.Absent
                        ),
                        AttachmentKeysInput(Optional.presentIfNotNull(example)),
                        expiredAt = "2022-10-01" // 记住一定要是yyyy-MM-dd
                    )
                ).execute()

            } catch (e: Exception) {
                toast(e.toString())
                return@async false
            }
            Log.i(packageName, response.toString())

            return@async true
        }
        CoroutineScope(Dispatchers.Default).launch {
            val str = job.await()
            if (str) {
                toast("发布成功")
                delay(500)
                runOnUiThread {
                    // PublishRequirementActivity的启动模式是singleTop,如果处于栈顶，会调用onNewIntent，在那里finish该任务栈
                    val intent = Intent(
                        this@PayOrderActivity,
                        SquareActivity::class.java
                    )
                    startActivity(intent)
                }
            }
        }
    }

}