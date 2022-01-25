package cn.copaint.audience

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.authing.core.graphql.GraphQLException
import cn.authing.core.types.LoginByPhoneCodeInput
import com.bugsnag.android.Bugsnag
import cn.copaint.audience.databinding.ActivityVerificationcodeBinding
import cn.copaint.audience.utils.AuthingUtils.authenticationClient
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.views.OnInputListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VerificationCodeActivity : AppCompatActivity() {

    var phoneNumber = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        val binding = ActivityVerificationcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        binding.vcivCode.setOnInputListener(object: OnInputListener {
            override fun onComplete(code: String) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        user = authenticationClient.loginByPhoneCode(
                            LoginByPhoneCodeInput(phoneNumber, code)
                        ).execute()
                        val sharedPref = app.getSharedPreferences("Authing", Context.MODE_PRIVATE)
                        sharedPref.edit().putString("token",user.token).apply()
                        finish()
                    } catch (e: GraphQLException) {
                        toast("验证码不正确或已过期")
                    } catch (e: Exception) {
                        toast(e.message ?: "发生未知报错")
                        Bugsnag.notify(e)
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        val intent = Intent(this,LoginActivity::class.java)
        intent.putExtra("phoneNumber",phoneNumber)
        startActivity(intent)
        finish()
    }
}