package cn.copaint.audience.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.activity.AppointmentDetailsActivity
import cn.copaint.audience.R
import cn.copaint.audience.activity.SquareActivity
import cn.copaint.audience.databinding.FragmentItemSearchAppointmentsBinding
import cn.copaint.audience.databinding.ItemFootViewBinding
import cn.copaint.audience.utils.DateUtils
import cn.copaint.audience.utils.GlideEngine
import cn.copaint.audience.views.MyPhotoView
import com.wanglu.photoviewerlibrary.PhotoViewer


class SquareAppointmentAdapter(private val activity: SquareActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(val binding: FragmentItemSearchAppointmentsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, activity: SquareActivity) {
            val dataList = activity.squareViewModel.dataList.value!!
            binding.authorName.text = dataList[position].nickname ?: ""
            binding.title.text = dataList[position].title
            binding.description.text = dataList[position].description
            binding.date.text = DateUtils.rcfDateStr2StandardDateStrWithoutTime(
                dataList[position].createAt ?: ""
            )
            if (dataList[position].colorMode == "") {
                binding.workType.visibility = View.INVISIBLE
            } else {
                binding.workType.text = dataList[position].colorMode
            }

            binding.yuanbeiText.text =
                "${(dataList[position].balance ?: 0) * (dataList[position].stock ?: 0)}"

            dataList[position].avatar?.let {
                GlideEngine.loadImage(
                    activity,
                    it, binding.avatar
                )
            }

            if (dataList[position].example?.size ?: 0 > 0) {
                val coverPicUrl =
                    activity.resources.getString(R.string.PicUrlPrefix) + (dataList[position].example?.get(
                        0
                    )?.key ?: "")
                binding.coverPicCardView.visibility = View.VISIBLE
                GlideEngine.loadImage(activity, coverPicUrl, binding.coverPic)
                binding.coverPicCardView.setOnClickListener {
                    if (coverPicUrl.isNotBlank()) {
                        PhotoViewer.setClickSingleImg(
                            coverPicUrl,
                            binding.coverPic
                        )
                            .setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
                                override fun show(iv: ImageView, url: String) {
                                    GlideEngine.loadImage(activity, url, iv)
                                }
                            })
                            .start(activity)
                    }
                }
                // 例图的数量
                if (dataList[position].example?.size ?: 0 > 1) {
                    val num = dataList[position].example?.size!! - 1
                    binding.picCount.visibility = View.VISIBLE
                    binding.picCount.text = "+$num"
                } else {
                    binding.picCount.visibility = View.GONE
                }
            } else {
                // 用户没有上传例图的情况
                binding.coverPicCardView.visibility = View.GONE
                binding.picCount.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                val intent = Intent(activity, AppointmentDetailsActivity::class.java)
                intent.putExtra("proposalId", dataList[position].id)
                intent.putExtra("creatorNickName", dataList[position].nickname)
                intent.putExtra("creatorAvatarUri", dataList[position].avatar)
                activity.startActivity(intent)
            }
        }
    }

    class BottomViewHolder(binding:ItemFootViewBinding) : RecyclerView.ViewHolder(binding.root) {

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when(viewType){
            0->{
                val binding =
                    FragmentItemSearchAppointmentsBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                ViewHolder(binding)
            }
            else->{
                val binding: ItemFootViewBinding = ItemFootViewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                BottomViewHolder(binding)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position < activity.squareViewModel.dataList.value!!.size){
            0   //内容ViewHolder
        }else{
            1   //footViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder){
            holder.bind(position, activity)
        }

    }

    override fun getItemCount(): Int {
        return if (activity.squareViewModel.hasNextPage){
            activity.squareViewModel.dataList.value!!.size
        }else{
            activity.squareViewModel.dataList.value!!.size + 1
        }

    }



}