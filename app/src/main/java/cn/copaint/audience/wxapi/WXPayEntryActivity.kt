package cn.copaint.audience.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import cn.copaint.audience.wxapi.util.WXConstants;

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

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, WXConstants.Companion.getAPP_ID());
        api.handleIntent(getIntent(), this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        api = WXAPIFactory.createWXAPI(this, WXConstants.Companion.getAPP_ID());
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        api = WXAPIFactory.createWXAPI(this, WXConstants.Companion.getAPP_ID());
        api.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
    }

    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            Log.e("info", "onPayFinish,errCode=" + baseResp.errCode);
            if (baseResp.errCode == 0) {
                Toast.makeText(this, "支付成功", Toast.LENGTH_SHORT).show();
            } else if (baseResp.errCode == -1) {
                Toast.makeText(this, "配置错误", Toast.LENGTH_SHORT).show();
            } else if (baseResp.errCode == -2) {
                Toast.makeText(this, "取消支付", Toast.LENGTH_SHORT).show();
            }
            this.finish();
        } else {
            Toast.makeText(this, baseResp.errStr, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }
}
