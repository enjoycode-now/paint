package cn.copaint.audience

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.adapter.FlowAdapter
import cn.copaint.audience.databinding.ActivityPublishedWorkBinding
import cn.copaint.audience.databinding.ItemSearchRecommendBinding
import cn.copaint.audience.utils.GlideEngine
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.bugsnag.android.Bugsnag
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener


class PublishedWorkActivity : AppCompatActivity() {
    lateinit var binding: ActivityPublishedWorkBinding
    val recommendList =
        mutableListOf<String>("一号机", "机甲", "绝对领域", "绝对领域剧场初雪", "最终机", "机甲格斗", "AOE", "无限世界拳击")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivityPublishedWorkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this
        //防止弹出软键盘时将屏幕顶上去
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        binding.recommendRecyclerView.setAdapter(MyFlowAdapter())
        binding.uploadCoverImage.setOnClickListener { selectPic() }
        binding.uploadVideo.setOnClickListener { selectVideo() }
    }


    fun onBackPress(view: View) {
        finish()
    }

    fun selectPic() {
        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine)
            .setSelectionMode(1)
            .setFilterMaxFileSize(5120)//最大5MB
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {
                    if (result != null && result.size > 0) {
                        GlideEngine.loadGridImage(this@PublishedWorkActivity, result[0]?.realPath.toString(),binding.uploadCoverImage)
                    }else{
                        toast("没选中图片")
                    }
                }

                override fun onCancel() {
                    toast("你已经退出")
                }
            })
    }

    fun selectVideo() {
        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofVideo())
            .setImageEngine(GlideEngine)
            .setSelectionMode(1)
            .setSelectMaxFileSize(204800)//200MB以内
            .setFilterVideoMaxSecond(120)//2min以内
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {
                    if (result != null && result.size > 0) {
                        binding.videoLogo.visibility = View.VISIBLE
                        GlideEngine.loadGridImage(this@PublishedWorkActivity, result[0]?.realPath.toString(),binding.uploadVideo)
                    }else{
                        toast("没选中图片")
                    }
                }
                override fun onCancel() {
                    toast("你已经退出选择")
                }
            })
    }

    inner class MyFlowAdapter : FlowAdapter() {
        override val count: Int = recommendList.size

        override fun getView(position: Int, parent: ViewGroup?): View {
            val itemBinding = ItemSearchRecommendBinding.inflate(layoutInflater)
            val s: String = recommendList[position]
            itemBinding.itemTextview.text = s
            itemBinding.itemTextview.isSelected = false
            itemBinding.root.setOnClickListener {
                if(it.isSelected){
                    it.background = resources.getDrawable(R.drawable.bg_item_search_recommend)
                }else{
                    it.background = resources.getDrawable(R.drawable.bg_item_search_recommend_selected)
                }
                it.isSelected = !it.isSelected
                toast(s)
            }
            return itemBinding.root
        }
    }

    fun onPublishedWorkSecondActivity(view: View) {
        startActivity(Intent(this@PublishedWorkActivity,PublishedWorkSecondActivity::class.java))
    }

}
