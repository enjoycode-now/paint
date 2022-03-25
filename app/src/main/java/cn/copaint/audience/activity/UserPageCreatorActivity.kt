package cn.copaint.audience.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
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
import com.wacom.ink.utils.Bounds
import com.wanglu.photoviewerlibrary.PhotoViewer
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

    override fun initView() {
        StatusBarUtils.initSystemBar(window, "#dacdd8", true)
        creatorId = intent.getStringExtra("creatorId") ?: ""
        if (creatorId.isNotBlank()) {
            userCreatorViewModel.askUserPageCreatorData(creatorId)
            val blockChainAddress = creatorId.getDigest("SHA-256")
            val displayAddress =
                "0x" + blockChainAddress.replaceRange(
                    8,
                    blockChainAddress.length - 8,
                    "..."
                ).uppercase()
            binding.blockchainAddress.text = displayAddress
        } else {
            toast("error:creatorId is null")
        }

        if (creatorId == user.id) {
            binding.followBtn.visibility = View.GONE
        }

        binding.biography.setOnClickListener {
            if (binding.biography.ellipsize == null) {
                binding.biography.ellipsize = TextUtils.TruncateAt.END
                binding.biography.setLines(3)
                val drawable =
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_expand_textview, null)
                drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumWidth)
                binding.biography.setCompoundDrawablesRelative(null, null, null, drawable)
            } else {
                binding.biography.ellipsize = null
                binding.biography.isSingleLine = false
                val drawable =
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_close_textview, null)
                drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumWidth)
                binding.biography.setCompoundDrawablesRelative(null, null, null, drawable)
            }
        }
        binding.biography.setOnLongClickListener {
            copyBiography(it as TextView)
            true
        }
        binding.userAvatar.setOnClickListener {
            userCreatorViewModel.userPageCreatorData.value?.authingUsersInfo?.get(0)?.photo.let {
                PhotoViewer.setClickSingleImg(
                    it ?: "",
                    binding.userAvatar
                )   //因为本框架不参与加载图片，所以还是要写回调方法
                    .setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
                        override fun show(iv: ImageView, url: String) {
                            GlideEngine.loadImage(this@UserPageCreatorActivity, url, iv)
                        }
                    })
                    .start(this)
            }
        }

        val userPageCreatorDataObserver = Observer<UserPageCreatorActivityInitQuery.Data> {
            binding.followText.text = it.followInfo.followingCount.toString()
            binding.fansText.text = it.followInfo.followersCount.toString()
            binding.authorName.text =
                if (it.authingUsersInfo[0].nickname.isNullOrBlank()) resources.getString(R.string.un_give_name)
                else it.authingUsersInfo[0].nickname
            binding.authorId.text = it.authingUsersInfo[0].id
            binding.biography.text =
                if (it.authingUsersInfo[0].biography.isNullOrBlank()) resources.getString(R.string.un_give_biography) else it.authingUsersInfo[0].biography
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

    fun copyBiography(view: TextView) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("biography", view.text)
        clipboardManager.setPrimaryClip(clipData)
        toast("用户个性签名复制成功")
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
