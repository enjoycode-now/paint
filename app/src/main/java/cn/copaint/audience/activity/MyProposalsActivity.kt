package cn.copaint.audience.activity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.copaint.audience.databinding.ActivityMyProposalsBinding
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.viewmodel.MyProposalsViewModel
import com.bugsnag.android.Bugsnag
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.android.ext.android.get

class MyProposalsActivity : BaseActivity() {
    lateinit var binding: ActivityMyProposalsBinding
    var myCurrentItem  = 0
    val myProposalsViewModel : MyProposalsViewModel by lazy {
        ViewModelProvider(this)[MyProposalsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProposalsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    override fun initView() {
        app = this
        Bugsnag.start(this)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        binding.mainViewPager.apply {
            adapter = ScreenSlidePagerAdapter(this@MyProposalsActivity)
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
                0 -> tab.text = "我的约稿"
                1 -> tab.text = "我的订单"
            }
        }.attach()
    }



    private inner class ScreenSlidePagerAdapter(fm: BaseActivity) : FragmentStateAdapter(fm) {
        override fun getItemCount() = if (myProposalsViewModel.fragmentList.value!=null) myProposalsViewModel.fragmentList.value!!.size else 0

        override fun createFragment(position: Int): Fragment {
            return myProposalsViewModel.fragmentList.value?.get(position)!!
        }
    }

}