package cn.copaint.audience.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.FollowUserMutation
import cn.copaint.audience.GetAuthingUsersInfoQuery
import cn.copaint.audience.R
import cn.copaint.audience.UnfollowUserMutation
import cn.copaint.audience.activity.PublishRequirementActivity
import cn.copaint.audience.activity.UserPageCreatorActivity
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.databinding.ItemCheckWaitingUserListBinding
import cn.copaint.audience.databinding.ItemSelectPainterRadioBinding
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.ToastUtils
import com.bumptech.glide.Glide
import com.luck.picture.lib.thread.PictureThreadUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class SelectPainterAdapter(private val userList: ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo>, val activity: PublishRequirementActivity,val view:View) :
    RecyclerView.Adapter<SelectPainterAdapter.ViewHolder>() {
    var selectedPainter :GetAuthingUsersInfoQuery.AuthingUsersInfo? = activity.selectedPaintUserInfo

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectPainterRadioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userList[position],activity)
    }

    override fun getItemCount() = userList.size

    inner class ViewHolder(private val itemBind: ItemSelectPainterRadioBinding) : RecyclerView.ViewHolder(itemBind.root) {
        fun bind(
            userinfo: GetAuthingUsersInfoQuery.AuthingUsersInfo,
            context: Context
        ) {
            itemBind.nicikname.text = userinfo.nickname ?: context.getText(R.string.un_give_name)
            if (userinfo.photo == "" || userinfo.photo?.endsWith("svg") == true) {
                Glide.with(context).load(R.drawable.avatar_sample).into(itemBind.avatar)
            } else {
                Glide.with(context).load(userinfo.photo).into(itemBind.avatar)
            }
            itemBind.radioBtn.isChecked = userinfo.id == selectedPainter?.id
            itemBind.radioBtn.setOnClickListener {
                selectedPainter = userinfo
                notifyDataSetChanged()
                view.isEnabled = true
            }
            itemBind.root.setOnClickListener {
                selectedPainter = userinfo
                notifyDataSetChanged()
                view.isEnabled = true
            }
        }
    }
}