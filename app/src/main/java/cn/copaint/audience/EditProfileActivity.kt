package cn.copaint.audience

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import cn.authing.core.graphql.GraphQLCall
import cn.authing.core.types.SetUdvBatchResponse
import cn.authing.core.types.UpdateUserInput
import cn.authing.core.types.UserDefinedData
import cn.copaint.audience.databinding.ActivityEditProfileBinding
import cn.copaint.audience.utils.AuthingUtils.authenticationClient
import cn.copaint.audience.utils.AuthingUtils.biography
import cn.copaint.audience.utils.AuthingUtils.update
import cn.copaint.audience.utils.AuthingUtils.uploadAvatar
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.GlideEngine
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.bugsnag.android.Bugsnag
import com.bumptech.glide.Glide
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * 个人资料编辑页
 */
class EditProfileActivity : AppCompatActivity() {
    val RESQUEST_CODE = 1
    private lateinit var binding: ActivityEditProfileBinding
    val updateInput = UpdateUserInput()
    var photoUri: Uri? = null
    lateinit var setBiography: GraphQLCall<SetUdvBatchResponse, List<UserDefinedData>>

    override fun onCreate(savedInstanceState: Bundle?) {
        Bugsnag.start(this)
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this

        updateUiInfo()
        binding.nickName.doAfterTextChanged { text -> updateInput.nickname = text.toString() }
        binding.region.doAfterTextChanged { text -> updateInput.region = text.toString() }
        binding.province.doAfterTextChanged { text -> updateInput.province = text.toString() }
        binding.city.doAfterTextChanged { text -> updateInput.city = text.toString() }
        binding.biography.doAfterTextChanged { text ->
            setBiography =
                authenticationClient.setUdfValue(mapOf(Pair("biography", text.toString())))
        }
        binding.gender.doAfterTextChanged { text ->  updateGender(text.toString()) }
    }


    fun onBackPress(view: View) {
        val alertDialog = AlertDialog.Builder(this).setTitle("确定退出？")
            .setMessage("本次编辑没有保存，退出将抛弃编辑内容...")
            .setPositiveButton("确定", DialogInterface.OnClickListener { _, _ -> finish() })
            .setNegativeButton("取消", DialogInterface.OnClickListener { dialog, which ->dialog.dismiss()  })

        alertDialog.show()
    }

    private fun updateUiInfo() {
        Glide.with(this@EditProfileActivity).load(user.photo).into(binding.userAvatar)
        if (!user.nickname.isNullOrEmpty()) binding.nickName.setText(user.nickname)
        if (!biography.isNullOrEmpty()) binding.biography.setText(biography)
        setGender(user.gender ?: "U")
        if (!user.birthdate.isNullOrEmpty()) binding.birthDate.setText( user.birthdate!!.split("T")[0])
        if (!user.region.isNullOrEmpty()) binding.region.setText(user.region)
        if (!user.province.isNullOrEmpty()) binding.region.setText(user.province)
        if (!user.city.isNullOrEmpty()) binding.region.setText(user.city)
    }



    fun pickBirthDate(view: View) {
        val datePickerDialog = DatePickerDialog(this)
        datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
            val birthdate = "$year-${month + 1}-$dayOfMonth"
            updateInput.withBirthdate(birthdate)
            user.birthdate = birthdate
            updateUiInfo()
        }
        datePickerDialog.show()
    }

    fun setGender(str: String) {
//        updateInput.withGender(str)
        when (str) {
            "M" -> binding.gender.setText("男")
            "F" -> binding.gender.setText("女")
            else -> binding.gender.setText("男/女")
        }
    }

    fun updateGender(str: String) {
        when (str) {
            "男" -> updateInput.withGender("M")
            "女" -> updateInput.withGender("F")
            else -> updateInput.withGender("U")
        }
    }

    fun onChangeAvatar(view: View) {
        selectPic()
    }

    fun selectPic() {
        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine)
            .setSelectionMode(1)
            .setFilterMaxFileSize(5120)//最大5MB
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {
                    if (result != null && result.size > 0) {
                        GlideEngine.loadGridImage(this@EditProfileActivity, result[0]?.realPath.toString(),binding.userAvatar)
                        photoUri = result[0]?.path?.toUri()
                    }else{
                        toast("没选中图片")
                    }
                }

                override fun onCancel() {
                    toast("你已经退出")
                }
            })
    }


    fun onSubmit(view: View) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if(photoUri != null){
                    val inputStream = contentResolver.openInputStream(photoUri!!)
                    if (inputStream != null) {
                        updateInput.photo = uploadAvatar(inputStream.readBytes()) ?: updateInput.photo
                    }
                }
                authenticationClient.updateProfile(updateInput).execute()
                if (this@EditProfileActivity::setBiography.isInitialized) setBiography.execute()
                authenticationClient.update()
            }catch (e : Exception){
                toast(e.toString())
            }

            runOnUiThread { super.onBackPressed() }
        }
    }
}
