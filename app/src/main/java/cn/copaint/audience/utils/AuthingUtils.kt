package cn.copaint.audience.utils

import cn.authing.core.auth.AuthenticationClient
import cn.authing.core.types.User

object AuthingUtils {
    val authenticationClient = AuthenticationClient("61e51b16293e8847351c071a","https://89yxa5.authing.cn")

    var user = User(arn = "",id="", userPoolId ="")
    var biography = "这个人没有填简介啊"
}