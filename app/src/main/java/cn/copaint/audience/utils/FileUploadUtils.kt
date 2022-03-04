package cn.copaint.audience.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.lang.Exception

object FileUploadUtils {
    const val maxImageFileSize = 5 shl 20
    const val maxVideoFileSize = 200 shl 20

    /**
     * 上传图片
     */
    fun uploadPic(context: Context, byteArray: ByteArray, callback: Callback) : Boolean{
        if (byteArray.size > maxImageFileSize) {
            ToastUtils.toast("图片超过5MB,请更换一张")
            return false
        }
        var client = OkHttpClient().newBuilder()
            .build()
        var mediaType: MediaType = "image/*".toMediaType()
        var body: RequestBody = byteArray.toRequestBody(mediaType)
        var request: Request = Request.Builder()
            .url("http://120.78.173.15:20000/upload")
            .method("POST", body)
            .addHeader("x-file-size", byteArray.size.toString())
            .addHeader("x-file-type", "image")
            .addHeader("Content-Type", "image/*")
            .build()
//        CoroutineScope(Dispatchers.IO).launch {
//            var response: Response = try {
//                client.newCall(request).execute()
//            } catch (e: Exception) {
//                ToastUtils.toast(e.toString())
//                return@launch
//            }
//        }
        try {
            client.newCall(request).enqueue(callback)
        } catch (e: Exception) {
            ToastUtils.toast(e.toString())
        }
        return true
    }


    /**
     * 上传图片
     */
    fun uploadVideo(context: Context, byteArray: ByteArray, callback: Callback) : Boolean{
        if (byteArray.size > maxVideoFileSize) {
            ToastUtils.toast("视频超过200MB,请更换一个")
            return false
        }
        var client = OkHttpClient().newBuilder()
            .build()
        var mediaType: MediaType = "image/*".toMediaType()
        var body: RequestBody = byteArray.toRequestBody(mediaType)
        var request: Request = Request.Builder()
            .url("http://120.78.173.15:20000/upload")
            .method("POST", body)
            .addHeader("x-file-size", byteArray.size.toString())
            .addHeader("x-file-type", "video")
            .addHeader("Content-Type", "video/*")
            .build()
//        CoroutineScope(Dispatchers.IO).launch {
//            var response: Response = try {
//                client.newCall(request).execute()
//            } catch (e: Exception) {
//                ToastUtils.toast(e.toString())
//                return@launch
//            }
//        }
        try {
            client.newCall(request).enqueue(callback)
        } catch (e: Exception) {
            ToastUtils.toast(e.toString())
        }
        return true
    }
}