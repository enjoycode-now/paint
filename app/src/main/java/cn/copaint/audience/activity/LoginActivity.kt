package cn.copaint.audience.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import cn.copaint.audience.databinding.ActivityLoginBinding
import cn.copaint.audience.utils.AuthingUtils.authenticationClient
import cn.copaint.audience.utils.DialogUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.bugsnag.android.Bugsnag
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    var dialog: PopupWindow? = null
    private var phoneNumber = ""

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
                if (isPhoneNumber(it.toString())) Color.rgb(181, 160, 255)
                else Color.rgb(228, 220, 252)
            )
        }
    }

    fun onSubmit(view: View) {
        if (!isPhoneNumber(phoneNumber)) toast("请输入正确的手机号")
        else if (!binding.checkbox.isChecked) toast("请同意《用户协议》《隐私政策》")
        else {
            val intent = Intent(this, VerificationCodeActivity::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    authenticationClient.sendSmsCode(phoneNumber).execute()
                    runOnUiThread {
                        intent.putExtra("phoneNumber", phoneNumber)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: IOException) {
                    handleSmsError(e.message ?: "")
                }
            }
        }
    }

    private fun handleSmsError(cause: String) {
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

    private fun isPhoneNumber(str: String): Boolean {
        if (str.length != 11) return false
        for (i in str) if (i !in '0'..'9') return false
        return true
    }
    fun onUserAgreementDialog(view: View) {
        if (dialog?.isShowing == true) {
            return
        }
        dialog = DialogUtils.userAgreementDialog(this, window)
        dialog?.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
    }
    fun onPrivacyPolicyDialog(view: View) {
        if (dialog?.isShowing == true) {
            return
        }
        dialog = DialogUtils.privacyPolicyDialog(this, window)
        dialog?.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
    }



}
