package cn.copaint.audience.utils

import android.R
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.PublishRequirementActivity
import cn.copaint.audience.PublishedWorkActivity
import cn.copaint.audience.databinding.DialogHomepageAddBinding
import cn.copaint.audience.databinding.DialogLoadingBinding


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

}