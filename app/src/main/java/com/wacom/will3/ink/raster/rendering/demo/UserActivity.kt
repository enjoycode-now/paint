package com.wacom.will3.ink.raster.rendering.demo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bugsnag.android.Bugsnag
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.wacom.will3.ink.raster.rendering.demo.adapter.SupportWorksAdapter
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityUserBinding
import android.graphics.PixelFormat

import android.graphics.drawable.Drawable

class UserActivity : AppCompatActivity() {


    val supportWorksList = mutableListOf<Bitmap>()
    private lateinit var  binding : ActivityUserBinding
    val adapter = SupportWorksAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        setContentView(R.layout.activity_user)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        simulateData()
        binding.supportWorksRecylerView.layoutManager = GridLayoutManager(this,3)
        binding.supportWorksRecylerView.adapter = adapter
    }


    fun simulateData(){
        for (i in 1..10){
            val drawable = ContextCompat.getDrawable(this, R.drawable.ic_copy_link);
            if(drawable !=null){
                var bitmap = drawableToBitmap(drawable)
                if (bitmap!=null)
                supportWorksList.add(bitmap)
            }

        }
    }

    //将drawable转为bitmap
    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        //取drawable的宽高
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        //取drawable的颜色格式
        val config =
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        //创建对应的bitmap
        val bitmap = Bitmap.createBitmap(width, height, config)
        //创建对应的bitmap的画布
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        //把drawable内容画到画布中
        drawable.draw(canvas)
        return bitmap
    }
}