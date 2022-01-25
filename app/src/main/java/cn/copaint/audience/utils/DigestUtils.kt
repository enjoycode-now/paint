package cn.copaint.audience.utils

import java.security.MessageDigest

fun String.getDigest(algorithm: String): String {
    val md = MessageDigest.getInstance(algorithm)
    md.update(toByteArray())
    return md.digest().joinToString("") { "%02x".format(it) }
}