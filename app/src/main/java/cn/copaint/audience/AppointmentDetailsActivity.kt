package cn.copaint.audience

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.databinding.ActivityAppointmentDetailsBinding
import cn.copaint.audience.databinding.DialogSharepageMoreBinding
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast

class AppointmentDetailsActivity : AppCompatActivity() {
    lateinit var binding:ActivityAppointmentDetailsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)


        binding.picRecyclerview.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )
        binding.picRecyclerview.adapter = VericalLinearPhotoAdapter()
        binding.swipeRefreshLayout.setProgressViewOffset(true, -50, 50)
        binding.swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#B5A0FD"))
    }

    fun onBackPress(view: View) {
        finish()
    }
    fun onMoreDialog(view: View) {
        popupShareDialog(window)
    }
    fun onApplyBtn(view: View) {
        toast("暂无操作")
    }
    fun onAssessmentPainterList(view: View) {
        toast("暂无操作")
    }


    private fun popupShareDialog(window: Window) {
        val popBind = DialogSharepageMoreBinding.inflate(LayoutInflater.from(this))
        // 弹出PopUpWindow
        val layerDetailWindow = PopupWindow(popBind.root, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true)
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

        layerDetailWindow.showAtLocation(binding.root, Gravity.BOTTOM, 0, 0)
    }

    class VericalLinearPhotoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int=0

    }
}