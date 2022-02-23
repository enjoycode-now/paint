package cn.copaint.audience.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.SearchActivity
import cn.copaint.audience.databinding.ItemFansBinding
import cn.copaint.audience.databinding.ItemSearchHistoryBinding

class SearchHistoryAdapter(private val activity: SearchActivity) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>()  {
    class ViewHolder (val binding: ItemSearchHistoryBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(searchHistoryString: String,position: Int,activity: SearchActivity,searchHistoryAdapter: SearchHistoryAdapter){
            binding.historyTv.text = searchHistoryString
            binding.historyDelete.setOnClickListener{
                activity.searchHistoryList.removeAt(position)
                searchHistoryAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.searchHistoryList[position],position,activity,this)
    }

    override fun getItemCount(): Int {
        return activity.searchHistoryList.size
    }
}