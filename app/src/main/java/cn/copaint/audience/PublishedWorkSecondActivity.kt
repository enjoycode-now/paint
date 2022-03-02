package cn.copaint.audience

import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.databinding.ActivityPublishedWorkSecondBinding
import kotlinx.coroutines.*

/**
 * 上传作品-第二步页
 */
class PublishedWorkSecondActivity : AppCompatActivity() {
    lateinit var bind: ActivityPublishedWorkSecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityPublishedWorkSecondBinding.inflate(layoutInflater)
        setContentView(bind.root)
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
}