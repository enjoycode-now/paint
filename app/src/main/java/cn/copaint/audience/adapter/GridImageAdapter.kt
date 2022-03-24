package cn.copaint.audience.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import cn.copaint.audience.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.luck.picture.lib.adapter.holder.PreviewGalleryAdapter
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.utils.DateUtils
import java.io.File

class GridImageAdapter(context: Context?, result: List<LocalMedia>?) :
    RecyclerView.Adapter<GridImageAdapter.ViewHolder>() {
    val TAG = "PictureSelector"
    private val TYPE_CAMERA = 1
    val TYPE_PICTURE = 2
    var mInflater: LayoutInflater? = null
    val list = ArrayList<LocalMedia>()
    var selectMax = 9

    /**
     * 删除
     */
    fun delete(position: Int) {
        try {
            if (position != RecyclerView.NO_POSITION && list.size > position) {
                list.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, list.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init{
        mInflater = LayoutInflater.from(context)
        list.addAll(result!!)
    }



    fun getData(): ArrayList<LocalMedia>? {
        return list
    }

    fun remove(position: Int) {
        if (position < list.size) {
            list.removeAt(position)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mImg: ImageView
        var mIvDel: ImageView
        var tvDuration: TextView

        init {
            mImg = view.findViewById(R.id.fiv)
            mIvDel = view.findViewById(R.id.iv_del)
            tvDuration = view.findViewById(R.id.tv_duration)
        }
    }

    override fun getItemCount(): Int {
        return if (list.size < selectMax) {
            list.size + 1
        } else {
            list.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isShowAddItem(position)) {
            TYPE_CAMERA
        } else {
            TYPE_PICTURE
        }
    }

    /**
     * 创建ViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view: View = mInflater?.inflate(R.layout.item_select_pic, viewGroup, false)!!
        return ViewHolder(view)
    }

    private fun isShowAddItem(position: Int): Boolean {
        val size = list.size
        return position == size
    }

    /**
     * 设置值
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        //少于MaxSize张，显示继续添加的图标
        if (getItemViewType(position) == TYPE_CAMERA) {
            viewHolder.mImg.setImageResource(R.drawable.ic_add_light)
            viewHolder.mImg.setPadding(25)
            viewHolder.mImg.setOnClickListener {
                if (mItemClickListener != null) {
                    mItemClickListener!!.openPicture()
                }
            }
            viewHolder.mIvDel.visibility = View.INVISIBLE
        } else {
            viewHolder.mIvDel.visibility = View.VISIBLE
            viewHolder.mImg.setPadding(0)
            viewHolder.mIvDel.setOnClickListener { view: View? ->
                val index = viewHolder.absoluteAdapterPosition
                if (index != RecyclerView.NO_POSITION && list.size > index) {
                    list.removeAt(index)
                    notifyItemRemoved(index)
                    notifyItemRangeChanged(index, list.size)
                }
            }
            val media = list[position]
            val chooseModel = media.chooseModel
            val path: String
            path = if (media.isCut && !media.isCompressed) {
                // 裁剪过
                media.cutPath
            } else if (media.isCut || media.isCompressed) {
                // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                media.compressPath
            } else {
                // 原图
                media.path
            }
            Log.i(TAG, "原图地址::" + media.path)
            if (media.isCut) {
                Log.i(TAG, "裁剪地址::" + media.cutPath)
            }
            if (media.isCompressed) {
                Log.i(TAG, "压缩地址::" + media.compressPath)
                Log.i(TAG, "压缩后文件大小::" + File(media.compressPath).length() / 1024 + "k")
            }
            if (media.isToSandboxPath) {
                Log.i(TAG, "Android Q特有地址::" + media.sandboxPath)
            }
            if (media.isOriginal) {
                Log.i(TAG, "是否开启原图功能::" + true)
                Log.i(TAG, "开启原图功能后地址::" + media.originalPath)
            }
            val duration = media.duration
            viewHolder.tvDuration.visibility =
                if (PictureMimeType.isHasVideo(media.mimeType)) View.VISIBLE else View.GONE
            if (chooseModel == SelectMimeType.ofAudio()) {
                viewHolder.tvDuration.visibility = View.VISIBLE
                viewHolder.tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ps_ic_audio,
                    0,
                    0,
                    0
                )
            } else {
                viewHolder.tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ps_ic_video,
                    0,
                    0,
                    0
                )
            }
            viewHolder.tvDuration.text = DateUtils.formatDurationTime(duration)
            if (chooseModel == SelectMimeType.ofAudio()) {
                viewHolder.mImg.setImageResource(R.drawable.ps_audio_placeholder)
            } else {
                Glide.with(viewHolder.itemView.context)
                    .load(
                        if (PictureMimeType.isContent(path) && !media.isCut && !media.isCompressed) Uri.parse(
                            path
                        ) else path
                    )
                    .centerCrop()
                    .placeholder(R.color.ps_color_fa632d)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(viewHolder.mImg)
            }
            //itemView 的点击事件
            if (mItemClickListener != null) {
                viewHolder.itemView.setOnClickListener { v: View? ->
                    val adapterPosition = viewHolder.absoluteAdapterPosition
                    mItemClickListener!!.onItemClick(v, adapterPosition)
                }
            }
            if (mItemLongClickListener != null) {
                viewHolder.itemView.setOnLongClickListener { v: View? ->
                    val adapterPosition = viewHolder.absoluteAdapterPosition
                    mItemLongClickListener!!.onItemLongClick(viewHolder, adapterPosition, v)
                    true
                }
            }
        }
    }

    private var mItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(l: OnItemClickListener?) {
        mItemClickListener = l
    }

    interface OnItemClickListener {
        /**
         * Item click event
         *
         * @param v
         * @param position
         */
        fun onItemClick(v: View?, position: Int)

        /**
         * Open PictureSelector
         */
        fun openPicture()
    }

    private var mItemLongClickListener: PreviewGalleryAdapter.OnItemLongClickListener? = null

    fun setItemLongClickListener(l: PreviewGalleryAdapter.OnItemLongClickListener?) {
        mItemLongClickListener = l
    }
}