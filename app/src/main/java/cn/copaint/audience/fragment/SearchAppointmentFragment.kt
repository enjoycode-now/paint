package cn.copaint.audience.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.adapter.FragmentSearchAppointmentsAdapter
import cn.copaint.audience.databinding.FragmentSearchAppointmentsBinding

class SearchAppointmentFragment : Fragment() {

    lateinit var binding: FragmentSearchAppointmentsBinding
    val data: ArrayList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSearchAppointmentsBinding.inflate(inflater,container,false)
        binding.appointmentsRecyclerView.layoutManager = LinearLayoutManager(context,
            LinearLayoutManager.VERTICAL,false)
        binding.appointmentsRecyclerView.adapter = FragmentSearchAppointmentsAdapter(this)
        data.clear()
        data.add("test1")
        data.add("test2")
        return binding.root
    }



}