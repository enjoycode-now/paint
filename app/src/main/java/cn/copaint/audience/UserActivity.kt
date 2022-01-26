package cn.copaint.audience

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
import cn.copaint.audience.adapter.SupportWorksAdapter
import cn.copaint.audience.databinding.ActivityUserBinding
import cn.copaint.audience.utils.AuthingUtils.authenticationClient
import cn.copaint.audience.utils.AuthingUtils.biography
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.utils.dp
import cn.copaint.audience.utils.getDigest
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Exception

class UserActivity : AppCompatActivity() {

    val sponsorList = mutableListOf<String>()
    private lateinit var binding: ActivityUserBinding
    val adapter = SupportWorksAdapter(this)
    val RESQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        setContentView(R.layout.activity_user)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        hignLightBtn(binding.userPageBtn)
        binding.supportWorksRecylerView.layoutManager = GridLayoutManager(this, 3)
        binding.supportWorksRecylerView.adapter = adapter
        val statusBarId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = if (statusBarId>0)resources.getDimensionPixelSize(statusBarId) else 24.dp
        binding.supportWorksRecylerView.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels - statusBarHeight - 96.dp
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
    }

    fun updateInfo() {
        runOnUiThread {
            binding.authorName.text = user.nickname
            binding.authorId.text = user.id.uppercase()
            val blockChainAddress = user.id.getDigest("SHA-256")
            val displayAddress = "0x"+blockChainAddress.replaceRange(8,blockChainAddress.length-8,"...").uppercase()
            binding.blockchainAddress.text = displayAddress
            binding.biography.text = biography
            try {
                Glide.with(this@UserActivity)
                    .load(user.photo)
                    .into(binding.userAvatar)
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
        }
    }

    fun hignLightBtn(view: View) {
        view as TextView
        binding.homePageBtn.isSelected = false
        binding.userPageBtn.isSelected = false
        binding.homePageBtn.setTextColor(Color.argb(0xcc,179, 179, 179))
        binding.userPageBtn.setTextColor(Color.argb(0xcc,179, 179, 179))
        view.isSelected = true
        view.setTextColor(Color.argb(255,0, 0, 0))
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
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("blockchainAddress", user.id.getDigest("SHA-256"))
        clipboardManager.setPrimaryClip(clipData)
        toast("区块链地址复制成功")
    }

    fun copyId(view: View) {
        val id: String = binding.authorId.text.replaceRange(0, 3, "").toString()
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("authorId", id)
        clipboardManager.setPrimaryClip(clipData)
        toast("用户ID复制成功")
    }
}