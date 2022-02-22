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
import cn.copaint.audience.databinding.ActivityHomePageBinding
import cn.copaint.audience.fragment.FollowFragment
import cn.copaint.audience.fragment.LiveFragment
import cn.copaint.audience.fragment.RecommendFragment
import cn.copaint.audience.utils.AuthingUtils.authenticationClient
import cn.copaint.audience.utils.AuthingUtils.loginCheck
import cn.copaint.audience.utils.AuthingUtils.update
import cn.copaint.audience.utils.BitmapUtils.picQueue
import cn.copaint.audience.utils.GrpcUtils.buildStub
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.bugsnag.android.Bugsnag
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomePageActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomePageBinding

    val fragmentList = mutableListOf(LiveFragment(), FollowFragment(), RecommendFragment())
    var lastBackPressedTimeMillis = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        buildStub()
        highLightBtn(binding.homePageBtn)
        binding.mainViewPager.apply {
            adapter = ScreenSlidePagerAdapter(this@HomePageActivity)
            setCurrentItem(2, false)
        }

        TabLayoutMediator(binding.tabLayout, binding.mainViewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "直播"
                1 -> tab.text = "关注"
                2 -> tab.text = "推荐"
            }
        }.attach()

        CoroutineScope(Dispatchers.Default).launch {
            authenticationClient.update()
            repeat(32) {
                picQueue.add("https://api.ghser.com/random/pe.php")
                delay(125)
            }
        }
    }

    fun onDrawActivity(view: View) {
        if (loginCheck()) startActivity(Intent(this, DrawActivity::class.java))
    }

    fun onMyWorksActivity(view: View) {
        if (loginCheck()) startActivity(Intent(this, MyWorksActivity::class.java))
    }

    private fun highLightBtn(view: View) {
        view as TextView
        binding.homePageBtn.isSelected = false
        binding.userPageBtn.isSelected = false
        binding.homePageBtn.setTextColor(Color.argb(80, 255, 255, 255))
        binding.userPageBtn.setTextColor(Color.argb(80, 255, 255, 255))
        view.isSelected = true
        view.setTextColor(Color.argb(255, 255, 255, 255))
    }

    fun onUserPage(view: View) {
        if (loginCheck()) {
            highLightBtn(binding.userPageBtn)
            binding.homePageBtn.isSelected = false
            binding.userPageBtn.isSelected = true
            startActivity(Intent(this, UserActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    override fun onBackPressed() {
        if ( System.currentTimeMillis() - lastBackPressedTimeMillis < 2000){
            super.onBackPressed()
        }else{
            toast("再按一次退出")
            lastBackPressedTimeMillis = System.currentTimeMillis()
        }
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
        override fun getItemCount() = 3

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }
}
