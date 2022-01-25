package cn.copaint.audience.wxapi

import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.IWXAPI
import android.os.Bundle
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.constants.ConstantsAPI
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.utils.APP_ID
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast

/**
 * 描述 : 微信支付回调
 *
 * @作者 WeChat
 * @时间 2021年 2月1日
 *
 * 1、创建Android签名文件,获取包名和签名
 * 2、需要用签名文件打包成为apk文件，安装后，打开微信提供的获取签名的app，输入项目的包名
 * 微信开放平台：https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=8_5
 */
class WXPayEntryActivity : AppCompatActivity(), IWXAPIEventHandler {
    private lateinit var api: IWXAPI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = this
        api = WXAPIFactory.createWXAPI(this, APP_ID)
        api.handleIntent(intent, this)
    }

    override fun onReq(baseReq: BaseReq) {}

    override fun onResp(baseResp: BaseResp) {
        if (baseResp.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
            Log.e("info", "onPayFinish,errCode=" + baseResp.errCode)
            when (baseResp.errCode) {
                0 -> toast("支付成功")
                -1 -> toast("配置错误")
                -2 -> toast("取消支付")
            }
            finish()
        } else toast(baseResp.errStr)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        api.handleIntent(intent, this)
    }
}