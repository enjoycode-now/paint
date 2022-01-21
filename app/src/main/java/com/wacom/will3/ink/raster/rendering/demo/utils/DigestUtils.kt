package com.wacom.will3.ink.raster.rendering.demo.utils

import java.security.MessageDigest

fun String.getDigest(algorithm: String): String {
    val md = MessageDigest.getInstance(algorithm)
    md.update(toByteArray())
    return md.digest().joinToString("") { "%02x".format(it) }
}