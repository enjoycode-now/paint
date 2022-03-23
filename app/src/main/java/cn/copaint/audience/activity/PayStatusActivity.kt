package cn.copaint.audience.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import cn.copaint.audience.databinding.ActivityPayStatusBinding

class PayStatusActivity : AppCompatActivity() {

    lateinit var binding: ActivityPayStatusBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chronometer.setOnChronometerTickListener {
            if (it.base - SystemClock.elapsedRealtime() <= 0){
                it.stop()
                // SquareActivity的启动模式是singleTask
                startActivity(Intent(this, SquareActivity::class.java))
            }
        }

        binding.chronometer.base = SystemClock.elapsedRealtime()+5000
        binding.chronometer.format = "支付成功！%s秒后自动跳转"
        binding.chronometer.start()

    }

    override fun onResume() {
        super.onResume()
        binding.lottieView.playAnimation()
    }



    override fun onBackPressed() {
        super.onBackPressed()
        onBackPress(binding.root)
    }


    override fun onStop() {
        super.onStop()
        binding.chronometer.stop()
    }

    fun onBackPress(view: View) {
        binding.chronometer.stop()
        startActivity(Intent(this, SquareActivity::class.java))
    }
}