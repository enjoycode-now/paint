package cn.copaint.audience

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.ActivityPublishedWorkSecondBinding
import cn.copaint.audience.type.CreatePaintingInput
import cn.copaint.audience.type.FollowInfoInput
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.apollographql.apollo3.api.Optional
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.*
import java.lang.Exception

/**
 * 上传作品-第二步页
 */
class PublishedWorkSecondActivity : AppCompatActivity() {
    lateinit var bind: ActivityPublishedWorkSecondBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityPublishedWorkSecondBinding.inflate(layoutInflater)
        Bugsnag.start(this)
        setContentView(bind.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this

    }

    fun onMinusReleaseShareNum(view: View) {
        val currentNum = bind.shareEditText.text.toString().trimEnd('%').toInt()
        if (currentNum >= 1) {
            bind.shareEditText.text = (currentNum - 1).toString() + "%"
        }
    }

    fun onAddReleaseShareNum(view: View) {
        val currentNum = bind.shareEditText.text.toString().trimEnd('%').toInt()
        if (currentNum <= 99) {
            bind.shareEditText.text = (currentNum + 1).toString() + "%"
        }
    }

    fun onMinusEveryShareCost(view: View) {
        val currentNum = bind.priceEditText.text.toString().toInt()
        if (currentNum >= 1) {
            bind.priceEditText.text = (currentNum - 1).toString()
        }
    }

    fun onAddEveryShareCost(view: View) {
        val currentNum = bind.priceEditText.text.toString().toInt()
        bind.priceEditText.text = (currentNum + 1).toInt().toString()
    }

    fun onBackPress(view: View) {
        finish()
    }

    fun onSubmitBtn(view: View) {
        val workName = intent.getStringExtra("workName")
        val workIntroduction = intent.getStringExtra("workIntroduction")
        val coverPicUrlKey = intent.getStringExtra("coverPicUrlKey")
        val videoUrlKey = intent.getStringExtra("videoUrlKey")

        if (coverPicUrlKey.isNullOrEmpty()) {
            toast("封面还没上传")
            return
        }

        if (videoUrlKey.isNullOrEmpty()) {
            toast("视频还没上传")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient(this@PublishedWorkSecondActivity).mutation(
                    CreatePaintingMutation(
                        CreatePaintingInput(
                            workName ?: "",
                            workIntroduction ?: "",
                            Optional.Absent
                        ), coverPicUrlKey, videoUrlKey
                    )
                ).execute()
            } catch (e: Exception) {
                toast(e.toString())
                return@launch
            }
            runOnUiThread {
                toast("发布成功")
                Log.i("PublishedWorkSecondActivity", response.toString())
            }

        }

    }
}