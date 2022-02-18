package cn.copaint.audience.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.PayActivity
import cn.copaint.audience.databinding.ItemYuanbeiDetailBinding
import cn.copaint.audience.type.BalanceRecordAction
import cn.copaint.audience.type.BalanceRecordType
import cn.copaint.audience.utils.ToastUtils.toast
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

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
            if (list[position].balanceRecordType.equals(BalanceRecordType.OBTAIN)) {
                type = "+"
            } else {
                type = "-"
            }
            binding.detailNum.setText(type + list[position].balance.toString())
            binding.createAt.setText("时间：" + rcfDateStr2DateStr(list[position].createAt))
            when (list[position].balanceRecordAction) {
                BalanceRecordAction.TOP_UP -> {
                    binding.purchasedWorkName.setText("元贝充值")
                }
                BalanceRecordAction.TRANSACTION -> {
                    binding.purchasedWorkName.setText("交易所得")
                }
                else -> {
                    binding.purchasedWorkName.setText("其他")
                }
            }

            binding.cardview.setOnClickListener {
                toast("你点击了第${position + 1}条账单")
            }
        }

        // 将rcf339时间字符串 转换为 标准时间字符串
        fun rcfDateStr2DateStr(str: String): String {
//            var instant = Instant.parse(str)
//            instant.to

            val cal = Calendar.getInstance(Locale.getDefault())
            val sdf = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss")
            val sdf2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            cal.time = sdf.parse(str.replace("Z", "").replace("T", "-"))
            return sdf2.format(cal.time)
        }

    }


}
