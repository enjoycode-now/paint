package cn.copaint.audience.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.*
import cn.copaint.audience.databinding.ItemFansBinding
import cn.copaint.audience.databinding.ItemFollowBinding
import cn.copaint.audience.model.Follow
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.ToastUtils.toast
import com.apollographql.apollo3.ApolloClient
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FansAdapter(private val activity: FansActivity) : RecyclerView.Adapter<FansAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFansBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.fansList[position], position,activity,this)
    }

    override fun getItemCount() = activity.fansList.size

    class ViewHolder(val itemBind: ItemFansBinding) : RecyclerView.ViewHolder(itemBind.root) {
        fun bind(fans: GetAuthingUsersInfoQuery.AuthingUsersInfo,position: Int, activity: FansActivity,adapter: FansAdapter) {
            itemBind.nicikname.text = fans.nickname ?: "此用户未命名"
            if (fans.photo == "" || fans.photo?.endsWith("svg") == true) {
                Glide.with(activity).load(R.drawable.avatar_sample).into(itemBind.avatar)
            } else {
                Glide.with(activity).load(fans.photo).into(itemBind.avatar)
            }
            itemBind.unsubscribe.setOnClickListener{
                val apolloclient = ApolloClient.Builder()
                    .serverUrl("http://120.78.173.15:20000/query")
                    .addHttpHeader("Authorization", "Bearer " + AuthingUtils.user.token!!)
                    .build()
                CoroutineScope(Dispatchers.IO).launch {
                    val response = apolloclient.mutation(RemoveFollowerMutation(fans.id)).execute()
                    if ( response?.data?.removeFollower == 1){
                        activity.runOnUiThread{
//                            activity.fansAdapter.notifyItemChanged(position)
                            activity.updateUiInfo()
                        }
                        toast("删除成功")
                    }
                    Log.i("adpater", fans.id+"\n"+response?.data?.removeFollower)
                }

            }
        }
    }
}
