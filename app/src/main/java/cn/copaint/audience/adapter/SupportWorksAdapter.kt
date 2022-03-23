package cn.copaint.audience.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.activity.UserActivity
import cn.copaint.audience.databinding.ItemSupportWorksBinding
import cn.copaint.audience.utils.GlideEngine
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.views.MyPhotoView
import com.bumptech.glide.Glide
import com.wanglu.photoviewerlibrary.OnLongClickListener
import com.wanglu.photoviewerlibrary.PhotoViewer

class SupportWorksAdapter(private val activity: UserActivity) : RecyclerView.Adapter<SupportWorksAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSupportWorksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activity.userViewModel.picUrlList.value?.get(position) ?: "", position, activity)
    }

    override fun getItemCount() = activity.userViewModel.picUrlList.value?.size ?: 0

    class ViewHolder(val binding: ItemSupportWorksBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String, position: Int, activity: UserActivity) {
            Glide.with(activity)
                .load(url)
                .centerCrop()
                .into(binding.image)

            binding.image.setOnClickListener {
                toast("你点击了第${position + 1}张图片")
                MyPhotoView.setData(activity.userViewModel.picUrlList.value!!)
                    .setImgContainer(activity.binding.supportWorksRecyclerView)
                    .setCurrentPage(position)
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
