package cn.copaint.audience

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.databinding.ActivityPublishedWorkBinding
import cn.copaint.audience.utils.ToastUtils.app
import com.bugsnag.android.Bugsnag

class PublishedWorkActivity : AppCompatActivity() {
    lateinit var bind: ActivityPublishedWorkBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        bind = ActivityPublishedWorkBinding.inflate(layoutInflater)
        setContentView(bind.root)
        app = this
    }
}
