package cn.copaint.audience.apollo

import android.content.Context
import okhttp3.OkHttpClient

object myOkHttpClient {
    private var instance: OkHttpClient? = null

    fun myOkHttpClient(context: Context): OkHttpClient {
        if (instance != null) {
            return instance!!
        }
        instance = OkHttpClient.Builder()
            .addNetworkInterceptor(AuthorizationInterceptor(context))
            .build()
        return instance!!
    }
}