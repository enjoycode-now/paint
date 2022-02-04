package cn.copaint.audience

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.databinding.ActivityEditProfileBinding
import cn.copaint.audience.utils.ToastUtils
import com.bumptech.glide.Glide

class EditProfileActivity : AppCompatActivity() {
    val RESQUEST_CODE = 1
    private lateinit var binding: ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ToastUtils.app = this
    }

    fun onChangeAvatar(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, RESQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESQUEST_CODE && resultCode == RESULT_OK) {
            Glide.with(this)
                .load(data?.data)
                .into(binding.userAvatar)
        }
    }
}
