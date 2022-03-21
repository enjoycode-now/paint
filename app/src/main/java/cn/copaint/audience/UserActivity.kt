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
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.ActivityUserBinding
import cn.copaint.audience.databinding.DialogHomepageAddBinding
import cn.copaint.audience.type.FollowInfoInput
import cn.copaint.audience.utils.*
import cn.copaint.audience.utils.AuthingUtils.biography
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.bugsnag.android.Bugsnag
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
        Log.i("user_token", user.token?:"")
    }

    override fun onResume() {
        super.onResume()
        updateUiInfo()
    }



    private fun updateUiInfo() {
        runOnUiThread {

            CoroutineScope(Dispatchers.IO).launch {
                val response = try {
                    apolloClient(this@UserActivity).query(
                        UserPageInitQuery(
                            input =
                                FollowInfoInput(userID = user.id ?:"")
                        )
                    ).execute()
                } catch (e: Exception){
                    toast(e.toString())
                    return@launch
                }
                runOnUiThread {
                    binding.followText.text = response.data?.followInfo?.followingCount.toString()
                    binding.fansText.text = response.data?.followInfo?.followersCount.toString()
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
            user.photo?.let { GlideEngine.loadImage(this@UserActivity, it,binding.userAvatar) }
            binding.editProfileButton.isEnabled = true
        }
    }



    fun editProfile(view: View) {
        startActivity(Intent(this, EditProfileActivity::class.java))
    }

    fun onSetting(view: View) {
        startActivity(Intent(this, SettingActivity::class.java))
    }

    fun onFollows(view: View) {
        val intent = Intent(this, FollowsActivity::class.java)
        intent.putExtra("userId",user.id)
        startActivity(intent)
    }

    fun onHomePage(view: View) {
        startActivity(Intent(this, HomePageActivity::class.java))
        overridePendingTransition(0, 0)
    }

    fun onMessage(view: View) {
        startActivity(Intent(this, SquareActivity::class.java))
        overridePendingTransition(0, 0)
    }

    fun onSquare(view: View) {
        startActivity(Intent(this, SquareActivity::class.java))
        overridePendingTransition(0, 0)
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
        DialogUtils.onAddDialog(binding.root,this)
    }

    // 判断当前EditText是否可滚动
    private fun canVerticalScroll(text: EditText): Boolean {
        return text.lineCount > text.maxLines
    }

    fun onFans(view: View) {
        if (user.id.isNotBlank()){
            val intent = Intent(this, FansActivity::class.java)
            intent.putExtra("currentUserID",user.id)
            startActivity(intent)
        }
    }
    fun onLikes(view: View) {
        toast("后端尚无接口")
    }
}
