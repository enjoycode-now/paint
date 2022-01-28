package cn.copaint.audience

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.utils.ToastUtils.app

class PayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay)
        app = this
    }
}
