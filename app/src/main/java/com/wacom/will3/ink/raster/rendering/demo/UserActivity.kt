package com.wacom.will3.ink.raster.rendering.demo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import cn.authing.core.graphql.GraphQLException
import com.bugsnag.android.Bugsnag
import com.bumptech.glide.Glide
import com.wacom.will3.ink.raster.rendering.demo.adapter.SupportWorksAdapter
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityUserBinding
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.authenticationClient
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.biography
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.user
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.app
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.toast
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Exception

class UserActivity : AppCompatActivity() {

    val sponsorList = mutableListOf<String>()
    private lateinit var binding: ActivityUserBinding
    val adapter = SupportWorksAdapter(this)
    val RESQUEST_CODE = 1
    lateinit var job: Job

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        setContentView(R.layout.activity_user)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hignLightBtn(binding.myPageBtn)
        binding.supportWorksRecylerView.layoutManager = GridLayoutManager(this, 3)
        binding.supportWorksRecylerView.adapter = adapter
        binding.supportWorksRecylerView.layoutParams.height =
            Resources.getSystem().displayMetrics.heightPixels
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            updateInfo()
            val sharedPref =
                app.getSharedPreferences("Authing", Context.MODE_PRIVATE) ?: return@launch
            authenticationClient.token = sharedPref.getString("token", "") ?: ""
            try {
                user = authenticationClient.getCurrentUser().execute()
            } catch (e: GraphQLException) {
                runOnUiThread { startActivity(Intent(app, LoginActivity::class.java)) }
                return@launch
            } catch (e: IOException) {
                toast("用户信息获取失败")
            }
            biography =
                (authenticationClient.getUdfValue().execute()["biography"] ?: "这个人没有填简介啊") as String
            updateInfo()

            // 应援记录数据
            CoroutineScope(Dispatchers.Default).launch {
                for (i in 0..31) {
                    sponsorList.add("https://api.ghser.com/random/pe.php")
                    delay(125)
                    runOnUiThread { adapter.notifyItemChanged(i) }
                }
            }
        }

        job = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(250)
                runOnUiThread {
                    with(binding) {
                        if (supportWorksRecylerView.canScrollVertically((-1).dp)) {
                            if (sponsorNote.visibility == View.GONE) crossShow()
                        } else {
                            if (sponsorNote.visibility == View.VISIBLE) crossFade()
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        job.cancel()
    }

    private fun crossShow() {
        binding.sponsorNote.apply {
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            alpha = 0f
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                .alpha(1f)
                .setDuration(200)
                .setListener(null)
        }
    }

    private fun crossFade() {
        binding.sponsorNote.animate()
            .alpha(0f)
            .setDuration(200)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.sponsorNote.visibility = View.GONE
                }
            })
    }

    fun updateInfo() {
        runOnUiThread {
            binding.authorName.text = user.nickname
            binding.authorId.text = "ID:${user.id}"
            binding.biography.text = biography
            try {
                Glide.with(this@UserActivity)
                    .load(user.photo)
                    .into(binding.userAvatar)
                Glide.with(this@UserActivity)
                    .load(user.photo)
                    .into(binding.smallAvatar)
            } catch (e: Exception) {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESQUEST_CODE && resultCode == RESULT_OK) {
            Glide.with(this)
                .load(data?.data)
                .into(binding.userAvatar)
            Glide.with(this@UserActivity)
                .load(data?.data)
                .into(binding.smallAvatar)
        }
    }

    fun hignLightBtn(view: View) {
        view as TextView
        binding.homePageBtn.isSelected = false
        binding.myPageBtn.isSelected = false
        binding.homePageBtn.setTextColor(Color.rgb(179, 179, 179))
        binding.myPageBtn.setTextColor(Color.rgb(179, 179, 179))
        view.isSelected = true
        view.setTextColor(Color.rgb(255, 255, 255))
    }

    fun onChangeAvatar(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, RESQUEST_CODE)
    }

    fun onSetting(view: View) {
        startActivity(Intent(this, SettingActivity::class.java))
    }

    fun onHomePage(view: View) {
        hignLightBtn(binding.homePageBtn)
        startActivity(Intent(this, HomePageActivity::class.java))
        overridePendingTransition(0, 0)
        finish()
    }

    fun buyScallop(view: View) {
        val intent = Intent(this, PayActivity::class.java)
        startActivity(intent)
    }

    fun copyAddress(view: View) {
        val address: String = binding.blockchainAddress.text.replaceRange(0, 6, "").toString()
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("blockchainAddress", address)
        clipboardManager.setPrimaryClip(clipData)
        toast("地址复制成功")
    }

    fun copyId(view: View) {
        val id: String = binding.authorId.text.replaceRange(0, 3, "").toString()
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("authorId", id)
        clipboardManager.setPrimaryClip(clipData)
        toast("ID复制成功")
    }
}