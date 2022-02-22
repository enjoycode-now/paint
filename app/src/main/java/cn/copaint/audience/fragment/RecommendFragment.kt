package cn.copaint.audience.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.copaint.audience.R
import cn.copaint.audience.databinding.FragmentRecommendBinding

class RecommendFragment : Fragment() {

    lateinit var fragmentBinding: FragmentRecommendBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentBinding = FragmentRecommendBinding.inflate(inflater, container, false)
        val view = fragmentBinding.root

        fragmentBinding.showImageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            val myFragment = childFragmentManager.findFragmentByTag("f" + fragmentBinding.showImageViewPager.currentItem)
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                when (state) {
                    1 -> myFragment?.view?.findViewById<View>(R.id.toolbar)?.alpha = 0.5f
                    else -> myFragment?.view?.findViewById<View>(R.id.toolbar)?.alpha = 1f
                }
            }
        })

        fragmentBinding.showImageViewPager.apply {
            offscreenPageLimit = 2
            adapter = ScreenSlidePagerAdapter(this@RecommendFragment)
        }

        return view
    }

    private inner class ScreenSlidePagerAdapter(fm: Fragment) : FragmentStateAdapter(fm) {
        override fun getItemCount() = Int.MAX_VALUE

        override fun createFragment(position: Int): Fragment {
            return ItemRecommendFragment()
        }
    }
}
