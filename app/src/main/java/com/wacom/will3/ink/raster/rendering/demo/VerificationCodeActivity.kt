package com.wacom.will3.ink.raster.rendering.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import cn.authing.core.graphql.GraphQLException
import cn.authing.core.types.LoginByPhoneCodeInput
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityVerificationcodeBinding
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.authenticationClient
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.user
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.app
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.toast


class VerificationCodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        val binding = ActivityVerificationcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        val phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        binding.vcivCode.setOnInputListener { code ->
            val intent = Intent(this@VerificationCodeActivity, UserActivity::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    user = authenticationClient.loginByPhoneCode(
                        LoginByPhoneCodeInput(phoneNumber, code)
                    ).execute()
                    runOnUiThread {
                        startActivity(intent)
                    }
                } catch (e: GraphQLException) {
                    toast("验证码不正确或已过期")
                } catch (e: Exception) {
                    toast(e.message ?: "发生未知报错")
                    Bugsnag.notify(e)
                }
            }
        }

    }
}