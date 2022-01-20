package com.wacom.will3.ink.raster.rendering.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.uitest.LiveFragment
import com.example.uitest.RecommendFragment
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityHomePageBinding
import kotlinx.android.synthetic.main.activity_home_page.*

class HomePageActivity : FragmentActivity() {
    lateinit var binding : ActivityHomePageBinding

    val max_bound = 3

    var current_positon = 2

    companion object{
        //数据源，填充部分初始数据
        var photoUri = "https://api.ghser.com/random/pe.php"
        val fragmentList = mutableListOf<Fragment>(
            LiveFragment(photoUri)
            ,LiveFragment(photoUri)
            ,LiveFragment(photoUri))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainViewPager.apply {
            adapter = ScreenSlidePagerAdapter(this@HomePageActivity)
            setCurrentItem(1,false)
        }



    }




    private inner class ScreenSlidePagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
        override fun getItemCount() = Int.MAX_VALUE

        override fun createFragment(position: Int): Fragment {

            if (position == fragmentList.size-1){
                fragmentList.add(LiveFragment(photoUri))
            }



            return fragmentList[position]
        }

    }
}