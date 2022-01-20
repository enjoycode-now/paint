package com.wacom.will3.ink.raster.rendering.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.authing.core.types.User
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivitySettingBinding
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.biography
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.user
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.app
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.toast

class SettingActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this
    }

    override fun onBackPressed() {
        startActivity(Intent(this,UserActivity::class.java))
        finish()
    }

    fun onFinish(view: View) = onBackPressed()

    fun onLogout(view: View){
        val sharedPref = app.getSharedPreferences("Authing", Context.MODE_PRIVATE)
        sharedPref.edit().putString("token", "").commit()
        user = User(arn = "",id="", userPoolId ="")
        biography = "这个人没有填简介啊"
        toast("退出登录成功")
        startActivity(Intent(this,HomePageActivity::class.java))
        finish()
    }
}