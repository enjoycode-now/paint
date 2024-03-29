package cn.copaint.audience.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.activity.SearchActivity
import cn.copaint.audience.activity.SearchResultActivity
import cn.copaint.audience.databinding.ItemSearchHistoryBinding

class SearchHistoryAdapter(private val activity: SearchActivity) :
    RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemSearchHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            searchHistoryString: String,
            position: Int,
            activity: SearchActivity,
            searchHistoryAdapter: SearchHistoryAdapter
        ) {
            binding.root.setOnClickListener {
                activity.searchHistoryList.remove(searchHistoryString)
                activity.searchHistoryList.add(0,searchHistoryString)
                searchHistoryAdapter.notifyItemRemoved(0)
                activity.startActivity(
                    Intent(
                        activity,
                        SearchResultActivity::class.java
                    ).putExtra("SearchContent", searchHistoryString)
                )
            }
            binding.historyTv.text = searchHistoryString
            binding.historyDelete.setOnClickListener {
                activity.searchHistoryList.removeAt(position)
                searchHistoryAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSearchHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.searchHistoryList[position], position, activity, this)
    }

    override fun getItemCount(): Int {
        return if (activity.searchHistoryList.size > 6) 6 else activity.searchHistoryList.size
    }
}