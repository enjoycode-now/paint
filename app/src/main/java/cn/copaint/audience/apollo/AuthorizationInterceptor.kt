package cn.copaint.audience.apollo

import android.content.Context
import cn.copaint.audience.utils.AuthingUtils
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(val context: Context): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${AuthingUtils.user.token ?: ""}")
            .build()

        return chain.proceed(request)
    }
}