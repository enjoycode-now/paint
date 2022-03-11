package cn.copaint.audience.adapter

import android.graphics.Color
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.*
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.DialogCreatorMoreBinding
import cn.copaint.audience.databinding.DialogRemoveFanBinding
import cn.copaint.audience.databinding.ItemFansBinding
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.utils.dp
import com.apollographql.apollo3.ApolloClient
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

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
            itemBind.status.setOnClickListener {
                if( (it as TextView).text.equals("回关") ){
                    it.text = "互相关注"
                    it.setTextColor(Color.parseColor("#A9A9A9"))
                }else{
                    it.text = "回关"
                    it.setTextColor(Color.parseColor("#8767E2"))
                }
            }
            itemBind.unsubscribe.setOnClickListener{
                onConfirmRemoveDialog(activity,fans.id,fans.photo)
            }
        }
    }
}

fun onConfirmRemoveDialog(activity: FansActivity, id: String, photo: String?) {
    var popBind = DialogRemoveFanBinding.inflate(LayoutInflater.from(activity))
    if (photo == "" || photo?.endsWith("svg") == true) {
        Glide.with(activity).load(R.drawable.avatar_sample).into(popBind.avatar)
    } else {
        Glide.with(activity).load(photo).into(popBind.avatar)
    }

    // 弹出PopUpWindow
    var layerDetailWindow = PopupWindow(popBind.root, 300.dp, 240.dp, true)
    layerDetailWindow.isOutsideTouchable = true
    val window = activity.window

    // 设置弹窗时背景变暗
    var layoutParams = window.attributes
    layoutParams.alpha = 0.4f // 设置透明度
    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    window.attributes = layoutParams

    // 弹窗消失时背景恢复
    layerDetailWindow.setOnDismissListener {
        layoutParams = window.attributes
        layoutParams.alpha = 1f
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams
    }

    layerDetailWindow.showAtLocation(activity.binding.root, Gravity.CENTER, 0, 0)

    popBind.cancelBtn.setOnClickListener {
        layerDetailWindow.dismiss()
    }
    popBind.closeBtn.setOnClickListener {
        layerDetailWindow.dismiss()
    }
    popBind.confirmBtn.setOnClickListener {

        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient(activity).mutation(RemoveFollowerMutation(id)).execute()
            }catch ( e: Exception){
                Log.e("FansAdapter", e.toString() )
                return@launch
            }
            if ( response.data?.removeFollower == 1){
                activity.runOnUiThread{
                    activity.updateUiInfo()
                }
                toast("删除成功")
            }
            Log.i("adpater", id+"\n"+ response.data?.removeFollower)
        }
        layerDetailWindow.dismiss()
    }

}