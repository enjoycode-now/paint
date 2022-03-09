package cn.copaint.audience.model

import cn.copaint.audience.FindProposalsQuery

data class Proposal (
    val title: String? = "",
    val description: String? = "",
    var requirementType: String? = "", //需求类型
    var expiredAt: String? = "",// 截稿日期
    var createAt: String? = "",
    var workStyle: String? = "", // 作品风格
    var colorMode: String? = "", // 颜色模式
    var dimensions: String? = "", // 尺寸规格
    var wordFormat: String? = "", // 稿件格式
    var acceptancePhase: String? = "",
    var balance: Int? = 0,
    val stock: Int? = 0,
    val example: List<FindProposalsQuery.Example>?,
    var creatorId: String? = "",
    var nickname: String? = "",
    var avatar: String? = ""
)