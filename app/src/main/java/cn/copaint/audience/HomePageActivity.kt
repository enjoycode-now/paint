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
import com.example.uitest.FollowFragment
import cn.copaint.audience.databinding.ActivityHomePageBinding
import cn.copaint.audience.fragment.LiveFragment
import cn.copaint.audience.fragment.RecommendFragment
import cn.copaint.audience.utils.ToastUtils.app
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.item_support_works.*

class HomePageActivity : AppCompatActivity() {

    lateinit var binding : ActivityHomePageBinding
    val photoUri = "https://api.ghser.com/random/pe.php"
    val fragmentList = mutableListOf<Fragment>(FollowFragment(),LiveFragment(photoUri),RecommendFragment())

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
    }

    fun highLightBtn(view: View){
        view as TextView
        binding.homePageBtn.isSelected = false
        binding.userPageBtn.isSelected = false
        binding.homePageBtn.setTextColor(Color.rgb(179,179,179))
        binding.userPageBtn.setTextColor(Color.rgb(179,179,179))
        view.isSelected = true
        view.setTextColor(Color.rgb(255,255,255))
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