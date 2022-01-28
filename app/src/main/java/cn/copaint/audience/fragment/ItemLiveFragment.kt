package cn.copaint.audience.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.copaint.audience.databinding.FragmentItemLiveBinding
import cn.copaint.audience.utils.BitmapUtils.picQueue
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ItemLiveFragment : Fragment() {

    lateinit var binding: FragmentItemLiveBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentItemLiveBinding.inflate(layoutInflater, container, false)

        CoroutineScope(Dispatchers.Default).launch {
            var url = picQueue.removeLastOrNull()
            while (url.isNullOrEmpty()) {
                delay(150)
                url = picQueue.removeLastOrNull()
            }
            picQueue.add("https://api.ghser.com/random/pe.php")
            activity?.runOnUiThread {
                Glide.with(this@ItemLiveFragment)
                    .load(url)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .into(binding.image)
            }
        }

        binding.toolbar.likeBtn.setOnClickListener {
            binding.toolbar.likeBtn.isLiked = !binding.toolbar.likeBtn.isLiked
        }

        return binding.root
    }
}
