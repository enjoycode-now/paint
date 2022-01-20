package com.wacom.will3.ink.raster.rendering.demo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityHomePageBinding
import com.wacom.will3.ink.raster.rendering.demo.fragment.LiveFragment
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.app
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.activity_main.view.*

class HomePageActivity : AppCompatActivity() {
    lateinit var binding : ActivityHomePageBinding
    val photoUri = "https://api.ghser.com/random/pe.php"
    val picList = mutableListOf(LiveFragment(photoUri),LiveFragment(photoUri),LiveFragment(photoUri))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        highLightBtn(binding.homePageBtn)
        binding.mainViewPager.apply {
            offscreenPageLimit=2
            adapter = ScreenSlidePagerAdapter(this@HomePageActivity)
        }
    }

    fun highLightBtn(view: View){
        view as TextView
        binding.homePageBtn.isSelected = false
        binding.myPageBtn.isSelected = false
        binding.homePageBtn.setTextColor(Color.rgb(179,179,179))
        binding.myPageBtn.setTextColor(Color.rgb(179,179,179))
        view.isSelected = true
        view.setTextColor(Color.rgb(255,255,255))
    }

    fun onUserPage(view: View){
        highLightBtn(binding.myPageBtn)
        binding.homePageBtn.isSelected = false
        binding.myPageBtn.isSelected = true
        startActivity(Intent(this,UserActivity::class.java))
        overridePendingTransition(0,0)
        finish()
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
        override fun getItemCount() = Int.MAX_VALUE

        override fun createFragment(position: Int): Fragment {
            while (position>picList.lastIndex-3)picList.add(LiveFragment(photoUri))
            return picList[position]
        }
    }
}