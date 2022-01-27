package cn.copaint.audience

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.lang.Exception
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import cn.copaint.audience.utils.APP_ID
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelpay.PayReq
import cn.copaint.audience.wxapi.WXPayEntryActivity
import com.alipay.sdk.app.PayTask





class PayActivity : AppCompatActivity() , IWXAPIEventHandler {

    /**
     * 用于支付宝支付业务的入参 app_id。
     */
    val APPID = ""

    /**
     * 用于支付宝账户登录授权业务的入参 pid。
     */
    val PID = ""

    /**
     * 用于支付宝账户登录授权业务的入参 target_id。
     */
    val TARGET_ID = ""

    val RSA2_PRIVATE = ""
    val RSA_PRIVATE = ""

    private val SDK_PAY_FLAG = 1
    private val SDK_AUTH_FLAG = 2

    private lateinit var api: IWXAPI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay)

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, null)

        // 将应用的appId注册到微信
        api.registerApp(APP_ID)

        try {
            api.handleIntent(intent, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onFinish(view: View) {
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        api.handleIntent(intent, this)
    }

    override fun onReq(p0: BaseReq?) {
    }

    override fun onResp(p0: BaseResp?) {
        val result = 0
        //有时候支付结果还需要发送给服务器确认支付状态
        if (p0?.getType() === ConstantsAPI.COMMAND_PAY_BY_WX) {
            if (p0?.errCode === 0) {
                Toast.makeText(this, "支付成功", Toast.LENGTH_LONG).show()
            } else if (p0.errCode === -2) {
                Toast.makeText(this, "取消支付", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "支付失败", Toast.LENGTH_LONG).show()
            }
            val builder= AlertDialog.Builder(this)
            builder.setTitle("提示")
        }
    }

    /**
     * 向服务器申请下单
     * */
    fun createOrder(){

    }

    fun wxpay(view: View) {

        //调起支付
        val request = PayReq()
        request.appId = APP_ID
        request.partnerId = ""
        request.prepayId = ""
        request.packageValue = ""
        request.nonceStr = ""
        request.timeStamp = ""
        request.sign = ""
        api.sendReq(request)
        startActivity(Intent(this,WXPayEntryActivity::class.java))
    }

    /**
     * 从后台获取预付单数据，唤醒支付宝app支付
     * */
//    fun AliPay(view : View){
//        val orderInfo : String = info // 订单信息
//
//        val payRunnable = Runnable {
//            val alipay = PayTask(this@PayActivity)
//            val result = alipay.payV2(orderInfo, true)
//            val msg = Message()
//            msg.what = SDK_PAY_FLAG
//            msg.obj = result
//            mHandler.sendMessage(msg)
//        }
//        // 必须异步调用
//        // 必须异步调用
//        val payThread = Thread(payRunnable)
//        payThread.start()
//    }


    /**
     * 从后台获取支付结果并处理
     * */
//    private var mHandler: Handler? = object : Handler() {
//        override fun handleMessage(msg: Message) {
//            when (msg.what) {
//                SDK_PAY_FLAG -> {
//                    val payResult = PayResult(msg.obj as Map<String?, String?>)
//
//                    /**
//                     * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
//                     */
//                    val resultInfo: String = payResult.getResult() // 同步返回需要验证的信息
//                    val resultStatus: String = payResult.getResultStatus()
//                    // 判断resultStatus 为9000则代表支付成功
//                    if (TextUtils.equals(resultStatus, "9000")) {
//                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
//                        com.alipay.sdk.pay.demo.PayDemoActivity.showAlert(
//                            this@PayDemoActivity,
//                            getString(R.string.pay_success) + payResult
//                        )
//                    } else {
//                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
//                        com.alipay.sdk.pay.demo.PayDemoActivity.showAlert(
//                            this@PayDemoActivity,
//                            getString(R.string.pay_failed) + payResult
//                        )
//                    }
//                }
//                com.alipay.sdk.pay.demo.PayDemoActivity.SDK_AUTH_FLAG -> {
//                    val authResult = AuthResult(msg.obj as Map<String?, String?>, true)
//                    val resultStatus: String = authResult.getResultStatus()
//
//                    // 判断resultStatus 为“9000”且result_code
//                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
//                    if (TextUtils.equals(
//                            resultStatus,
//                            "9000"
//                        ) && TextUtils.equals(authResult.getResultCode(), "200")
//                    ) {
//                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
//                        // 传入，则支付账户为该授权账户
//                        com.alipay.sdk.pay.demo.PayDemoActivity.showAlert(
//                            this@PayDemoActivity,
//                            getString(R.string.auth_success) + authResult
//                        )
//                    } else {
//                        // 其他状态值则为授权失败
//                        com.alipay.sdk.pay.demo.PayDemoActivity.showAlert(
//                            this@PayDemoActivity,
//                            getString(R.string.auth_failed) + authResult
//                        )
//                    }
//                }
//                else -> {}
//            }
//        }
//    }

}