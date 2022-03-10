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
import cn.copaint.audience.databinding.FragmentItemSearchWorkBinding
import cn.copaint.audience.databinding.ItemSearchRecommendBinding
import cn.copaint.audience.databinding.ItemUserpageEmptyViewBinding
import cn.copaint.audience.fragment.SearchAppointmentFragment
import cn.copaint.audience.utils.DateUtils.rcfDateStr2DateStr
import cn.copaint.audience.utils.DateUtils.rcfDateStr2StandardDateStr
import cn.copaint.audience.utils.DateUtils.rcfDateStr2StandardDateStrWithoutTime
import cn.copaint.audience.utils.ToastUtils
import com.bumptech.glide.Glide

class FragmentSearchAppointmentsAdapter(val fragment: SearchAppointmentFragment): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val EMPTY_TYPE = 0
    val NORMAL_TYPE = 1

    class ViewHolder(val binding: FragmentItemSearchAppointmentsBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, fragment: SearchAppointmentFragment) {
            binding.authorName.text = fragment.dataList[position].nickname
            binding.title.text = fragment.dataList[position].title
            binding.description.text = fragment.dataList[position].description
            binding.date.text = rcfDateStr2StandardDateStrWithoutTime(fragment.dataList[position].createAt?:"")
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
                val coverPicUrl =  fragment.resources.getString(R.string.PicUrlPrefix)+(fragment.dataList[position].example?.get(0)?.key ?: "")
                Glide.with(fragment)
                    .load(coverPicUrl)
                    .error(R.drawable.loading_failed)
                    .into(binding.coverPic)
            }else{
                // 用户没有上传例图
                binding.coverPic.visibility = View.GONE
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
    inner class EmptyViewHolder(val itemBind: ItemUserpageEmptyViewBinding) :
        RecyclerView.ViewHolder(itemBind.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {

            NORMAL_TYPE -> {
                val binding =
                    FragmentItemSearchAppointmentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding)
            }
            else -> {
                val binding = ItemUserpageEmptyViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return EmptyViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (fragment.dataList.size == 0)
            EMPTY_TYPE
        else
            NORMAL_TYPE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(position, fragment)
        } else {

        }
    }

    override fun getItemCount() = if (fragment.dataList.size == 0) 1 else fragment.dataList.size


}