package cn.copaint.audience.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.copaint.audience.R
import cn.copaint.audience.databinding.FragmentMyProposalBinding
import com.google.android.material.tabs.TabLayoutMediator

class MyOrdersFragment : Fragment() {
    lateinit var binding: FragmentMyProposalBinding
    var myCurrentItem = 0
    var fragmentList = arrayListOf(CommonItemFragment("MyOrders",0),CommonItemFragment("MyOrders",1),CommonItemFragment("MyOrders",2),CommonItemFragment("MyOrders",3))


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyProposalBinding.inflate(layoutInflater)
        binding.secondViewPager.apply {
            adapter = ScreenSlidePagerAdapter(this@MyOrdersFragment)
            setCurrentItem(myCurrentItem, false)
            getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
            this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    myCurrentItem = position
                }
            })
        }
        TabLayoutMediator(binding.tabLayout, binding.secondViewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "未开始"
                1 -> tab.text = "进行中"
                2 -> tab.text = "已完成"
                3 -> tab.text = "已中断"
            }
        }.attach()
        return binding.root
    }

    private inner class ScreenSlidePagerAdapter(fm: Fragment) : FragmentStateAdapter(fm) {
        override fun getItemCount() = fragmentList.size

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }
}