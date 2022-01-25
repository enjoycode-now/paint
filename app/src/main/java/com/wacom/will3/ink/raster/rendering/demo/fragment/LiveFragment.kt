package com.wacom.will3.ink.raster.rendering.demo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wacom.will3.ink.raster.rendering.demo.databinding.FragmentLiveBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LiveFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LiveFragment(uri : String ) : Fragment() {


    private var photoUri = uri


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentLiveBinding.inflate(inflater,container,false)
        val view = fragmentBinding.root

        activity?.runOnUiThread{
            Glide.with(this)
                .load(photoUri)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop()
                .into(fragmentBinding.image)
        }

        fragmentBinding.toolbar.likeBtn.setOnClickListener{
            fragmentBinding.toolbar.likeBtn.isLiked = !fragmentBinding.toolbar.likeBtn.isLiked
        }
        return view
    }


}