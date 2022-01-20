package com.wacom.will3.ink.raster.rendering.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityHomePageBinding
import com.wacom.will3.ink.raster.rendering.demo.fragment.LiveFragment
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.app
import kotlinx.android.synthetic.main.activity_home_page.*

class HomePageActivity : AppCompatActivity() {
    lateinit var binding : ActivityHomePageBinding
    val photoUri = "https://api.ghser.com/random/pe.php"
    val picList = mutableListOf(LiveFragment(photoUri),LiveFragment(photoUri),LiveFragment(photoUri))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        binding.homePageBtn.isSelected = true
        binding.myPageBtn.isSelected = false
        binding.mainViewPager.apply {
            offscreenPageLimit=2
            adapter = ScreenSlidePagerAdapter(this@HomePageActivity)
        }
    }

    fun onUserPage(view: View){

        binding.homePageBtn.isSelected = false
        binding.myPageBtn.isSelected = true
        startActivity(Intent(this,UserActivity::class.java))
        overridePendingTransition(0,0)
        finish()
    }

    fun onHomePage(view:View){
        binding.homePageBtn.isSelected = true
        binding.myPageBtn.isSelected = false
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
        override fun getItemCount() = Int.MAX_VALUE

        override fun createFragment(position: Int): Fragment {
            while (position>picList.lastIndex-3)picList.add(LiveFragment(photoUri))
            return picList[position]
        }
    }
}