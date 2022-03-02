package cn.copaint.audience

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import cn.copaint.audience.adapter.SupportWorksAdapter
import cn.copaint.audience.databinding.ActivityUserBinding
import cn.copaint.audience.databinding.DialogHomepageAddBinding
import cn.copaint.audience.type.FollowInfoInput
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.AuthingUtils.biography
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.utils.dp
import cn.copaint.audience.utils.getDigest
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.bugsnag.android.Bugsnag
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.math.abs
import kotlin.random.Random

class UserActivity : AppCompatActivity() {

    val sponsorList = mutableListOf<String>()
    lateinit var binding: ActivityUserBinding
    var lastBackPressedTimeMillis = 0L
    val adapter = SupportWorksAdapter(this)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityUserBinding.inflate(layoutInflater)
        StatusBarUtils.initSystemBar(window,"#dacdd8",false)
        setContentView(binding.root)
        app = this

        highLightBth(binding.userPageBtn)
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

        // 解决嵌套滑动冲突
        // 当触摸的是TextView & 当前TextView可滚动时，则将事件交给TextView处理
        binding.biography.setOnTouchListener { v, event ->
            if(v == binding.biography && canVerticalScroll(v as EditText)){
                v.getParent().requestDisallowInterceptTouchEvent(true);
                // 否则将事件交由其父类处理
                if (event.action == MotionEvent.ACTION_UP) {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

    }

    override fun onResume() {
        super.onResume()
        updateUiInfo()
        // 模拟用户的作品数据
        CoroutineScope(Dispatchers.Default).launch {
            val count: Int = abs(Random(System.currentTimeMillis()).nextInt())%10
            for (i in 0..count) {
                sponsorList.add("https://api.ghser.com/random/pe.php")
                delay(125)
                runOnUiThread { adapter.notifyItemChanged(i) }
            }
            if(sponsorList.size == 0){
                binding.supportWorksRecyclerView.visibility = View.GONE
                binding.emptyView.emptyLayout.visibility = View.VISIBLE
            }else{
                binding.supportWorksRecyclerView.visibility = View.VISIBLE
                binding.emptyView.emptyLayout.visibility = View.GONE
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
                val response = try {
                    apolloclient.query(
                        UserPage_InitQuery(
                            input = Optional.presentIfNotNull(
                                FollowInfoInput(userID = user.id)
                            )
                        )
                    ).execute()
                } catch (e: Exception){
                    toast(e.toString())
                    return@launch
                }
                runOnUiThread {
                    binding.moneyText.text = response.data?.wallet?.balance.toString()
                    binding.followingText.text =
                        response.data?.followInfo?.followingCount.toString()
                }

            }
            binding.authorName.text = user.nickname
            binding.authorId.text = user.id.uppercase()
            val blockChainAddress = user.id.getDigest("SHA-256")
            val displayAddress =
                "0x" + blockChainAddress.replaceRange(8, blockChainAddress.length - 8, "...")
                    .uppercase()
            binding.blockchainAddress.text = displayAddress
            binding.biography.setText(biography)
            Glide.with(this@UserActivity).load(user.photo).into(binding.userAvatar)
            binding.editProfileButton.isEnabled = true
        }
    }

    private fun highLightBth(view: View) {
        view as TextView
        binding.homePageBtn.isSelected = false
        binding.userPageBtn.isSelected = false
        binding.playground.isSelected = false
        binding.message.isSelected = false
        binding.homePageBtn.setTextColor(Color.parseColor("#B3B3B3"))
        binding.userPageBtn.setTextColor(Color.parseColor("#B3B3B3"))
        binding.playground.setTextColor(Color.parseColor("#B3B3B3"))
        binding.message.setTextColor(Color.parseColor("#B3B3B3"))
        view.isSelected = true
        view.setTextColor(Color.parseColor("#333333"))
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
        highLightBth(binding.homePageBtn)
        startActivity(Intent(this, HomePageActivity::class.java))
        overridePendingTransition(0, 0)
        finish()
    }

    fun onMessage(view: View) {
        highLightBth(view)
    }

    fun onPlayground(view: View) {
        highLightBth(view)
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

    override fun onBackPressed() {
        if (System.currentTimeMillis() - lastBackPressedTimeMillis < 2000) {
            super.onBackPressed()
        } else {
            toast("再按一次退出")
            lastBackPressedTimeMillis = System.currentTimeMillis()
        }
    }

    fun onAddDialog(view: View) {
        val popBind = DialogHomepageAddBinding.inflate(LayoutInflater.from(this))

        // 弹出PopUpWindow
        val layerDetailWindow =
            PopupWindow(popBind.root, WindowManager.LayoutParams.MATCH_PARENT, 250.dp, true)
        layerDetailWindow.isOutsideTouchable = true

        // 设置弹窗时背景变暗
        var layoutParams = window.attributes
        layoutParams.alpha = 0.4f // 设置透明度
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams

        // 弹窗消失时背景恢复
        layerDetailWindow.setOnDismissListener {
            layoutParams = window.attributes
            layoutParams.alpha = 1f
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.attributes = layoutParams
        }

        layerDetailWindow.showAtLocation(binding.root, Gravity.BOTTOM, 0, 0)

        popBind.uploadWorkBtn.setOnClickListener {
//            startActivity(Intent(this,UpLoadWorkActivity::class.java))
            layerDetailWindow.dismiss()
        }
        popBind.publishRequirementBtn.setOnClickListener {
            startActivity(Intent(this, PublishRequirementActivity::class.java))
            layerDetailWindow.dismiss()
        }
        popBind.closeBtn.setOnClickListener {
            layerDetailWindow.dismiss()
        }
        popBind.root.setOnClickListener {
            layerDetailWindow.dismiss()
        }
    }

    // 判断当前EditText是否可滚动
    private fun canVerticalScroll(text: EditText): Boolean {
        return text.lineCount > text.maxLines
    }
}
