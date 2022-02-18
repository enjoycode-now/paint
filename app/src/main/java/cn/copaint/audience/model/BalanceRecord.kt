package cn.copaint.audience.model

import cn.copaint.audience.type.BalanceRecordAction
import cn.copaint.audience.type.BalanceRecordType

class BalanceRecord(
    val id: String,
    var balance: Double,
    var balanceRecordAction: BalanceRecordAction,
    var balanceRecordType: BalanceRecordType,
    var createAt: String
)
