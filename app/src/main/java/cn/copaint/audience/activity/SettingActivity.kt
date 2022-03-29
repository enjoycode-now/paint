package cn.copaint.audience.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.authing.core.types.User
import cn.copaint.audience.R
import cn.copaint.audience.databinding.ActivitySettingBinding
import cn.copaint.audience.utils.AuthingUtils.biography
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast

class SettingActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this
    }


    fun onLogout(view: View) {
        val sharedPref = app.getSharedPreferences("Authing", Context.MODE_PRIVATE)
        sharedPref.edit().putString("token", "").apply()
        user = User(arn = "", id = "", userPoolId = "")
        biography = getText(R.string.un_give_biography).toString()
        toast("退出登录成功")
        startActivity(Intent(this, HomePageActivity::class.java))
    }

    fun onBackPress(view: View) {
        finish()
    }

    fun buyScallop(view: View) {
        val intent = Intent(this, PayActivity::class.java)
        startActivity(intent)
    }

    fun onWorkShareAccord(view: View) {
        toast("你点击了作品份额记录单")
    }
    fun onServiceAgreement(view: View) {
        toast("你点击了服务协议")
    }
    fun onPrivacyPolicy(view: View) {
        toast("你点击了隐私政策")
    }
}
