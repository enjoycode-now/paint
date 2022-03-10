package cn.copaint.audience.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.R
import cn.copaint.audience.SquareActivity
import cn.copaint.audience.UserActivity
import cn.copaint.audience.databinding.ActivitySquareBinding
import cn.copaint.audience.databinding.FragmentItemSearchAppointmentsBinding
import cn.copaint.audience.fragment.SearchAppointmentFragment
import cn.copaint.audience.utils.DateUtils
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope

class SquareAppointmentAdapter(private val activity: SquareActivity): RecyclerView.Adapter<SquareAppointmentAdapter.ViewHolder>() {
    class ViewHolder(val binding:FragmentItemSearchAppointmentsBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, activity: SquareActivity) {
            binding.authorName.text = activity.dataList[position].nickname
            binding.title.text = activity.dataList[position].title
            binding.description.text = activity.dataList[position].description
            binding.date.text = DateUtils.rcfDateStr2StandardDateStrWithoutTime(
                activity.dataList[position].createAt ?: ""
            )
            if (activity.dataList[position].colorMode == ""){
                binding.workType.visibility = View.INVISIBLE
            }else{
                binding.workType.text = activity.dataList[position].colorMode
            }


            binding.yuanbeiText.text = "${(activity.dataList[position].balance ?:0) * (activity.dataList[position].stock ?:0)}"
            Glide.with(activity)
                .load(activity.dataList[position].avatar)
                .into(binding.avatar)
            if(activity.dataList[position].example?.size?:0 > 0){
                val coverPicUrl =  activity.resources.getString(R.string.PicUrlPrefix)+(activity.dataList[position].example?.get(0)?.key ?: "")

                Glide.with(activity)
                    .load(coverPicUrl)
                    .error(R.drawable.loading_failed)
                    .into(binding.coverPic)
            }else{
                // 用户没有上传例图的情况
                binding.coverPic.visibility = View.GONE
            }
            binding.description.setOnClickListener {
                if (binding.description.ellipsize == null ){
                    binding.description.ellipsize = TextUtils.TruncateAt.END
                    binding.description.setLines(2)
                }else{
                    binding.description.ellipsize = null
                    binding.description.isSingleLine = false
                }
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SquareAppointmentAdapter.ViewHolder {
        val binding =
            FragmentItemSearchAppointmentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position,activity)
    }

    override fun getItemCount() = activity.dataList.size


}