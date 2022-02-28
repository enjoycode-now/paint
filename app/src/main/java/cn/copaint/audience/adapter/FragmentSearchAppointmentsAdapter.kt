package cn.copaint.audience.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.R
import cn.copaint.audience.databinding.FragmentItemSearchAppointmentsBinding
import cn.copaint.audience.fragment.SearchAppointmentFragment
import com.bumptech.glide.Glide

class FragmentSearchAppointmentsAdapter(val fragment: SearchAppointmentFragment): RecyclerView.Adapter<FragmentSearchAppointmentsAdapter.ViewHolder>() {
    class ViewHolder(val binding: FragmentItemSearchAppointmentsBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, fragment: SearchAppointmentFragment) {
            binding.title.text = fragment.data[position]
            Glide.with(fragment)
                .load(R.drawable.avatar_sample)
                .into(binding.coverPic)
            Glide.with(fragment)
                .load(R.drawable.avatar_sample)
                .into(binding.avatar)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            FragmentItemSearchAppointmentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position,fragment)
    }

    override fun getItemCount() = fragment.data.size
}