package com.wacom.will3.ink.raster.rendering.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import cn.authing.core.graphql.GraphQLException
import com.wacom.will3.ink.raster.rendering.demo.fragment.LiveFragment
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityHomePageBinding
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.app
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomePageActivity : AppCompatActivity() {
    lateinit var binding : ActivityHomePageBinding
    val photoUri = "https://api.ghser.com/random/pe.php"
    val picList = mutableListOf(LiveFragment(photoUri),LiveFragment(photoUri),LiveFragment(photoUri))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        binding.mainViewPager.apply {
            offscreenPageLimit=2
            adapter = ScreenSlidePagerAdapter(this@HomePageActivity)
        }


        binding.homePageBtn.setOnClickListener{

        }


        binding.myPageBtn.setOnClickListener{

        }

        CoroutineScope(Dispatchers.IO).launch {
            val sharedPref = app.getSharedPreferences("Authing", Context.MODE_PRIVATE) ?: return@launch
            AuthingUtils.authenticationClient.token = sharedPref.getString("token","") ?: ""
            try {
                AuthingUtils.user = AuthingUtils.authenticationClient.getCurrentUser().execute()
            }catch (e:GraphQLException){
                runOnUiThread { startActivity(Intent(app, LoginActivity::class.java)) }
            }
        }
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
        override fun getItemCount() = Int.MAX_VALUE

        override fun createFragment(position: Int): Fragment {
            while (position>picList.lastIndex-3)picList.add(LiveFragment(photoUri))
            return picList[position]
        }
    }
}