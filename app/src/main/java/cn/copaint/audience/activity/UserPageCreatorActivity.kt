package cn.copaint.audience.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.copaint.audience.FollowUserMutation
import cn.copaint.audience.R
import cn.copaint.audience.UnfollowUserMutation
import cn.copaint.audience.UserPageCreatorActivityInitQuery
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.ActivityUserPageCreatorBinding
import cn.copaint.audience.databinding.DialogCreatorMoreBinding
import cn.copaint.audience.type.FollowInfoInput
import cn.copaint.audience.type.FollowerWhereInput
import cn.copaint.audience.utils.*
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.viewmodel.UserCreatorViewModel
import com.apollographql.apollo3.api.Optional
import com.bugsnag.android.Bugsnag
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserPageCreatorActivity : BaseActivity() {
    lateinit var creatorId: String
    lateinit var binding: ActivityUserPageCreatorBinding
    var lastTimeMillis = 0L
    val userCreatorViewModel: UserCreatorViewModel by lazy {
        ViewModelProvider(this)[UserCreatorViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityUserPageCreatorBinding.inflate(layoutInflater)
        Bugsnag.start(this)
        setContentView(binding.root)
        app = this
        initView()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        StatusBarUtils.initSystemBar(window, "#dacdd8", true)
        // 当触摸的是TextView & 当前TextView可滚动时，则将事件交给TextView处理
        binding.biography.setOnTouchListener { v, event ->
            if (v == binding.biography && canVerticalScroll(v as EditText)) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                // 否则将事件交由其父类处理
                if (event.action == MotionEvent.ACTION_UP) {
                    v.parent.requestDisallowInterceptTouchEvent(false);
                }
            }
            false
        }

        val userPageCreatorDataObserver = Observer<UserPageCreatorActivityInitQuery.Data> {
            binding.followText.text = it.followInfo.followingCount.toString()
            binding.fansText.text = it.followInfo.followersCount.toString()
            binding.authorName.text = it.authingUsersInfo[0].nickname
            binding.authorId.text = it.authingUsersInfo[0].id
            binding.biography.setText(it.authingUsersInfo[0].biography)
            Glide.with(this@UserPageCreatorActivity).load(it.authingUsersInfo[0].photo)
                .into(binding.userAvatar)
        }
        val isFollowObserver = Observer<Boolean> {
            if (it) {
                binding.followBtn.text = "已关注"
                binding.followBtn.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_btn_unfollow, null)
            } else {
                binding.followBtn.text = "关注"
                binding.followBtn.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_btn_follow, null)
            }
        }
        userCreatorViewModel.userPageCreatorData.observe(this, userPageCreatorDataObserver)
        userCreatorViewModel.is_follow.observe(this, isFollowObserver)
    }

    override fun onResume() {
        super.onResume()
        updateUiInfo()
    }

    private fun updateUiInfo() {
        creatorId = intent.getStringExtra("creatorId") ?: ""
        if (creatorId.isNotBlank()) {
            userCreatorViewModel.askUserPageCreatorData(creatorId)
            val blockChainAddress = user.id.getDigest("SHA-256")
            val displayAddress =
                "0x" + blockChainAddress.replaceRange(
                    8,
                    blockChainAddress.length - 8,
                    "..."
                ).uppercase()
            binding.blockchainAddress.text = displayAddress
        } else {

        }
    }

    fun onFollows(view: View) {
        val intent = Intent(this, FollowsActivity::class.java)
        intent.putExtra("userId", creatorId)
        startActivity(intent)
    }

    fun onFans(view: View) {
        if (creatorId.isNotBlank()) {
            val intent = Intent(this, FansActivity::class.java)
            intent.putExtra("currentUserID", creatorId)
            startActivity(intent)
        }
    }

    fun editProfile(view: View) {
        startActivity(Intent(this, EditProfileActivity::class.java))
    }

    fun onSetting(view: View) {
        startActivity(Intent(this, SettingActivity::class.java))
    }

    fun buyScallop(view: View) {
        val intent = Intent(this, PayActivity::class.java)
        startActivity(intent)
    }

    fun copyAddress(view: View) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData =
            ClipData.newPlainText("blockchainAddress", user.id.getDigest("SHA-256"))
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

    fun onFollowAction(view: View) {

        if (System.currentTimeMillis() - lastTimeMillis < 1000) {
            toast("操作过于频繁，请慢一点")
            return
        } else {
            lastTimeMillis = System.currentTimeMillis()
        }

        when (userCreatorViewModel.is_follow.value) {
            true -> {
                userCreatorViewModel.unFollow(creatorId)
            }
            else -> {
                userCreatorViewModel.follow(creatorId)
            }
        }
    }


    fun onMoreBtn(view: View) {
        val popBind =
            DialogCreatorMoreBinding.inflate(LayoutInflater.from(this@UserPageCreatorActivity))

        // 弹出PopUpWindow
        val layerDetailWindow =
            PopupWindow(popBind.root, WindowManager.LayoutParams.MATCH_PARENT, 120.dp, true)
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
    }

    fun onBackBtn(view: View) {
        finish()
    }

    // 判断当前EditText是否可滚动
    private fun canVerticalScroll(text: EditText): Boolean {
        return text.lineCount > text.maxLines
    }

}
