package com.wacom.will3.ink.raster.rendering.demo

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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog



import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelpay.PayReq
import com.wacom.will3.ink.raster.rendering.demo.wxapi.WXPayEntryActivity
import com.wacom.will3.ink.raster.rendering.demo.wxapi.util.WXConstants


class PayActivity : AppCompatActivity() , IWXAPIEventHandler {

    private lateinit var api: IWXAPI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay)

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, null)

        // 将应用的appId注册到微信
        api.registerApp(WXConstants.APP_ID)

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


    fun pay(view: View) {

        //调起支付
        val request = PayReq()
        request.appId = WXConstants.APP_ID
        request.partnerId = ""
        request.prepayId = ""
        request.packageValue = ""
        request.nonceStr = ""
        request.timeStamp = ""
        request.sign = ""
        api.sendReq(request)
        startActivity(Intent(this,WXPayEntryActivity::class.java))
    }

}