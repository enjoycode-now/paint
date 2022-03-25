package cn.copaint.audience.model

import cn.copaint.audience.type.BalanceRecordAction
import cn.copaint.audience.type.BalanceRecordType

class BalanceRecord(
    val id: String,
    var balance: Double, // 元贝数值
    var balanceRecordAction: BalanceRecordAction,// 充值 || 交易 || 约稿
    var balanceRecordType: BalanceRecordType,// 收入 || 支出 || 未知
    var createAt: String
)
