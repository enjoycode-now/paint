package com.wacom.will3.ink.raster.rendering.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivitySettingBinding
import com.wacom.will3.ink.raster.rendering.demo.databinding.FragmentLiveBinding

class SettingActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun onclick(view: android.view.View) {
        when(view.id){
            binding.logoutBtn.id ->{

            }
        }

    }
}