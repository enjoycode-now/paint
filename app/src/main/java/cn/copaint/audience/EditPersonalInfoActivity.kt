package cn.copaint.audience

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.copaint.audience.utils.ToastUtils.app

class EditPersonalInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_personal_info)
        app = this
    }
}