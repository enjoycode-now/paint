package com.wacom.will3.ink.raster.rendering.demo.utils

import cn.authing.core.auth.AuthenticationClient
import cn.authing.core.types.User

object AuthingUtils {
    val authenticationClient = AuthenticationClient("61e51b16293e8847351c071a","https://89yxa5.authing.cn")

    lateinit var user: User
}