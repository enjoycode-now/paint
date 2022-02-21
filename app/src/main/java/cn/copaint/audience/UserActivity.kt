package cn.copaint.audience

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import cn.copaint.audience.adapter.SupportWorksAdapter
import cn.copaint.audience.databinding.ActivityUserBinding
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.AuthingUtils.biography
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.utils.dp
import cn.copaint.audience.utils.getDigest
import com.apollographql.apollo3.ApolloClient
import com.bugsnag.android.Bugsnag
import com.bumptech.glide.Glide
import kotlinx.coroutines.*

class UserActivity : AppCompatActivity() {

    val sponsorList = mutableListOf<String>()
    private lateinit var binding: ActivityUserBinding
    val adapter = SupportWorksAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        hignLightBtn(binding.userPageBtn)
        binding.supportWorksRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.supportWorksRecyclerView.adapter = adapter
        val statusBarId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight =
            if (statusBarId > 0) resources.getDimensionPixelSize(statusBarId) else 24.dp
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        binding.supportWorksRecyclerView.layoutParams.height =
            screenHeight - statusBarHeight - 96.dp
    }


    override fun onResume() {
        super.onResume()
        updateUiInfo()
        // 应援记录数据
        CoroutineScope(Dispatchers.Default).launch {
            for (i in 0..31) {
                sponsorList.add("https://api.ghser.com/random/pe.php")
                delay(125)
                runOnUiThread { adapter.notifyItemChanged(i) }
            }
        }
    }

    private fun updateUiInfo() {
        runOnUiThread {
            val apolloclient = ApolloClient.Builder()
                .serverUrl("http://120.78.173.15:20000/query")
                .addHttpHeader("Authorization", "Bearer " + AuthingUtils.user.token!!)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                val response = apolloclient.query(UserPage_InitQuery()).execute()
                runOnUiThread {
                    binding.moneyText.text = response.data?.wallet?.balance.toString()
                    binding.fansText.text = response.data?.followers?.totalCount.toString()
                }

            }
            binding.authorName.text = user.nickname
            binding.authorId.text = user.id.uppercase()
            val blockChainAddress = user.id.getDigest("SHA-256")
            val displayAddress =
                "0x" + blockChainAddress.replaceRange(8, blockChainAddress.length - 8, "...")
                    .uppercase()
            binding.blockchainAddress.text = displayAddress
            binding.biography.text = biography
            Glide.with(this@UserActivity).load(user.photo).into(binding.userAvatar)
            binding.editProfileButton.isEnabled = true
        }
    }

    private fun hignLightBtn(view: View) {
        view as TextView
        binding.homePageBtn.isSelected = false
        binding.userPageBtn.isSelected = false
        binding.homePageBtn.setTextColor(Color.argb(0xcc, 179, 179, 179))
        binding.userPageBtn.setTextColor(Color.argb(0xcc, 179, 179, 179))
        view.isSelected = true
        view.setTextColor(Color.argb(255, 0, 0, 0))
    }

    fun editProfile(view: View) {
        startActivity(Intent(this, EditProfileActivity::class.java))
    }

    fun onSetting(view: View) {
        startActivity(Intent(this, SettingActivity::class.java))
    }

    fun onFollows(view: View) {
        startActivity(Intent(this, FollowsActivity::class.java))
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
