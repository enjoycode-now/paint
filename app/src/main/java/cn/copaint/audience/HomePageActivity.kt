package cn.copaint.audience

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import cn.copaint.audience.fragment.FollowFragment
import cn.copaint.audience.databinding.ActivityHomePageBinding
import cn.copaint.audience.fragment.LiveFragment
import cn.copaint.audience.fragment.RecommendFragment
import cn.copaint.audience.utils.BitmapUtils.picQueue
import cn.copaint.audience.utils.ToastUtils.app
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.item_support_works.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomePageActivity : AppCompatActivity() {

    lateinit var binding : ActivityHomePageBinding

    val fragmentList = mutableListOf(FollowFragment(),LiveFragment(),RecommendFragment())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        highLightBtn(binding.homePageBtn)
        binding.mainViewPager.apply {
            adapter = ScreenSlidePagerAdapter(this@HomePageActivity)
            setCurrentItem(1,false)
        }


        TabLayoutMediator(binding.tabLayout, binding.mainViewPager) { tab, position ->
            when(position){
                0->tab.text="关注"
                1->tab.text="直播"
                2->tab.text="推荐"
            }
        }.attach()

        CoroutineScope(Dispatchers.Default).launch {
            repeat(32){
                picQueue.add("https://api.ghser.com/random/pe.php")
                delay(125)
            }
        }
    }

    fun highLightBtn(view: View){
        view as TextView
        binding.homePageBtn.isSelected = false
        binding.userPageBtn.isSelected = false
        binding.homePageBtn.setTextColor(Color.argb(80,255,255,255))
        binding.userPageBtn.setTextColor(Color.argb(80,255,255,255))
        view.isSelected = true
        view.setTextColor(Color.argb(255,255,255,255))
    }

    fun onUserPage(view: View){
        highLightBtn(binding.userPageBtn)
        binding.homePageBtn.isSelected = false
        binding.userPageBtn.isSelected = true
        startActivity(Intent(this,UserActivity::class.java))
        overridePendingTransition(0,0)
        finish()
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
        override fun getItemCount() = 3

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }
}