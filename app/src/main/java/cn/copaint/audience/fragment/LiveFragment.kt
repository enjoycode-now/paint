package cn.copaint.audience.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cn.copaint.audience.R
import cn.copaint.audience.databinding.FragmentLiveBinding
import kotlinx.android.synthetic.main.fragment_item_live.*
import kotlin.properties.Delegates
private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f
class LiveFragment : Fragment() {

    lateinit var fragmentBinding : FragmentLiveBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        fragmentBinding = FragmentLiveBinding.inflate(inflater,container,false)
        val view = fragmentBinding.root

        fragmentBinding.showImageViewPager.registerOnPageChangeCallback( object : ViewPager2.OnPageChangeCallback() {
            var current_fragment : View? = null
            var last_fragment : View? = null
            val myFragment = childFragmentManager.findFragmentByTag("f" + fragmentBinding.showImageViewPager.currentItem)
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                when(state){
                    1 ->{
//                        myFragment?.alpha = 0.5f
                        myFragment?.view?.findViewById<View>(R.id.toolbar)?.alpha = 0.5f
                    }
                    else ->{
//                        myFragment?.alpha = 1f
                        myFragment?.view?.findViewById<View>(R.id.toolbar)?.alpha = 1f
                    }
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
//                last_fragment  = current_fragment
//                current_fragment = childFragmentManager.fragments[fragmentBinding.showImageViewPager.currentItem].toolbar
                Log.i("chenlin", "position= "+position)
            }
        })

        fragmentBinding.showImageViewPager.apply {
            offscreenPageLimit=2
            adapter = ScreenSlidePagerAdapter(this@LiveFragment)
        }

        return view
    }



    private inner class ScreenSlidePagerAdapter(fm: Fragment ) : FragmentStateAdapter(fm) {
        override fun getItemCount() = Int.MAX_VALUE

        override fun createFragment(position: Int): Fragment {
            return ItemLiveFragment()
        }
    }

}





