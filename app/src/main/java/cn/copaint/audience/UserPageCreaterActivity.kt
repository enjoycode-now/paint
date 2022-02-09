package cn.copaint.audience

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.databinding.ActivityUserPageCreaterBinding
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.ToastUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.getDigest
import com.bugsnag.android.Bugsnag

class UserPageCreaterActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserPageCreaterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserPageCreaterBinding.inflate(layoutInflater)
        Bugsnag.start(this)
        setContentView(binding.root)
        app = this
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
        val clipData = ClipData.newPlainText("blockchainAddress", AuthingUtils.user.id.getDigest("SHA-256"))
        clipboardManager.setPrimaryClip(clipData)
        ToastUtils.toast("区块链地址复制成功")
    }

    fun copyId(view: View) {
        val id: String = binding.authorId.text.replaceRange(0, 3, "").toString()
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("authorId", id)
        clipboardManager.setPrimaryClip(clipData)
        ToastUtils.toast("用户ID复制成功")
    }
}
