package cn.copaint.audience

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.copaint.audience.utils.ToastUtils.app


class PayActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay)
        app = this
    }
}