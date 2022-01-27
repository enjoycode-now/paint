package cn.copaint.audience

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import cn.copaint.audience.databinding.ActivityPayBinding
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.pingplusplus.android.Pingpp
import com.pingplusplus.android.Pingpp.REQUEST_CODE_PAYMENT
import com.pingplusplus.android.Pingpp.createPayment
import com.pingplusplus.android.Pingpp.enableMiddlePage
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 * 【说明文档】https://github.com/PingPlusPlus/pingpp-android/blob/master/docs/ping%2B%2B安卓SDK使用文档.md
 *
 * 【注意】运行该示例，需要用户填写一个YOUR_URL。
 *
 * ping++ sdk 使用流程如下：
 * 1）客户端已经有订单号、订单金额、支付渠道
 * 2）客户端请求服务端获得charge。服务端生成charge的方式参考ping++ 官方文档，地址 https://pingxx.com/guidance/server/import
 * 3）收到服务端的charge，调用ping++ sdk 。
 * 4）onActivityResult 中获得支付结构。
 * 5）如果支付成功。服务端会收到ping++ 异步通知，支付成功依据服务端异步通知为准。
 */
lateinit var binding: ActivityPayBinding
val YOUR_URL = "http://218.244.151.190/demo/charge"
val CHARGE_URL: String = YOUR_URL
val LIVEMODE = false

class PayActivity : AppCompatActivity() {
    /**
     *开发者需要填一个服务端URL 该URL是用来请求支付需要的charge。务必确保，URL能返回json格式的charge对象。
     *服务端生成charge 的方式可以参考ping++官方文档，地址 https://pingxx.com/guidance/server/import
     *
     *【 http://218.244.151.190/demo/charge 】是 ping++ 为了方便开发者体验 sdk 而提供的一个临时 url 。
     * 该 url 仅能调用【模拟支付控件】，开发者需要改为自己服务端的 url 。
     */



    var orderAmount = 100//假设为100元订单


    /**
     * 微信支付渠道
     */
    private val CHANNEL_WECHAT = "wx"

    /**
     * 支付宝支付渠道
     */
    val CHANNEL_ALIPAY = "alipay"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        Pingpp.enableDebugLog(true)
    }

    /**
     * onActivityResult 获得支付结果，如果支付成功，服务器会收到ping++ 服务器发送的异步通知。
     * 最终支付成功根据异步通知为准
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.submitButton.setOnClickListener{
            aliPay(binding.submitButton)
        }

        //支付页面返回处理
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == RESULT_OK) {
                val result = data?.extras!!.getString("pay_result")
                /* 处理返回值
                 * "success" - payment succeed
                 * "fail"    - payment failed
                 * "cancel"  - user canceld
                 * "invalid" - payment plugin not installed
                 */
                val errorMsg = data.extras!!.getString("error_msg") // 错误信息
                val extraMsg = data.extras!!.getString("extra_msg") // 错误信息
                showMsg(result, errorMsg, extraMsg)
            }
        }
    }

    fun aliPay(view: View) {
        PaymentTask().execute(
            PaymentRequest(CHANNEL_ALIPAY,orderAmount)
        )
    }

    fun wxPay(view: View) {
        PaymentTask().execute(
            PaymentRequest(CHANNEL_WECHAT,orderAmount)
        )
    }

    fun showMsg(title: String?, msg1: String?, msg2: String?) {
        var str = title
        if (null != msg1 && msg1.length != 0) {
            str += """
            
            $msg1
            """.trimIndent()
        }
        if (null != msg2 && msg2.length != 0) {
            str += """
            
            $msg2
            """.trimIndent()
        }
        val builder = AlertDialog.Builder(this@PayActivity)
        builder.setMessage(str)
        builder.setTitle("提示")
        builder.setPositiveButton("OK", null)
        builder.create().show()
    }




    internal class PaymentTask :AsyncTask<PaymentRequest?, Void?, String?>() {
        override fun onPreExecute() {
            //按键点击之后的禁用，防止重复点击
//            binding.submitButton.setOnClickListener(null)

        }

        override fun doInBackground(vararg pr: PaymentRequest?): String? {
            val paymentRequest = pr[0]
            var data: String? = null
            try {
                val myObject = JSONObject()
                myObject.put("channel", paymentRequest?.channel)
                myObject.put("amount", paymentRequest?.amount)
                myObject.put("livemode", paymentRequest?.livemode)
                val json = myObject.toString()
                //向Your Ping++ Server SDK请求数据
                data = postJson(CHARGE_URL, json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return data
        }

        /**
         * 获取charge
         * @param urlStr charge_url
         * @param json 获取charge的传参
         * @return charge
         * @throws IOException
         */
        @Throws(IOException::class)
        fun postJson(urlStr: String, json: String): String? {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.doInput = true
            conn.outputStream.write(json.toByteArray())
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "utf-8"))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                return response.toString()
            }
            return null
        }

        /**
         * 获得服务端的charge，调用ping++ sdk。
         */
        override fun onPostExecute(data: String?) {
            if (null == data) {
                toast("请求出错,请检查URL\nURL无法获取charge")
                return
            }
            Log.d("charge", data)

            /*
              启用中间跳转页面，并且设置延迟 1.0s 才显示页面内容，
              SDK 自带非常简陋的页面布局，用户需要自定义该页面，
              参考示例增加一个 pingpp_payment_activity_main.xml 布局文件。
              必须保证包含三个按钮，id 分别为：
                pingpp_button_reopen 用于重试调用打开支付组件
                pingpp_button_cancel 当 SDK 无法获取结果时，用户点击这个，会返回值为 "user_cancel" 的 pay_result
                pingpp_button_success 同上，会返回值为 "user_success" 的 pay_result
             */enableMiddlePage(true, 1.0)

            // 除 QQ 钱包外，其他渠道调起支付方式：
            // 参数一：Activity 当前调起支付的 Activity
            // 参数二：data 获取到的 charge 或 order 的 JSON 字符串
            createPayment(this, data)

            // QQ 钱包调用方式
            // 参数一：Activity  当前调起支付的 Activity
            // 参数二：data 获取到的 charge 或 order 的 SON 字符串
            // 参数三：“qwalletXXXXXXX” 需与 AndroidManifest.xml 中的 scheme 值一致
            // Pingpp.createPayment(ClientSDKActivity.this, data, "qwalletXXXXXXX");
        }


    }

    internal class PaymentRequest(var channel: String, var amount: Int) {
        var livemode: Boolean

        init {
            livemode = LIVEMODE
        }
    }

    fun onFinish(view: android.view.View) {
        finish()
    }


}