package cn.copaint.audience.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.R
import cn.copaint.audience.activity.AppointmentDetailsActivity
import cn.copaint.audience.databinding.*
import cn.copaint.audience.fragment.CommonItemFragment
import cn.copaint.audience.utils.DateUtils
import com.bumptech.glide.Glide


class CommonItemAdapter(val fragment: CommonItemFragment, val type: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 未完成 进行中 已完成 已中断
    val UnStart = 0
    val OnGoing = 1
    val HasDone = 2
    val DisContinue = 3
    val Empty = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            UnStart -> {
                val binding =
                    FragmentItemSearchAppointmentsBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                UnStartViewHolder(binding)
            }
            OnGoing -> {
                val binding = ItemUserpageEmptyViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                object : RecyclerView.ViewHolder(binding.root) {}
            }
            HasDone -> {
                val binding = ItemUserpageEmptyViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                object : RecyclerView.ViewHolder(binding.root) {}
            }
            DisContinue -> {
                val binding = ItemUserpageEmptyViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                object : RecyclerView.ViewHolder(binding.root) {}
            }
            else -> {
                val binding = ItemUserpageEmptyViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                object : RecyclerView.ViewHolder(binding.root) {}
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && fragment.dataList.isEmpty()) {
            Empty
        } else {
            type
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UnStartViewHolder -> {
                holder.bind(fragment, position)
            }
            else -> {}
        }
    }

    override fun getItemCount(): Int {
        return if (fragment.dataList.isEmpty())
            1
        else
            fragment.dataList.size
    }


    class UnStartViewHolder(val binding: FragmentItemSearchAppointmentsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(fragment: CommonItemFragment, position: Int) {
            binding.authorName.text = fragment.dataList[position].nickname
            binding.title.text = fragment.dataList[position].title
            binding.description.text = fragment.dataList[position].description
            binding.date.text =
                DateUtils.rcfDateStr2StandardDateStrWithoutTime(
                    fragment.dataList[position].createAt ?: ""
                )
            if (fragment.dataList[position].colorMode == "") {
                binding.workType.visibility = View.INVISIBLE
            } else {
                binding.workType.text = fragment.dataList[position].colorMode
            }

            binding.yuanbeiText.text =
                "${(fragment.dataList[position].balance ?: 0) * (fragment.dataList[position].stock ?: 0)}"
            Glide.with(fragment)
                .load(fragment.dataList[position].avatar)
                .into(binding.avatar)

            // 将例图列表的首图作为封面图
            if (fragment.dataList[position].example?.size ?: 0 > 0) {
                binding.coverPic.visibility = View.VISIBLE
                val coverPicUrl =
                    fragment.resources.getString(R.string.PicUrlPrefix) + (fragment.dataList[position].example?.get(
                        0
                    )?.key ?: "")
                Glide.with(fragment)
                    .load(coverPicUrl)
                    .error(R.drawable.loading_failed)
                    .into(binding.coverPic)

                // 例图的数量
                if (fragment.dataList[position].example?.size ?: 0 > 1) {
                    val num = fragment.dataList[position].example?.size!! - 1
                    binding.picCount.visibility = View.VISIBLE
                    binding.picCount.text = "+$num"
                } else {
                    binding.picCount.visibility = View.GONE
                }
            } else {
                // 用户没有上传例图
                binding.coverPicCardView.visibility = View.GONE
                binding.picCount.visibility = View.GONE
            }
            binding.description.setOnClickListener {
                val intent = Intent(fragment.activity, AppointmentDetailsActivity::class.java)
                intent.putExtra("proposalId", fragment.dataList[position].proposalId)
                intent.putExtra("creatorNickName", fragment.dataList[position].nickname)
                intent.putExtra("creatorAvatarUri", fragment.dataList[position].avatar)
                fragment.startActivity(intent)
            }
        }

    }
}