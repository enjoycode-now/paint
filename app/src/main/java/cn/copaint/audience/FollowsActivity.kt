package cn.copaint.audience

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.adapter.FollowAdapter
import cn.copaint.audience.databinding.ActivityFollowsBinding
import cn.copaint.audience.model.Follow
import cn.copaint.audience.utils.ToastUtils.app
import com.bugsnag.android.Bugsnag

class FollowsActivity : AppCompatActivity() {
    lateinit var binding: ActivityFollowsBinding
    val followList = listOf(
        Follow(
            "美绪",
            "https://c-ssl.duitang.com/uploads/item/201709/21/20170921092830_C4URf.thumb.700_0.jpeg",
            "1"
        ),
        Follow(
            "はかせ",
            "https://c-ssl.duitang.com/uploads/item/201709/24/20170924174109_s8TFR.jpeg",
            "1"
        )
    )
    val followAdapter = FollowAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityFollowsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        binding.followRecycle.layoutManager = LinearLayoutManager(this)
        binding.followRecycle.adapter = followAdapter
    }

    fun onBackPress(view: View) = onBackPressed()
}
