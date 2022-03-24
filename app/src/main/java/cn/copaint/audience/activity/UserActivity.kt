package cn.copaint.audience.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import cn.copaint.audience.UserPageInitQuery
import cn.copaint.audience.adapter.SupportWorksAdapter
import cn.copaint.audience.databinding.ActivityUserBinding
import cn.copaint.audience.utils.*
import cn.copaint.audience.utils.AuthingUtils.biography
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.viewmodel.UserViewModel
import cn.copaint.audience.views.MyPhotoView
import com.apollographql.apollo3.api.ApolloResponse
import com.bugsnag.android.Bugsnag
import com.wanglu.photoviewerlibrary.PhotoViewer

class UserActivity : BaseActivity() {

    lateinit var binding: ActivityUserBinding
    var lastBackPressedTimeMillis = 0L
    val adapter = SupportWorksAdapter(this)
    val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityUserBinding.inflate(layoutInflater)
        StatusBarUtils.initSystemBar(window, "#dacdd8", false)
        setContentView(binding.root)
        app = this
        initView()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        val picUrlListObserver = Observer<ArrayList<String>> {
            adapter.notifyDataSetChanged()
            if (userViewModel.picUrlList.value?.size == 0) {
                binding.supportWorksRecyclerView.visibility = View.GONE
                binding.emptyView.emptyLayout.visibility = View.VISIBLE
            } else {
                binding.supportWorksRecyclerView.visibility = View.VISIBLE
                binding.emptyView.emptyLayout.visibility = View.GONE
            }
            binding.Works.text = "作品 ${it.size}"
        }
        val responseObserver = Observer<ApolloResponse<UserPageInitQuery.Data>> {
            binding.followText.text = it.data?.followInfo?.followingCount.toString()
            binding.fansText.text = it.data?.followInfo?.followersCount.toString()
        }
        userViewModel.picUrlList.observe(this, picUrlListObserver)
        userViewModel.response.observe(this, responseObserver)

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

        // 点击头像监听器
        binding.userAvatar.setOnClickListener { previewPhoto() }
        // 解决嵌套滑动冲突
        // 当触摸的是TextView & 当前TextView可滚动时，则将事件交给TextView处理
        binding.biography.setOnTouchListener { v, event ->
            if (v == binding.biography && canVerticalScroll(v as EditText)) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                // 否则将事件交由其父类处理
                if (event.action == MotionEvent.ACTION_UP) {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

        // 模拟用户的作品数据
        userViewModel.getUserWorksUrlList()
    }

    override fun onResume() {
        super.onResume()
        app = this
        updateUiInfo()
    }


    private fun updateUiInfo() {
        userViewModel.getUserInfo()

        binding.authorName.text = user.nickname
        binding.authorId.text = user.id.uppercase()
        val blockChainAddress = user.id.getDigest("SHA-256")
        val displayAddress =
            "0x" + blockChainAddress.replaceRange(8, blockChainAddress.length - 8, "...")
                .uppercase()
        binding.blockchainAddress.text = displayAddress
        binding.biography.setText(biography)
        user.photo?.let { GlideEngine.loadImage(this@UserActivity, it, binding.userAvatar) }
        binding.editProfileButton.isEnabled = true

    }


    fun editProfile(view: View) {
        startActivity(Intent(this, EditProfileActivity::class.java))
    }

    fun onSetting(view: View) {
        startActivity(Intent(this, SettingActivity::class.java))
    }

    fun onFollows(view: View) {
        val intent = Intent(this, FollowsActivity::class.java)
        intent.putExtra("userId", user.id)
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
        DialogUtils.onAddDialog(binding.root, this)
    }

    // 判断当前EditText是否可滚动
    private fun canVerticalScroll(text: EditText): Boolean {
        return text.lineCount > text.maxLines
    }

    fun onFans(view: View) {
        if (user.id.isNotBlank()) {
            val intent = Intent(this, FansActivity::class.java)
            intent.putExtra("currentUserID", user.id)
            startActivity(intent)
        }
    }

    fun onLikes(view: View) {
        toast("后端尚无接口")
    }

    fun previewPhoto() {
        if (!user.photo.isNullOrBlank()) {
            PhotoViewer.setClickSingleImg(
                user.photo!!,
                binding.userAvatar
            )   //因为本框架不参与加载图片，所以还是要写回调方法
                .setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
                    override fun show(iv: ImageView, url: String) {
                        GlideEngine.loadImage(this@UserActivity, url, iv)
                    }
                })
                .start(this)
        }
    }
}
