package cn.copaint.audience.utils

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.text.InputFilter
import android.view.*
import android.widget.PopupWindow
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.FindIsFollowQuery
import cn.copaint.audience.GetAuthingUsersInfoQuery
import cn.copaint.audience.activity.PayActivity
import cn.copaint.audience.activity.PublishRequirementActivity
import cn.copaint.audience.activity.PublishedWorkActivity
import cn.copaint.audience.adapter.CheckWaitingUserListAdapter
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.databinding.*
import cn.copaint.audience.type.FollowerWhereInput
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.apollographql.apollo3.api.Optional
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.Callback
import okhttp3.Response


object DialogUtils {

    /**
     *
     * @param context
     * @param isAlpha 是否需要透明度
     * @param message 显示加载的内容
     * @return
     */
    fun getLoadingDialog(context: Context, isAlpha: Boolean, message: String?): Dialog {

        val progressDialog = Dialog(context)
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (isAlpha) {
            val lp: WindowManager.LayoutParams? = progressDialog.window?.attributes
            if (lp != null) {
                lp.alpha = 0.8f
                progressDialog.window?.attributes = lp
            }
        }
        val inflater = LayoutInflater.from(context)
        val binding = DialogLoadingBinding.inflate(inflater)

        val progressBar: ProgressBar = binding.pbProgressBar
        progressBar.visibility = View.VISIBLE
        if (message == null || message == "") {
            binding.tvLoading.visibility = View.GONE
        } else {
            binding.tvLoading.text = message
            binding.tvLoading.setTextColor(Color.parseColor("#8767E2"))
        }
        progressDialog.setContentView(
            binding.root,
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        )
        return progressDialog
    }

    /**
     *
     * @param view activity的root view
     * @param activity 当前活动
     * @return
     */
    fun onAddDialog(view: View, activity: AppCompatActivity) {
        val popBind = DialogHomepageAddBinding.inflate(LayoutInflater.from(activity))

        // 弹出PopUpWindow
        val layerDetailWindow = PopupWindow(
            popBind.root,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            true
        )
        layerDetailWindow.isOutsideTouchable = true

        layerDetailWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0)

        popBind.uploadWorkBtn.setOnClickListener {
            if (AuthingUtils.loginCheck()) activity.startActivity(
                Intent(
                    activity,
                    PublishedWorkActivity::class.java
                )
            )
            layerDetailWindow.dismiss()
        }
        popBind.publishRequirementBtn.setOnClickListener {
            activity.startActivity(Intent(activity, PublishRequirementActivity::class.java))
            layerDetailWindow.dismiss()
        }
        popBind.closeBtn.setOnClickListener {
            layerDetailWindow.dismiss()
        }
        popBind.root.setOnClickListener {
            layerDetailWindow.dismiss()
        }
    }


    fun onMoneyInputDialog(view: View,activity: PayActivity){
        val popBind = DialogPayInputCustomNumBinding.inflate(LayoutInflater.from(activity))

        // 弹出PopUpWindow
        val layerDetailWindow = PopupWindow(
            popBind.root,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        layerDetailWindow.isOutsideTouchable = true

        // 设置弹窗时背景变暗
        var layoutParams = activity.window.attributes
        layoutParams.alpha = 0.4f // 设置透明度
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        activity.window.attributes = layoutParams

        // 弹窗消失时背景恢复
        layerDetailWindow.setOnDismissListener {
            layoutParams = activity.window.attributes
            layoutParams.alpha = 1f
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            activity.window.attributes = layoutParams
            toast("你取消了输入")
        }
        layerDetailWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        popBind.moneyEditText.filters = arrayOf(MoneyInputFilter)
        popBind.tv.setOnClickListener{
            popBind.moneyEditText.requestFocus()
        }
        popBind.submitBtn.setOnClickListener {
            // 重新设置监听器，取消toast提示
            layerDetailWindow.setOnDismissListener {
                layoutParams = activity.window.attributes
                layoutParams.alpha = 1f
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                activity.window.attributes = layoutParams
            }
            activity.payViewModel.currentNum.value = popBind.moneyEditText.text.toString().toDouble()
            layerDetailWindow.dismiss()
        }
    }

    fun popupShareDialog(context: Context,view: View,window: Window) {
        val popBind = DialogSharepageMoreBinding.inflate(LayoutInflater.from(context))
        // 弹出PopUpWindow
        val layerDetailWindow = PopupWindow(
            popBind.root,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        layerDetailWindow.isOutsideTouchable = true

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

        layerDetailWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0)
    }

    fun checkWaitingUserList(userIdList: ArrayList<String>,context: Context,view: View,window: Window){
        val tempList = ArrayList< GetAuthingUsersInfoQuery.AuthingUsersInfo>()
        val isFollowList = ArrayList<Boolean>()

        val popBind = DialogCheckWaitingUserListBinding.inflate(LayoutInflater.from(context))
        popBind.recycler.layoutManager = LinearLayoutManager(context)
        val adapter = CheckWaitingUserListAdapter(userList = tempList, isFollowList = isFollowList,context)
        popBind.recycler.adapter = adapter

            CoroutineScope(Dispatchers.IO).launch {
                try {
                myApolloClient.apolloClient(app)
                    .query(GetAuthingUsersInfoQuery(userIdList))
                    .execute().data?.authingUsersInfo?.forEach {
                        tempList.add(it)
                    }
                tempList.forEach {
                    val followResponse = myApolloClient.apolloClient(ToastUtils.app).query(
                        FindIsFollowQuery(
                            where = Optional.presentIfNotNull(
                                FollowerWhereInput(
                                    userID = Optional.presentIfNotNull(it.id),
                                    followerID = Optional.presentIfNotNull(user.id)
                                )
                            )
                        )
                    ).execute().data
                    if (followResponse?.followers?.totalCount == 1) {
                        isFollowList.add(true)
                    } else {
                        isFollowList.add(false)
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    adapter.notifyDataSetChanged()
                    popBind.progressBar.visibility = View.GONE
                }

                } catch (e: Exception) {
                    toast(e.toString())
                }
            }



//        adapter.notifyDataSetChanged()
        // 弹出PopUpWindow
        val layerDetailWindow = PopupWindow(
            popBind.root,
            300.dp,
            300.dp,
            true
        )
        layerDetailWindow.isOutsideTouchable = true

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

        layerDetailWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    fun getConfirmDialog(
        share: String,
        price: String,
        context: Context,
        view: View,
        window: Window,
        confirmListener: View.OnClickListener
    ) : PopupWindow{
        val popBind = DialogConfirmUploadWorkBinding.inflate(LayoutInflater.from(context))
        popBind.share.text = popBind.share.text.toString().plus(share).plus('%')
        popBind.price.text = popBind.price.text.toString().plus(price).plus("元贝")
        // 弹出PopUpWindow
        val layerDetailWindow = PopupWindow(
            popBind.root,
            300.dp,
            300.dp,
            true
        )
        layerDetailWindow.isOutsideTouchable = false

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
        popBind.confirmBtn.setOnClickListener(confirmListener)
        popBind.cancelBtn.setOnClickListener{
            layerDetailWindow.dismiss()
        }
        popBind.closeBtn.setOnClickListener{
            layerDetailWindow.dismiss()
        }
        layerDetailWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        return layerDetailWindow
    }

}