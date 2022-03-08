package cn.copaint.audience

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import cn.copaint.audience.adapter.GridImageAdapter
import cn.copaint.audience.databinding.ActivityPublishRequirementBinding
import cn.copaint.audience.type.ProposalType
import cn.copaint.audience.utils.DialogUtils
import cn.copaint.audience.utils.FileUploadUtils.uploadPic
import cn.copaint.audience.utils.GlideEngine
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.apollographql.apollo3.api.Optional
import com.luck.picture.lib.app.PictureAppMaster
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.PictureSelectionConfig.selectorStyle
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.utils.MediaUtils
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 * 发布约稿页面
 */
class PublishRequirementActivity : AppCompatActivity() {
    private val TAG = "PublishRequirementActivity"
    lateinit var binding: ActivityPublishRequirementBinding
    val photoList = arrayListOf<LocalMedia>()
    lateinit var mAdapter: GridImageAdapter
    val maxSelectNum = 9
    lateinit var launcherResult: ActivityResultLauncher<Intent>
    var proposalTitle:String= ""
    var proposalDescription :String = ""
    var requirementType = "" //需求类型
    var dealLine = Optional.presentIfNotNull(Date())  // 截稿日期
    var workStyle: String = "机甲风" // 作品风格
    var colorMode: String = "暖色调" // 颜色模式
    var dimensions = "1920x1080" // 尺寸规格
    var wordFormat: String = "" // 稿件格式
    val example: ArrayList<String> = arrayListOf() //样例图
    var acceptancePhase: String = ""// 验收阶段
    var balance: Int = 100
    val stock: Int = 10
    var isPublic= Optional.presentIfNotNull(ProposalType.PUBLIC) // 稿件是否公开

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublishRequirementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        selectorStyle = PictureSelectorStyle()
        // 注册需要写在onCreate或Fragment onAttach里，否则会报java.lang.IllegalStateException异常
        launcherResult = createActivityResultLauncher()


        binding.referenceSampleRecyclerview.layoutManager =
            GridLayoutManager(this,3,GridLayoutManager.VERTICAL, false)
        mAdapter = GridImageAdapter(this, photoList)
        binding.referenceSampleRecyclerview.adapter = mAdapter

        binding.proposalTitle.doAfterTextChanged { text -> proposalTitle = text.toString() }
        binding.proposalDescription.doAfterTextChanged { text -> proposalDescription = text.toString() }

        mAdapter.setOnItemClickListener(object : GridImageAdapter.OnItemClickListener {
            override fun onItemClick(v: View?, position: Int) {
                // 预览图片、视频、音频
                PictureSelector.create(this@PublishRequirementActivity)
                    .openPreview()
                    .setImageEngine(GlideEngine)
                    .setSelectorUIStyle(selectorStyle)
                    .isPreviewFullScreenMode(true)
                    .setExternalPreviewEventListener(object : OnExternalPreviewEventListener {
                        override fun onPreviewDelete(position: Int) {
                            mAdapter.remove(position)
                            mAdapter.notifyItemRemoved(position)
                        }

                        override fun onLongPressDownload(media: LocalMedia): Boolean {
                            return false
                        }
                    })
                    .startActivityPreview(position, true, mAdapter.getData())
            }

            override fun openPicture() {
                val mode: Boolean = true
                if (mode) {
                    // 进入相册
                    val selectionModel: PictureSelectionModel =
                        PictureSelector.create(this@PublishRequirementActivity)
                            .openGallery(SelectMimeType.ofImage())
                            .setSelectorUIStyle(selectorStyle)
                            .setImageEngine(GlideEngine)
                            .isDisplayTimeAxis(true)//显示资源时间轴
                            .isPageStrategy(false)//是否分页
                            .isOriginalControl(true)//是否开启原图功能
                            .isDisplayCamera(true)//是否支持相机拍摄
                            .isFastSlidingSelect(true) //是否支持滑动选择
                            .isWithSelectVideoImage(false)//是否支持图片视频同选
                            .isPreviewFullScreenMode(true)//全屏预览
                            .isPreviewZoomEffect(true)//预览缩放效果
                            .isPreviewImage(true)//是否预览图片
                            .isPreviewVideo(false)
                            .isPreviewAudio(false)
                            .isMaxSelectEnabledMask(true)//是否开启蒙版
                            .setMaxSelectNum(maxSelectNum)//多选模式下最大数量
                            .isGif(true)//是否显示gif图
                            .setSelectedData(mAdapter.getData())

                    selectionModel.forResult(launcherResult)


                }
            }
        })

    }


    /**
     * 创建一个ActivityResultLauncher
     *
     * @return
     */
    private fun createActivityResultLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val resultCode = result?.resultCode
            if (resultCode == RESULT_OK) {
                // 将选择的结果添加到mAdapter中的数据源中
                val tempList = PictureSelector.obtainSelectorList(result.data)
                analyticalSelectResults(tempList)
            } else if (resultCode == RESULT_CANCELED) {
                toast("你已经退出")
                Log.i(TAG, "onActivityResult PictureSelector Cancel")
            }
        }
    }

    /**
     * 处理选择结果
     *
     * @param result
     */
    private fun analyticalSelectResults(result: java.util.ArrayList<LocalMedia>) {
        for (media in result) {
            if (media.width == 0 || media.height == 0) {
                if (PictureMimeType.isHasImage(media.mimeType)) {
                    val imageExtraInfo = MediaUtils.getImageSize(media.path)
                    media.width = imageExtraInfo.width
                    media.height = imageExtraInfo.height
                } else if (PictureMimeType.isHasVideo(media.mimeType)) {
                    val videoExtraInfo = MediaUtils.getVideoSize(
                        PictureAppMaster.getInstance().appContext,
                        media.path
                    )
                    media.width = videoExtraInfo.width
                    media.height = videoExtraInfo.height
                }
            }
            Log.i(TAG, "文件名: " + media.fileName)
            Log.i(TAG, "是否压缩:" + media.isCompressed)
            Log.i(TAG, "压缩:" + media.compressPath)
            Log.i(TAG, "原图:" + media.path)
            Log.i(TAG, "绝对路径:" + media.realPath)
            Log.i(TAG, "是否裁剪:" + media.isCut)
            Log.i(TAG, "裁剪:" + media.cutPath)
            Log.i(TAG, "是否开启原图:" + media.isOriginal)
            Log.i(TAG, "原图路径:" + media.originalPath)
            Log.i(TAG, "沙盒路径:" + media.sandboxPath)
            Log.i(TAG, "原始宽高: " + media.width + "x" + media.height)
            Log.i(TAG, "裁剪宽高: " + media.cropImageWidth + "x" + media.cropImageHeight)
            Log.i(TAG, "文件大小: " + media.size)
        }
        runOnUiThread {
            val isMaxSize = result.size == mAdapter.selectMax
            val oldSize = mAdapter.list.size
            mAdapter.notifyItemRangeRemoved(0, if (isMaxSize) oldSize + 1 else oldSize)
            mAdapter.list.clear()
            mAdapter.list.addAll(result)
            mAdapter.notifyItemRangeInserted(0, result.size)
        }
    }

    fun onBackPress(view: View) {
        finish()
    }




    fun onSubmit(view: View) {
        if (binding.proposalTitle.text.toString() == ""){
            toast("标题不能为空")
            return
        }
        if (binding.proposalDescription.text.toString() == ""){
            toast("需求描述不能为空")
            return
        }
        if (binding.proposalDescription.text.length !in 10..1000){
            toast("需求描述至少10字符，至多1000字符")
            return
        }
        val progressDialog = DialogUtils.getLoadingDialog(this,false,"文件上传中，请稍候...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);//设置点击屏幕加载框不会取消（返回键可以取消）
        example.clear()

        // 开启子线程批量上传图片
        val job = CoroutineScope(Dispatchers.IO).async {
            mAdapter.list.forEach {
                try {
                    val readBytes =
                        contentResolver.openInputStream(it.path.toUri())?.readBytes()
                    if (readBytes != null) {
                        val json = uploadPic(readBytes)
                        if (JSONObject(json).get("key")!=null && !JSONObject(json).get("key").equals("")){
                            example.add(JSONObject(json).get("key").toString())
                            Log.i(TAG, "JSONObject(json).get(\"key\").toString() == "+JSONObject(json).get("key").toString())
                        }
                    }

                }catch (e:Exception){
                    toast(e.toString())
                    return@async false
                }
            }
            return@async true

        }

        // 调用apollo graphql的接口新增约稿
        CoroutineScope(Dispatchers.IO).launch {
            if(job.await()){
                toast("上传成功")
                progressDialog.dismiss()
                delay(1000)
                runOnUiThread{
                    val intent = Intent(this@PublishRequirementActivity,PublishRequirementSecondActivity::class.java)
                    intent.putExtra("example",example)
                    intent.putExtra("proposalTitle",proposalTitle)
                    intent.putExtra("proposalDescription",proposalDescription)
                    startActivity(intent)
                }
            }else{
                toast("上传失败")
                progressDialog.dismiss()
            }
        }



    }

    override fun onNewIntent(intent: Intent?) {
        if(intent?.getBooleanExtra("isFinish",false)==true){
            this.finish()
        }
        super.onNewIntent(intent)
    }

}