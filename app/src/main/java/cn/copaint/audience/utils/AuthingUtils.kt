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
import java.io.IOException

object AuthingUtils {
    val authenticationClient = AuthenticationClient("61e51b153de29133842c975b")
    var user = User(arn = "", id = "", userPoolId = "")
    var biography = "这个人没有填简介啊"

    init {
        authenticationClient.appId = "61e51b16293e8847351c071a"
    }

    fun AuthenticationClient.update(): Boolean {
        val sharedPref = app.getSharedPreferences("Authing", Context.MODE_PRIVATE) ?: return false
        token = sharedPref.getString("token", "") ?: ""
        return try {
            user = getCurrentUser().execute()
            setToken(user.token ?: "")
            true
        } catch (e: GraphQLException) {
            toast("未登录")
            false
        } catch (e: IOException) {
            toast("用户信息获取失败")
            false
        }
    }
}
