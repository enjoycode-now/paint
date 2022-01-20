package com.wacom.will3.ink.raster.rendering.demo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bugsnag.android.Bugsnag
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.wacom.will3.ink.raster.rendering.demo.adapter.SupportWorksAdapter
import com.wacom.will3.ink.raster.rendering.demo.databinding.ActivityUserBinding
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.View
import cn.authing.core.graphql.GraphQLException
import cn.authing.core.types.User
import com.bumptech.glide.Glide
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.authenticationClient
import com.wacom.will3.ink.raster.rendering.demo.utils.AuthingUtils.user
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils
import com.wacom.will3.ink.raster.rendering.demo.utils.ToastUtils.toast
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

class UserActivity : AppCompatActivity() {


    val supportWorksList = mutableListOf<Bitmap>()
    private lateinit var  binding : ActivityUserBinding
    val adapter = SupportWorksAdapter(this)
    val RESQUEST_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        setContentView(R.layout.activity_user)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        simulateData()
        binding.supportWorksRecylerView.layoutManager = GridLayoutManager(this,3)
        binding.supportWorksRecylerView.adapter = adapter

        CoroutineScope(Dispatchers.IO).launch {
            val sharedPref = ToastUtils.app.getSharedPreferences("Authing", Context.MODE_PRIVATE) ?: return@launch
            authenticationClient.token = sharedPref.getString("token","") ?: ""
            try {
                user = authenticationClient.getCurrentUser().execute()
            }catch (e: GraphQLException){
                runOnUiThread { startActivity(Intent(ToastUtils.app, LoginActivity::class.java)) }
                return@launch
            }
            val bioString:String = (authenticationClient.getUdfValue().execute()["biography"] ?: "这个人没有填简介啊") as String
            val userAvatarUrl = user.photo
            runOnUiThread {
                authorName.text = user.nickname
                authorId.text = "ID:${user.id}"
                biography.text = bioString
                Glide.with(this@UserActivity).load(userAvatarUrl).error(R.drawable.avatar_sample).into(binding.userAvatar)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RESQUEST_CODE && resultCode == RESULT_OK){
            Glide.with(this).load(data?.data).into(binding.userAvatar)
        }
    }

    fun onChangeAvatar(view:View){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent,RESQUEST_CODE)
    }

    fun onHomePage(view:View){
        startActivity(Intent(this,HomePageActivity::class.java))
        overridePendingTransition(0,0)
        finish()
    }

    fun buyScallop(view:View){
        val intent = Intent(this, VerificationCodeActivity::class.java)
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


    fun simulateData(){
        repeat(10){
            val drawable = ContextCompat.getDrawable(this, R.drawable.ic_copy_link)
            if(drawable !=null){
                val bitmap = drawableToBitmap(drawable)
                if (bitmap!=null)
                supportWorksList.add(bitmap)
            }
        }
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
}