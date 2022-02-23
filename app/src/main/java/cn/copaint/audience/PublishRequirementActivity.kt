package cn.copaint.audience

import android.R
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.databinding.ActivityPublishRequirementBinding
import cn.copaint.audience.utils.StatusBarUtils


class PublishRequirementActivity : AppCompatActivity() {
    lateinit var binding: ActivityPublishRequirementBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublishRequirementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        StatusBarUtils.initSystemBar(window,"#FAFBFF",true)
    }

    fun onBackPress(view: View) {
        finish()
    }



}