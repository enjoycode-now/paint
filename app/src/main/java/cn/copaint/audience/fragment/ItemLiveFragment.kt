package cn.copaint.audience.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import cn.copaint.audience.R
import cn.copaint.audience.databinding.FragmentItemLiveBinding


class ItemLiveFragment(uri : String) : Fragment() {

    lateinit var fragmentItemLiveBinding: FragmentItemLiveBinding
    var photoUri = uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentItemLiveBinding = FragmentItemLiveBinding.inflate(layoutInflater,container,false)



        activity?.runOnUiThread{
            Glide.with(this)
                .load(photoUri)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop()
                .into(fragmentItemLiveBinding.image)
        }

        fragmentItemLiveBinding.toolbar.likeBtn.setOnClickListener{
            fragmentItemLiveBinding.toolbar.likeBtn.isLiked = !fragmentItemLiveBinding.toolbar.likeBtn.isLiked
        }

        return fragmentItemLiveBinding.root
    }

}