package cn.copaint.audience.adapter

import android.content.Intent
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.R
import cn.copaint.audience.SearchResultActivity
import cn.copaint.audience.databinding.FragmentItemSearchAppointmentsBinding
import cn.copaint.audience.databinding.ItemSearchRecommendBinding
import cn.copaint.audience.fragment.SearchAppointmentFragment
import cn.copaint.audience.utils.DateUtils.rcfDateStr2DateStr
import cn.copaint.audience.utils.ToastUtils
import com.bumptech.glide.Glide

class FragmentSearchAppointmentsAdapter(val fragment: SearchAppointmentFragment): RecyclerView.Adapter<FragmentSearchAppointmentsAdapter.ViewHolder>() {
    class ViewHolder(val binding: FragmentItemSearchAppointmentsBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, fragment: SearchAppointmentFragment) {
            binding.authorName.text = fragment.dataList[position].nickname
            binding.title.text = fragment.dataList[position].title
            binding.description.text = fragment.dataList[position].description
            binding.date.text = rcfDateStr2DateStr(fragment.dataList[position].createAt?:"")
            if (fragment.dataList[position].colorMode == ""){
                binding.workType.visibility = View.INVISIBLE
            }else{
                binding.workType.text = fragment.dataList[position].colorMode
            }


            binding.yuanbeiText.text = "${(fragment.dataList[position].balance ?:0) * (fragment.dataList[position].stock ?:0)}"
            Glide.with(fragment)
                .load(fragment.dataList[position].avatar)
                .into(binding.avatar)
            if(fragment.dataList[position].example?.size?:0 > 0){
                Glide.with(fragment)
                    .load(fragment.dataList[position].example?.get(0) ?: "")
                    .error(R.drawable.avatar_sample)
                    .into(binding.coverPic)
            }
            binding.description.setOnClickListener {
                if (binding.description.ellipsize == null ){
                    binding.description.ellipsize = TextUtils.TruncateAt.END
                    binding.description.setLines(2)
                }else{
                    binding.description.ellipsize = null
                    binding.description.isSingleLine = false
                }
            }

        }

//        inner class MyFlowAdapter(val searchAppointmentInfo: SearchAppointmentFragment.searchAppointmentInfo) :FlowAdapter(){
//            override val count: Int = if (searchAppointmentInfo)
//
//                override fun getView(position: Int, parent: ViewGroup?): View {
//                val itemBinding = ItemSearchRecommendBinding.inflate(layoutInflater)
//                val s: String = recommendList[position]
//                itemBinding.itemTextview.text = s
//                itemBinding.root.setOnClickListener {
//                    searchHistoryList.add(0,itemBinding.itemTextview.text.toString())
//                    startActivity(Intent(this@SearchActivity, SearchResultActivity::class.java).putExtra("SearchContent",itemBinding.itemTextview.text.toString()))
//                    ToastUtils.toast(s)
//                }
//                return itemBinding.root
//            }
//        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            FragmentItemSearchAppointmentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position,fragment)
    }

    override fun getItemCount() = fragment.dataList.size


}