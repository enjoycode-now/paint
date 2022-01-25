package cn.copaint.audience.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import cn.copaint.audience.databinding.FragmentLiveBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LiveFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
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