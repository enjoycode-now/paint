package cn.copaint.audience.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.copaint.audience.HomePageActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import cn.copaint.audience.R
import cn.copaint.audience.databinding.FragmentItemLiveBinding
import cn.copaint.audience.utils.BitmapUtils.picQueue
import cn.copaint.audience.utils.ToastUtils.app
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ItemLiveFragment : Fragment() {

    lateinit var binding: FragmentItemLiveBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        binding = FragmentItemLiveBinding.inflate(layoutInflater,container,false)

        Glide.with(this)
            .load(picQueue.removeFirst())
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerCrop()
            .into(binding.image)

        CoroutineScope(Dispatchers.Default).launch {
            if(picQueue.size<24)repeat(8){
                delay(125)
                picQueue.add("https://api.ghser.com/random/pe.php")
            }
        }

        binding.toolbar.likeBtn.setOnClickListener{
            binding.toolbar.likeBtn.isLiked = !binding.toolbar.likeBtn.isLiked
        }

        return binding.root
    }

}