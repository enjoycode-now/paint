package cn.copaint.audience.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import cn.copaint.audience.databinding.FragmentLiveBinding


class LiveFragment(uri : String) : Fragment() {


    val photoUri = uri
    val picList = mutableListOf(ItemLiveFragment(photoUri),ItemLiveFragment(photoUri),ItemLiveFragment(photoUri))
    lateinit var fragmentBinding : FragmentLiveBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentBinding = FragmentLiveBinding.inflate(inflater,container,false)
        val view = fragmentBinding.root

        fragmentBinding.showImageViewPager.apply {
            offscreenPageLimit=2
            adapter = ScreenSlidePagerAdapter(this@LiveFragment)
        }



//        fragmentBinding.showImageViewPager.listen

        return view
    }


    private inner class ScreenSlidePagerAdapter(fm: Fragment ) : FragmentStateAdapter(fm) {
        override fun getItemCount() = Int.MAX_VALUE

        override fun createFragment(position: Int): Fragment {
            while (position>picList.lastIndex-3)picList.add(ItemLiveFragment(photoUri))
            return picList[position]
        }
    }

}