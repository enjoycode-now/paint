package com.wacom.will3.ink.raster.rendering.demo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import cn.authing.core.types.User
import com.bugsnag.android.Bugsnag
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityLoginBinding
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.authenticationClient
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.user
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.app
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.toast
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    lateinit var binding:ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        supportActionBar?.hide()
        val appUpdate = AppUpdater(this)
        appUpdate.setUpdateFrom(UpdateFrom.JSON)
        appUpdate.setUpdateJSON("https://dev.unicorn.org.cn/download-apps/update-changelog.json")
        appUpdate.start()
        appUpdate.setDisplay(Display.DIALOG)

        binding.phoneText.doAfterTextChanged {
            if(isPhoneNumber(it.toString())) binding.submitButton.setColorFilter(Color.rgb(181,160,255))
            else binding.submitButton.setColorFilter(Color.rgb(228,220,252))
        }

        CoroutineScope(Dispatchers.IO).launch {
            val sharedPref = app.getSharedPreferences("Authing", Context.MODE_PRIVATE) ?: return@launch
            val arn = sharedPref.getString("arn","") ?: ""
            val userPoolId = sharedPref.getString("userPoolId","") ?: ""
            val id = sharedPref.getString("id","") ?: ""
            val token = sharedPref.getString("token","") ?: ""
            val nickname = sharedPref.getString("nickname","") ?: ""
            val photo = sharedPref.getString("photo","") ?: ""
            user = User(arn = arn, userPoolId = userPoolId, id=id,token=token, nickname = nickname,photo = photo)
            authenticationClient.setCurrentUser(user)
            val response = authenticationClient.checkLoginStatus().execute()
            toast(response.message ?: "")
            if(response.code == 200) runOnUiThread {
                val intent = Intent(this@LoginActivity, UserActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    fun onSubmit(view: View){
        val phonetext = binding.phoneText.text.toString()
        if (!isPhoneNumber(phonetext))toast("请输入正确的手机号")
        else if (!binding.checkbox.isChecked) toast("请同意《用户协议》《隐私政策》")
        else {
            val intent = Intent(this, VerificationCodeActivity::class.java)
            CoroutineScope(Dispatchers.IO).launch{
                try {
                    authenticationClient.sendSmsCode(phonetext).execute()
                    runOnUiThread {
                        intent.putExtra("phoneNumber",phonetext)
                        startActivity(intent)
                    }
                }catch (e:IOException){
                    toast(e.message ?: "发生错误")
                }
            }
        }
    }

    fun isPhoneNumber(str:String):Boolean{
        if (str.length!=11) return false
        for (i in str) if (i !in '0'..'9') return false
        return true
    }

}