package cn.copaint.audience

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import cn.copaint.audience.adapter.FlowAdapter
import cn.copaint.audience.databinding.ActivityPublishedWorkBinding
import cn.copaint.audience.databinding.ItemLabelCustomBinding
import cn.copaint.audience.databinding.ItemSearchRecommendBinding
import cn.copaint.audience.utils.*
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.bugsnag.android.Bugsnag
import com.google.gson.JsonObject
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception


/**
 * 上传作品页
 */
class PublishedWorkActivity : AppCompatActivity() {
    lateinit var binding: ActivityPublishedWorkBinding
    val recommendList =
        mutableListOf<String>("一号机", "机甲", "绝对领域", "绝对领域剧场初雪", "最终机", "机甲格斗", "AOE", "无限世界拳击")

    val tagList = mutableListOf<String>("机甲")
    var workName = ""
    var workIntroduction = ""
    var coverPicUrl = ""
    var videoUrl = ""
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
        binding.workNameEditText.doAfterTextChanged { text -> workName = text.toString()  }
        binding.workIntroductionEditText.doAfterTextChanged { text -> workIntroduction = text.toString()  }
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
                        binding.uploadCoverImageMask.visibility = View.GONE
                        coverPicUrl = result[0]?.availablePath.toString()
                        GlideEngine.loadGridImage(this@PublishedWorkActivity, result[0]?.availablePath.toString(),binding.uploadCoverImage)
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
                        binding.uploadCoverVideoMask.visibility = View.GONE
                        videoUrl = result[0]?.availablePath.toString()
                        GlideEngine.loadGridImage(this@PublishedWorkActivity, result[0]?.availablePath.toString(),binding.uploadVideo)
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
        override val count: Int = recommendList.size+1


        override fun getView(position: Int, parent: ViewGroup?): View {
            // 新增一个自定义label,位置处于最末尾
            if( position == recommendList.lastIndex+1){
                val itemBinding = ItemLabelCustomBinding.inflate(layoutInflater)
                itemBinding.itemTextview.isSelected = false
                itemBinding.itemTextview.setOnClickListener(
                    DoubleClick(object : DoubleClickListener {
                    override fun onSingleClickEvent(view: View?) {
                        if(view != null){
                            itemBinding.itemTextview.focusable = View.NOT_FOCUSABLE
                            if(itemBinding.itemTextview.isSelected){
                                itemBinding.itemTextview.setTextColor(Color.parseColor("#8C6DE3"))
                                itemBinding.root.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_item_search_recommend,null)
                            }else{
                                itemBinding.itemTextview.setTextColor(Color.parseColor("#FFFFFF"))
                                itemBinding.root.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_item_search_recommend_selected,null)
                            }
                            itemBinding.itemTextview.isSelected = !view.isSelected
                        }
                        toast("单击")
                    }

                    override fun onDoubleClickEvent(view: View?) {
                        itemBinding.itemTextview.focusable = View.FOCUSABLE
                        if (itemBinding.itemTextview.text.lastIndex>0)
                            itemBinding.itemTextview.setSelection(itemBinding.itemTextview.text.lastIndex)
                        else
                            itemBinding.itemTextview.setSelection(0)
                        toast("双击")
                    }
                })
                )
                itemBinding.itemTextview.setOnEditorActionListener { textview, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if(textview.text.toString() == ""){
                            textview.text = "自定义"
                            textview.clearFocus()
                            toast("标签不得为空")
                            return@setOnEditorActionListener false
                        }
                        itemBinding.itemTextview.isSelected = true
                        itemBinding.itemTextview.focusable = View.NOT_FOCUSABLE
                        itemBinding.root.background = resources.getDrawable(R.drawable.bg_item_search_recommend_selected)
                        val imm: InputMethodManager =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        // 隐藏软键盘
                        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
                    }
                    true
                }
                return itemBinding.root
            }else{
                val itemBinding = ItemSearchRecommendBinding.inflate(layoutInflater)
                val s: String = recommendList[position]
                itemBinding.itemTextview.text = s
                itemBinding.itemTextview.isSelected = false
                itemBinding.root.setOnClickListener {
                    if(it.isSelected){
                        itemBinding.itemTextview.setTextColor(Color.parseColor("#8C6DE3"))
                        itemBinding.root.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_item_search_recommend,null)
                    }else{
                        itemBinding.itemTextview.setTextColor(Color.parseColor("#FFFFFF"))
                        itemBinding.root.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_item_search_recommend_selected,null)
                    }
                    it.isSelected = !it.isSelected
                    toast(s)
                }
                return itemBinding.root
            }
        }
    }


    fun onPublishedWorkSecondActivity(view: View) {
        if (workName.isNullOrEmpty()){
            toast("作品名称不能为空")
            return
        }
        if (workIntroduction.isNullOrEmpty()){
            toast("作品简介不能为空")
            return
        }
        if (coverPicUrl.isNullOrEmpty()){
            toast("请选择封面图")
            return
        }
        if (videoUrl.isNullOrEmpty()){
            toast("请选择作画视频")
            return
        }
        var status = false
        contentResolver.openInputStream(coverPicUrl.toUri())
            ?.let { status = FileUploadUtils.uploadPic(this, it.readBytes(),UploadPicCallBack()) }

        contentResolver.openInputStream(videoUrl.toUri())
            ?.let { status = FileUploadUtils.uploadVideo(this, it.readBytes(),UploadVideoCallBack()) }

        intent.setClass(this,PublishedWorkSecondActivity::class.java)
        intent.putExtra("workName",workName)
        intent.putExtra("workIntroduction",workIntroduction)
        intent.putExtra("coverPicUrl",coverPicUrl)
        intent.putExtra("videoUrl",videoUrl)

    }

    inner class UploadPicCallBack(): Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("PublishedWorkActivity", e.toString() )
        }

        override fun onResponse(call: Call, response: Response) {
            val str = try {
                JSONObject(response.body?.string()).takeIf { !it.isNull("key") }?.getString("key")
            }catch ( e : Exception ){
                toast(e.toString())
                return
            }

            intent.putExtra("coverPicUrlKey",str)
            toast("图片上传成功")
        }

    }


    inner class UploadVideoCallBack(): Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("PublishedWorkActivity", e.toString() )
        }

        override fun onResponse(call: Call, response: Response) {
            val str = try {
                JSONObject(response.body?.string()).takeIf { !it.isNull("key") }?.getString("key")
            }catch ( e : Exception ){
                toast(e.toString())
                return
            }
            intent.putExtra("videoUrlKey",str)
            toast("视频上传成功")
            startActivity(intent)
        }

    }

}
