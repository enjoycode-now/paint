package cn.copaint.audience.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.activity.AppointmentDetailsActivity
import cn.copaint.audience.databinding.ItemPicProposalDetailBinding
import cn.copaint.audience.databinding.ItemUserpageEmptyViewBinding
import cn.copaint.audience.utils.GlideEngine
import cn.copaint.audience.utils.ToastUtils.app
import com.bumptech.glide.Glide
import com.wanglu.photoviewerlibrary.OnLongClickListener
import com.wanglu.photoviewerlibrary.PhotoViewer

class VericalLinearPhotoAdapter(val activity: AppointmentDetailsActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val EMPTY_TYPE = 0
    val NORMAL_TYPE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            NORMAL_TYPE -> {
                val binding =
                    ItemPicProposalDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolder(binding)
            }
            else -> {
                val binding = ItemUserpageEmptyViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                object : RecyclerView.ViewHolder(binding.root){}
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

        fun bind(activity: AppointmentDetailsActivity, position: Int){
            Log.i("chen", app.packageName)
            Glide.with(activity).load(activity.picUrlList[position]).into(binding.image)

            binding.image.setOnClickListener{
                PhotoViewer.setData(activity.picUrlList)
                    .setClickSingleImg(activity.picUrlList[position],binding.image)
                    .setCurrentPage(position)
                    .setImgContainer(activity.binding.picRecyclerview)
                    .setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
                        override fun show(iv: ImageView, url: String) {
                            // 设置自己加载图片的框架来加载图片
                            GlideEngine.loadImage(activity,url,iv)
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