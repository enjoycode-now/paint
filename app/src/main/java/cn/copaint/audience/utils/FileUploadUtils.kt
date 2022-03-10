package cn.copaint.audience.utils

import android.content.Context
import android.util.Log
import cn.copaint.audience.apollo.myOkHttpClient
import cn.copaint.audience.apollo.myOkHttpClient.myOkHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
     * 上传结果放在callback的onResponse
     */
    fun uploadPic(context: Context, byteArray: ByteArray, callback: Callback) {
        if (byteArray.size > maxImageFileSize) {
            ToastUtils.toast("图片超过5MB,请更换一张")
            return
        }
        var mediaType: MediaType = "image/*".toMediaType()
        var body: RequestBody = byteArray.toRequestBody(mediaType)
        var request: Request = Request.Builder()
            .url("http://120.78.173.15:20000/upload")
            .method("POST", body)
            .addHeader("x-file-size", byteArray.size.toString())
            .addHeader("x-file-type", "image")
            .addHeader("Content-Type", "image/*")
            .build()
        try {
            myOkHttpClient(context).newCall(request).enqueue(callback)
        } catch (e: Exception) {
            ToastUtils.toast(e.toString())
        }
        return
    }

    /**
     * 上传视频
     * 上传结果放在callback的onResponse
     */
    fun uploadVideo(context: Context, byteArray: ByteArray, callback: Callback) {
        if (byteArray.size > maxVideoFileSize) {
            ToastUtils.toast("视频超过200MB,请更换一个")
            return
        }
        var mediaType: MediaType = "image/*".toMediaType()
        var body: RequestBody = byteArray.toRequestBody(mediaType)
        var request: Request = Request.Builder()
            .url("http://120.78.173.15:20000/upload")
            .method("POST", body)
            .addHeader("x-file-size", byteArray.size.toString())
            .addHeader("x-file-type", "video")
            .addHeader("Content-Type", "video/*")
            .build()

        try {
            myOkHttpClient(context).newCall(request).enqueue(callback)
        } catch (e: Exception) {
            ToastUtils.toast(e.toString())
        }
    }

    /**
     * 上传图片方式2
     * 异步多线程上传，返回json结果
     */
    suspend fun uploadPic(context: Context,byteArray: ByteArray): String {
        if (byteArray.size > maxImageFileSize) {
            ToastUtils.toast("图片超过5MB,请更换一张")
            return ""
        }
        var mediaType: MediaType = "image/*".toMediaType()
        var body: RequestBody = byteArray.toRequestBody(mediaType)
        var request: Request = Request.Builder()
            .url("http://120.78.173.15:20000/upload")
            .method("POST", body)
            .addHeader("x-file-size", byteArray.size.toString())
            .addHeader("x-file-type", "image")
            .addHeader("Content-Type", "image/*")
            .build()
        val job = CoroutineScope(Dispatchers.IO).async {
            try {
                val response = myOkHttpClient(context).newCall(request).execute()
                return@async response.body?.string()
            } catch (e: Exception) {
                ToastUtils.toast(e.toString())
            }
        }
        return job.await().toString()
    }



}