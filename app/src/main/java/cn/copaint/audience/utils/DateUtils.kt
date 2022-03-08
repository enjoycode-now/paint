package cn.copaint.audience.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object DateUtils {

    // 将rcf339时间字符串 转换为 自定义时间字符串
    fun rcfDateStr2DateStr(str: String): String {
        if (str == ""){
            return ""
        }
        val instant = Instant.parse(str)
        val datetime =
            LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId())
        return DateTimeFormatter.ofPattern("创建于 yyyy 年 MM 月 dd 日 HH:mm:ss")
            .format(datetime)
    }

    // 将rcf339时间字符串 转换为 标准时间字符串
    fun rcfDateStr2StandardDateStr(str: String): String {
        if (str == ""){
            return ""
        }
        val instant = Instant.parse(str)
        val datetime =
            LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId())
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .format(datetime)
    }

    // 将rcf339时间字符串 转换为 自定义日期字符串
    fun rcfDateStr2StandardDateStrWithoutTime(str: String): String {
        if (str == ""){
            return ""
        }
        val instant = Instant.parse(str)
        val datetime =
            LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId())
        return DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .format(datetime)
    }
}