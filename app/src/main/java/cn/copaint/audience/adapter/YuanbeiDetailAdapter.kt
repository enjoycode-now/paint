package cn.copaint.audience.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.PayActivity
import cn.copaint.audience.databinding.ItemYuanbeiDetailBinding
import cn.copaint.audience.type.BalanceRecordAction
import cn.copaint.audience.type.BalanceRecordType
import cn.copaint.audience.utils.ToastUtils.toast
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import java.text.SimpleDateFormat as SimpleDateFormat1

class YuanbeiDetailAdapter(private var activity: PayActivity) :
    RecyclerView.Adapter<YuanbeiDetailAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemYuanbeiDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, activity)
    }

    override fun getItemCount(): Int {
        return activity.YuanbeiDetailList.size
    }

    class ViewHolder(val binding: ItemYuanbeiDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, activity: PayActivity) {

            val list = activity.YuanbeiDetailList
            lateinit var type: String
            if (list[position].balanceRecordType == BalanceRecordType.OBTAIN) {
                type = "+"
            } else {
                type = "-"
            }
            binding.detailNum.text = type + list[position].balance.toString()
            binding.createAt.text = rcfDateStr2DateStr(list[position].createAt)

            when (list[position].balanceRecordAction) {
                BalanceRecordAction.TOP_UP -> {
                    binding.purchasedWorkName.text = "元贝充值"
                    binding.purchasedWorkShare.text = ""
                }
                BalanceRecordAction.TRANSACTION -> {
                    binding.purchasedWorkName.text = "交易所得"
                    binding.purchasedWorkShare.text = "份额xx%"
                }
                else -> {
                    binding.purchasedWorkName.text = "其他"
                    binding.purchasedWorkShare.text = ""
                }
            }

            binding.cardview.setOnClickListener {
                toast("你点击了第${position + 1}条账单")
            }
        }

        // 将rcf339时间字符串 转换为 标准时间字符串
        fun rcfDateStr2DateStr(str: String): String {
            val instant = Instant.parse(str)
            val datetime =
                LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId())
            return DateTimeFormatter.ofPattern("创建于 yyyy 年 MM 月 dd 日 HH:mm:ss")
                .format(datetime)
        }

    }


}
