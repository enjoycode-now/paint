package cn.copaint.audience.apollo

import android.content.Context
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object myOkHttpClient {
    private var instance: OkHttpClient? = null

    fun myOkHttpClient(context: Context): OkHttpClient {
        if (instance != null) {
            return instance!!
        }
        instance = OkHttpClient.Builder()
            .addNetworkInterceptor(AuthorizationInterceptor(context))
            .retryOnConnectionFailure(true)
            .connectTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(15L, TimeUnit.SECONDS)
            .build()
        return instance!!
    }
}