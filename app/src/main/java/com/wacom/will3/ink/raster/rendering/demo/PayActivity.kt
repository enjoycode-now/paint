package com.wacom.will3.ink.raster.rendering.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class PayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay)

    }

    fun onFinish(view: View) {
        finish()
    }
}