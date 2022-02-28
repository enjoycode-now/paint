package cn.copaint.audience.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.R
import cn.copaint.audience.adapter.FragmentSearchUserAdapter
import cn.copaint.audience.adapter.FragmentSearchWorkAdapter
import cn.copaint.audience.databinding.FragmentItemSearchWorkBinding
import cn.copaint.audience.databinding.FragmentItemSearchWorksBinding

class SearchWorksFragment : Fragment() {
    lateinit var binding: FragmentItemSearchWorksBinding
    lateinit var adapter: FragmentSearchWorkAdapter
    val workList  = arrayListOf("https://files.authing.co/user-contents/photos/27421765-ac6f-4ee5-aff3-9b1b5695e75f.jpg")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentItemSearchWorksBinding.inflate(layoutInflater,container,false)
        binding.worksRecyclerView.layoutManager = LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false)
        adapter = FragmentSearchWorkAdapter(this)
        binding.worksRecyclerView.adapter = adapter
        return  binding.root
    }



}