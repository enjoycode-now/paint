package cn.copaint.audience.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.activity.SearchActivity
import cn.copaint.audience.databinding.ItemSearchHistoryBinding
import cn.copaint.audience.activity.searchHistoryList

class searchRecommendAdapter(private val activity: SearchActivity) : RecyclerView.Adapter<searchRecommendAdapter.ViewHolder>()  {
    class ViewHolder (val binding: ItemSearchHistoryBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(searchHistoryString: String){
            binding.historyTv.text = searchHistoryString
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(searchHistoryList[position])
    }

    override fun getItemCount(): Int {
        return searchHistoryList.size
    }
}