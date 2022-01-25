package com.wacom.will3.ink.raster.rendering.demo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wacom.will3.ink.raster.rendering.demo.R
import com.wacom.will3.ink.raster.rendering.demo.databinding.FragmentRecommendBinding


class RecommendFragment : Fragment() {

    lateinit var recommendBinding: FragmentRecommendBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        recommendBinding = FragmentRecommendBinding.inflate(layoutInflater,container,false)

        return recommendBinding.root
    }

}