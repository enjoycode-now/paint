package com.wacom.will3.ink.raster.rendering.demo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import cn.authing.core.graphql.GraphQLException
import com.bugsnag.android.Bugsnag
import com.bumptech.glide.Glide
import com.wacom.will3.ink.raster.rendering.demo.adapter.SupportWorksAdapter
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityUserBinding
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.authenticationClient
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.biography
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.user
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.app
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserActivity : AppCompatActivity() {

    val sponsorList = mutableListOf<String>()
    private lateinit var  binding : ActivityUserBinding
    val adapter = SupportWorksAdapter(this)
    val RESQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        setContentView(R.layout.activity_user)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        HignLightBtn(binding.myPageBtn)
        binding.supportWorksRecylerView.layoutManager = GridLayoutManager(this,3)
        binding.supportWorksRecylerView.adapter = adapter

        CoroutineScope(Dispatchers.Default).launch{

        }
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            updateInfo()
            val sharedPref = app.getSharedPreferences("Authing", Context.MODE_PRIVATE) ?: return@launch
            authenticationClient.token = sharedPref.getString("token","") ?: ""
            try {
                user = authenticationClient.getCurrentUser().execute()
            }catch (e: GraphQLException){
                runOnUiThread { startActivity(Intent(app, LoginActivity::class.java)) }
                return@launch
            }
            biography = (authenticationClient.getUdfValue().execute()["biography"] ?: "这个人没有填简介啊") as String
            updateInfo()

            // 应援记录数据
            repeat(16){sponsorList.add("https://api.ghser.com/random/pe.php")}
            runOnUiThread { adapter.notifyDataSetChanged() }
        }
    }

    fun updateInfo(){
        runOnUiThread {
            binding.authorName.text = user.nickname
            binding.authorId.text = "ID:${user.id}"
            binding.biography.text = biography
            Glide.with(this@UserActivity).load(user.photo).error(R.drawable.avatar_sample).into(binding.userAvatar)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RESQUEST_CODE && resultCode == RESULT_OK){
            Glide.with(this).load(data?.data).into(binding.userAvatar)
        }
    }

    fun HignLightBtn(view: View){
        val textview = view as TextView
        binding.homePageBtn.isSelected = false
        binding.myPageBtn.isSelected = false
        binding.homePageBtn.setTextColor(Color.rgb(179,179,179))
        binding.myPageBtn.setTextColor(Color.rgb(179,179,179))
        textview.isSelected = true
        textview.setTextColor(Color.rgb(255,255,255))
    }

    fun onChangeAvatar(view:View){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent,RESQUEST_CODE)
    }

    fun onSetting(view: View){
        startActivity(Intent(this,SettingActivity::class.java))
        finish()
    }

    fun onHomePage(view:View){
        HignLightBtn(binding.homePageBtn)
        startActivity(Intent(this,HomePageActivity::class.java))
        overridePendingTransition(0,0)
        finish()
    }

    fun buyScallop(view:View){
        val intent = Intent(this, PayActivity::class.java)
        startActivity(intent)
    }

    fun copyAddress(view: View) {
        val address : String  = binding.blockchainAddress.text.replaceRange(0, 6, "").toString()
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("blockchainAddress", address)
        clipboardManager.setPrimaryClip(clipData)
        toast("地址复制成功")
    }

    fun copyId(view: View) {
        val id : String  = binding.authorId.text.replaceRange(0, 3, "").toString()
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("authorId", id)
        clipboardManager.setPrimaryClip(clipData)
        toast("ID复制成功")
    }

    //将drawable转为bitmap
    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        //取drawable的宽高
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        //取drawable的颜色格式
        val config =
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        //创建对应的bitmap
        val bitmap = Bitmap.createBitmap(width, height, config)
        //创建对应的bitmap的画布
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        //把drawable内容画到画布中
        drawable.draw(canvas)
        return bitmap
    }

    fun onUserPage(view: android.view.View) {
        HignLightBtn(binding.myPageBtn)
    }
}