package cn.copaint.audience.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.AppointmentDetailsActivity
import cn.copaint.audience.R
import cn.copaint.audience.databinding.ItemPicProposalDetailBinding
import cn.copaint.audience.databinding.ItemUserpageEmptyViewBinding
import com.bumptech.glide.Glide
import com.luck.picture.lib.photoview.PhotoView
import com.wanglu.photoviewerlibrary.OnLongClickListener
import com.wanglu.photoviewerlibrary.PhotoViewer

class VericalLinearPhotoAdapter(val activity: AppointmentDetailsActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val EMPTY_TYPE = 0
    val NORMAL_TYPE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            NORMAL_TYPE -> {
                val binding =
                    ItemPicProposalDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding)
            }
            else -> {
                val binding = ItemUserpageEmptyViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return object : RecyclerView.ViewHolder(binding.root){}
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (activity.picUrlList.size == 0)
            EMPTY_TYPE
        else
            NORMAL_TYPE
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder){
            activity.picUrlList[position].let {
                holder.bind(activity,position)
            }
        }
    }

    override fun getItemCount(): Int{
        return if (activity.picUrlList.isEmpty())
            1
        else
            activity.picUrlList.size
    }


    class ViewHolder(val binding: ItemPicProposalDetailBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: AppointmentDetailsActivity,position: Int){
            Glide.with(activity).load(activity.picUrlList[position]).into(binding.image)

            binding.image.setOnClickListener{
                PhotoViewer.setData(activity.picUrlList)
                    .setCurrentPage(position)
                    .setImgContainer(activity.binding.picRecyclerview)
                    .setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
                        override fun show(iv: ImageView, url: String) {
                            // 设置自己加载图片的框架来加载图片
                            Glide.with(activity).load(url).into(iv)
                        }
                    })
                    .setOnLongClickListener(object : OnLongClickListener {
                        override fun onLongClick(view: View) {
                            // 长按图片的逻辑
                        }
                    })
                    .start(activity)
            }
        }
    }
}