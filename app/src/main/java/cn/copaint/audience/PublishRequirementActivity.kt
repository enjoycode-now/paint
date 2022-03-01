package cn.copaint.audience

import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.adapter.GridImageAdapter
import cn.copaint.audience.databinding.ActivityPublishRequirementBinding
import cn.copaint.audience.utils.GlideEngine
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.luck.picture.lib.app.PictureAppMaster
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.PictureSelectionConfig.selectorStyle
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.utils.MediaUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File


class PublishRequirementActivity : AppCompatActivity() {
    private val TAG = "chenlin"
    lateinit var binding: ActivityPublishRequirementBinding
    val photoList = arrayListOf<LocalMedia>()
    lateinit var mAdapter: GridImageAdapter
    val maxSelectNum = 10
    lateinit var launcherResult: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublishRequirementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        selectorStyle = PictureSelectorStyle()
        // 注册需要写在onCreate或Fragment onAttach里，否则会报java.lang.IllegalStateException异常
        launcherResult = createActivityResultLauncher()


        binding.imageView8.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mAdapter = GridImageAdapter(this, photoList)
        binding.imageView8.adapter = mAdapter

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


        // 清除缓存
//        clearCache()

    }






    /**
     * 创建一个ActivityResultLauncher
     *
     * @return
     */
    private fun createActivityResultLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult?> {


                override fun onActivityResult(result: ActivityResult?) {
                    val resultCode = result?.resultCode
                    if (resultCode == RESULT_OK) {
                        val selectList = PictureSelector.obtainSelectorList(result.data)
                        analyticalSelectResults(selectList)
                    } else if (resultCode == RESULT_CANCELED) {
                        Log.i("chenlin", "onActivityResult PictureSelector Cancel")
                    }
                }
            })
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

    fun onclick(view: View) {
        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine)
            .setSelectionMode(1)
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {
                    GlideEngine.loadGridImage(this@PublishRequirementActivity, result?.get(0)?.realPath.toString(),binding.imageView10)
                    val inputStream = result?.get(0)?.path?.toUri()
                        ?.let { contentResolver.openInputStream(it) }
                    uploadAvatar(inputStream?.readBytes()!!)
                }
                override fun onCancel() {
                    toast("你已经退出")
                }
            })
    }


    /**
     * 上传图片
     */
    fun uploadAvatar(byteArray: ByteArray) {
        var client = OkHttpClient().newBuilder()
            .build()
        var mediaType: MediaType = "image/*".toMediaType()
        var body: RequestBody = byteArray.toRequestBody(mediaType)
        var request: Request = Request.Builder()
            .url("http://120.78.173.15:20000/upload")
            .method("POST", body)
            .addHeader("x-file-size", byteArray.size.toString())
            .addHeader("Content-Type", "image/*")
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            var response = client.newCall(request).execute()
            val url = response.body?.string() ?: ""
            runOnUiThread{
                toast(url)
                Log.i(TAG, url)
            }
        }
    }

}