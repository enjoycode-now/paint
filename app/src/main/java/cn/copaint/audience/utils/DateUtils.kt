package cn.copaint.audience.utils

import cn.authing.core.types.S
import com.apollographql.apollo3.mpp.currentTimeMillis
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object DateUtils {

    // 将rcf339时间字符串 转换为 自定义时间字符串
    fun rcfDateStr2DateStr(str: String?): String {
        if (str.isNullOrBlank() || str == "null") {
            return ""
        }
        val instant = Instant.parse(str)
        val datetime =
            LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId())
        return DateTimeFormatter.ofPattern("创建于 yyyy 年 MM 月 dd 日 HH:mm:ss")
            .format(datetime)
    }

    // 将rcf339时间字符串 转换为 标准时间字符串
    fun rcfDateStr2StandardDateStr(str: String?): String {
        if (str.isNullOrBlank() || str == "null") {
            return ""
        }
        val instant = Instant.parse(str)
        val datetime =
            LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId())
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .format(datetime)
    }

    // 将rcf339时间字符串 转换为 自定义日期字符串
    fun rcfDateStr2StandardDateStrWithoutTime(str: String?): String {
        if (str.isNullOrBlank() || str == "null") {
            return ""
        }
        val instant = Instant.parse(str)
        val datetime =
            LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId())
        return DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .format(datetime)
    }


    // 得到当前rcf339时间字符串
    fun getCurrentRcfDateStr(): String {
        val instant = Instant.now()
        instant.atZone(ZoneId.systemDefault())
        return instant.toString()
    }

    /**
     * @param str rcf339时间字符串
     * @return  true-已过期 false-未过期
     */
    fun judgeIsExpired(str: String?): Boolean {
        if (str.isNullOrBlank() || str == "null") {
            return false
        }
        val instantExpired = Instant.parse(str)
        val instantNow = Instant.now()
        return instantExpired < instantNow
    }
}