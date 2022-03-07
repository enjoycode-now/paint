package cn.copaint.audience.utils

import android.R
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import cn.copaint.audience.databinding.DialogLoadingBinding


object  DialogUtils {

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
            binding.tvLoading.setTextColor(Color.parseColor("#EFEDFC"))
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
}