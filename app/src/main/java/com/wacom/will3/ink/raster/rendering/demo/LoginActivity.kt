package com.wacom.will3.ink.raster.rendering.demo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.bugsnag.android.Bugsnag
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityLoginBinding
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.authenticationClient
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.app
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    lateinit var binding:ActivityLoginBinding
    var phoneNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        supportActionBar?.hide()
        val appUpdate = AppUpdater(this)
        appUpdate.setUpdateFrom(UpdateFrom.JSON)
        appUpdate.setUpdateJSON("https://dev.unicorn.org.cn/download-apps/update-changelog.json")
        appUpdate.start()
        appUpdate.setDisplay(Display.DIALOG)

        binding.phoneText.setText(phoneNumber)
        binding.phoneText.doAfterTextChanged {
            phoneNumber = binding.phoneText.text.toString()
            binding.submitButton.setColorFilter(
                if(isPhoneNumber(it.toString()))Color.rgb(181,160,255)
                else Color.rgb(228,220,252)
            )
        }
    }

    fun onSubmit(view: View){
        if (!isPhoneNumber(phoneNumber))toast("请输入正确的手机号")
        else if (!binding.checkbox.isChecked) toast("请同意《用户协议》《隐私政策》")
        else {
            val intent = Intent(this, VerificationCodeActivity::class.java)
            CoroutineScope(Dispatchers.IO).launch{
                try {
                    authenticationClient.sendSmsCode(phoneNumber).execute()
                    runOnUiThread {
                        intent.putExtra("phoneNumber",phoneNumber)
                        startActivity(intent)
                        finish()
                    }
                }catch (e:IOException){
                    handleSmsError(e.message ?: "")
                }
            }
        }
    }

    fun handleSmsError(cause:String){
        with(cause) {
            when {
                contains("1分钟") -> {
                    runOnUiThread {
                        intent.putExtra("phoneNumber", phoneNumber)
                        startActivity(intent)
                        finish()
                    }
                }
                contains("格式") -> toast("请输入正确的手机号")
                else -> toast(cause)
            }
        }
    }

    fun isPhoneNumber(str:String):Boolean{
        if (str.length!=11) return false
        for (i in str) if (i !in '0'..'9') return false
        return true
    }

}