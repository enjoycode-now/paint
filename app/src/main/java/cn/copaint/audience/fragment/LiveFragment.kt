package cn.copaint.audience.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.copaint.audience.databinding.FragmentLiveBinding

class LiveFragment : Fragment() {

    lateinit var recommendBinding: FragmentLiveBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        recommendBinding = FragmentLiveBinding.inflate(layoutInflater, container, false)

        return recommendBinding.root
    }
}
