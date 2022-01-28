package cn.copaint.audience

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.authing.core.types.User
import cn.copaint.audience.databinding.ActivitySettingBinding
import cn.copaint.audience.utils.AuthingUtils.biography
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast

class SettingActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this
    }

    fun onFinish(view: View) = onBackPressed()

    fun onLogout(view: View) {
        val sharedPref = app.getSharedPreferences("Authing", Context.MODE_PRIVATE)
        sharedPref.edit().putString("token", "").commit()
        user = User(arn = "", id = "", userPoolId = "")
        biography = "这个人没有填简介啊"
        toast("退出登录成功")
        startActivity(Intent(this, HomePageActivity::class.java))
        finish()
    }
}
