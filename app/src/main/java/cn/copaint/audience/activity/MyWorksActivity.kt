package cn.copaint.audience.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import cn.copaint.audience.adapter.MyWorksAdapter
import cn.copaint.audience.databinding.ActivityMyWorksBinding
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.ToastUtils.app
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyWorksActivity : AppCompatActivity() {
    lateinit var binding: ActivityMyWorksBinding
    val adapter = MyWorksAdapter(this)
    val myWorksList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityMyWorksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        binding.myWorksRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.myWorksRecyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // 应援记录数据
        CoroutineScope(Dispatchers.Default).launch {
            for (i in 0..31) {
                myWorksList.add("https://api.ghser.com/random/pe.php")
                delay(125)
                runOnUiThread { adapter.notifyItemChanged(i) }
            }
        }
    }

    fun onPublishedWorkActivity(view: View) {
        if (AuthingUtils.loginCheck()) startActivity(Intent(this, PublishedWorkActivity::class.java))
    }

    fun onUserPageCreatorActivity(view: View) {
        if (AuthingUtils.loginCheck()) startActivity(Intent(this, UserPageCreatorActivity::class.java))
    }
}
