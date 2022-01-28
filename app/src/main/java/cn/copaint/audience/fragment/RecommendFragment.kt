package cn.copaint.audience.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.copaint.audience.databinding.FragmentRecommendBinding

class RecommendFragment : Fragment() {

    lateinit var recommendBinding: FragmentRecommendBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        recommendBinding = FragmentRecommendBinding.inflate(layoutInflater, container, false)

        return recommendBinding.root
    }
}
