package cn.copaint.audience.utils

import android.content.Context
import android.content.Intent
import cn.authing.core.auth.AuthenticationClient
import cn.authing.core.graphql.GraphQLException
import cn.authing.core.types.User
import cn.copaint.audience.LoginActivity
import cn.copaint.audience.utils.GrpcUtils.setToken
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object AuthingUtils {
    val authenticationClient = AuthenticationClient("61e51b153de29133842c975b")
    var user = User(arn = "", id = "", userPoolId = "")
    var biography = "这个人没有填简介啊"

    init {
        authenticationClient.appId = "61e51b16293e8847351c071a"
    }

    fun loginCheck(): Boolean {
        return if (user.id == "") {
            app.runOnUiThread {
                app.startActivity(Intent(app, LoginActivity::class.java))
            }
            false
        } else true
    }

    fun AuthenticationClient.update(): Boolean {
        val sharedPref = app.getSharedPreferences("Authing", Context.MODE_PRIVATE) ?: return false
        token = sharedPref.getString("token", "") ?: ""
        return try {
            user = getCurrentUser().execute()
            setToken(user.token ?: "")
            biography = (authenticationClient.getUdfValue().execute()["biography"] ?: "这个人没有填简介啊") as String
            true
        } catch (e: GraphQLException) {
            toast("未登录")
            false
        } catch (e: IOException) {
            toast("用户信息获取失败")
            false
        }
    }

    fun uploadAvatar(byteArray: ByteArray): String? {
        val client = OkHttpClient().newBuilder().build()
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", "avatar_${System.currentTimeMillis()}.jpg", byteArray.toRequestBody("image/jpeg".toMediaType(), 0, byteArray.size))
            .build()
        val request: Request = Request.Builder()
            .url("https://core.authing.cn/api/v2/upload?folder=photos")
            .method("POST", body)
            .build()
        val response: Response = client.newCall(request).execute()
        val url = response.body?.string() ?: ""
        return JSONObject(url).takeIf { it.get("code") == 200 }?.getJSONObject("data")?.getString("url")
    }
}
