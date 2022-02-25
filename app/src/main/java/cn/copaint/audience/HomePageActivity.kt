package cn.copaint.audience

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.OverScroller
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.copaint.audience.databinding.ActivityHomePageBinding
import cn.copaint.audience.databinding.DialogHomepageAddBinding
import cn.copaint.audience.fragment.FollowFragment
import cn.copaint.audience.fragment.RecommendFragment
import cn.copaint.audience.utils.AuthingUtils.authenticationClient
import cn.copaint.audience.utils.AuthingUtils.loginCheck
import cn.copaint.audience.utils.AuthingUtils.update
import cn.copaint.audience.utils.BitmapUtils.picQueue
import cn.copaint.audience.utils.GrpcUtils.buildStub
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.utils.dp
import com.bugsnag.android.Bugsnag
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomePageActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomePageBinding

    val fragmentList = mutableListOf( FollowFragment(), RecommendFragment())
    var lastBackPressedTimeMillis = 0L
    var myCurrentItem = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        StatusBarUtils.initSystemBar(window,"#303030",false)
        setContentView(binding.root)
        app = this

        buildStub()
        highLightBtn(binding.homePageBtn)
        binding.mainViewPager.apply {
            adapter = ScreenSlidePagerAdapter(this@HomePageActivity)
            setCurrentItem(myCurrentItem, false)
            getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
            this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    myCurrentItem = position
                }
            })
        }


        TabLayoutMediator(binding.tabLayout, binding.mainViewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "关注"
                1 -> tab.text = "推荐"
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

    override fun onResume() {
        super.onResume()
    }

    fun onDrawActivity(view: View) {
        if (loginCheck()) startActivity(Intent(this, DrawActivity::class.java))
    }

    fun onSearchActivity(view: View){
        startActivity(Intent(this, SearchActivity::class.java))
    }


    private fun highLightBtn(view: View) {
        view as TextView
        binding.homePageBtn.isSelected = false
        binding.userPageBtn.isSelected = false
        binding.message.isSelected = false
        binding.playground.isSelected = false
        binding.homePageBtn.setTextColor(Color.parseColor("#B3B3B3"))
        binding.userPageBtn.setTextColor(Color.parseColor("#B3B3B3"))
        binding.playground.setTextColor(Color.parseColor("#B3B3B3"))
        binding.message.setTextColor(Color.parseColor("#B3B3B3"))
        view.isSelected = true
        view.setTextColor(Color.parseColor("#FFFFFF"))
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
        override fun getItemCount() = 2

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }

    fun onAddDialog(view: View) {
        val popBind = DialogHomepageAddBinding.inflate(LayoutInflater.from(this))

        // 弹出PopUpWindow
        val layerDetailWindow = PopupWindow(popBind.root, WindowManager.LayoutParams.MATCH_PARENT, 250.dp, true)
        layerDetailWindow.isOutsideTouchable = true

        // 设置弹窗时背景变暗
        var layoutParams = window.attributes
        layoutParams.alpha = 0.4f // 设置透明度
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams

        // 弹窗消失时背景恢复
        layerDetailWindow.setOnDismissListener {
            layoutParams = window.attributes
            layoutParams.alpha = 1f
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.attributes = layoutParams
        }

        layerDetailWindow.showAtLocation(binding.root, Gravity.BOTTOM, 0, 0)

        popBind.uploadWorkBtn.setOnClickListener{
            if (loginCheck()) startActivity(Intent(this, PublishedWorkActivity::class.java))
            layerDetailWindow.dismiss()
        }
        popBind.publishRequirementBtn.setOnClickListener{
            startActivity(Intent(this,PublishRequirementActivity::class.java))
            layerDetailWindow.dismiss()
        }
        popBind.closeBtn.setOnClickListener{
            layerDetailWindow.dismiss()
        }
        popBind.root.setOnClickListener {
            layerDetailWindow.dismiss()
        }
    }

    fun onMessage(view: View) {
        highLightBtn(view)
    }
    fun onPlayground(view: View) {
        highLightBtn(view)
    }
}
