package cn.copaint.audience.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import cn.copaint.audience.adapter.ItemSearchAppointmentsAdapter
import cn.copaint.audience.adapter.YuanbeiDetailAdapter
import cn.copaint.audience.databinding.FragmentItemSearchAppointmentsBinding
import cn.copaint.audience.databinding.FragmentSearchAppointmentsBinding
import cn.copaint.audience.databinding.ItemYuanbeiDetailBinding

class SearchAppointmentFragment : Fragment() {

    lateinit var binding: FragmentSearchAppointmentsBinding
    val data: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSearchAppointmentsBinding.inflate(inflater,container,false)
        binding.appointmentsRecyclerView.layoutManager = LinearLayoutManager(context,
            LinearLayoutManager.VERTICAL,false)
        binding.appointmentsRecyclerView.adapter = ItemSearchAppointmentsAdapter(this)
        data.add("test1")
        data.add("test2")
        return binding.root
    }



}