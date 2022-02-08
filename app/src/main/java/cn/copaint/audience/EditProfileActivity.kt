package cn.copaint.audience

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import cn.copaint.audience.utils.ToastUtils.app
import com.bugsnag.android.Bugsnag
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {
    val RESQUEST_CODE = 1
    private lateinit var binding: ActivityEditProfileBinding
    val updateInput = UpdateUserInput()
    lateinit var setBiography: GraphQLCall<SetUdvBatchResponse, List<UserDefinedData>>

    override fun onCreate(savedInstanceState: Bundle?) {
        Bugsnag.start(this)
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        updateUiInfo()
        binding.nickName.doAfterTextChanged { text -> updateInput.nickname = text.toString() }
        binding.addressText.doAfterTextChanged { text -> updateInput.address = text.toString() }
        binding.biography.doAfterTextChanged { text ->
            setBiography =
                authenticationClient.setUdfValue(mapOf(Pair("biography", text.toString())))
        }
    }

    fun onBackPressed(view: View) = onBackPressed()

    override fun onBackPressed() {
        CoroutineScope(Dispatchers.IO).launch {
            authenticationClient.updateProfile(updateInput).execute()
            if (this@EditProfileActivity::setBiography.isInitialized) setBiography.execute()
            authenticationClient.update()
            runOnUiThread { super.onBackPressed() }
        }
    }

    private fun updateUiInfo() {
        Glide.with(this@EditProfileActivity).load(user.photo).into(binding.userAvatar)
        if (!user.nickname.isNullOrEmpty()) binding.nickName.setText(user.nickname)
        if (!biography.isNullOrEmpty()) binding.biography.setText(biography)
        setGender(user.gender ?: "U")
        if (!user.birthdate.isNullOrEmpty()) binding.birthText.text = user.birthdate!!.split("T")[0]
        if (!user.address.isNullOrEmpty()) binding.addressText.setText(user.address)
    }

    fun clickGender(view: View) {
        view as TextView
        when (view.text) {
            "男" -> setGender("M")
            "女" -> setGender("F")
            "未知" -> setGender("U")
        }
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
        binding.maleGender.setBackgroundColor(Color.GRAY)
        binding.femaleGender.setBackgroundColor(Color.GRAY)
        binding.unknowGender.setBackgroundColor(Color.GRAY)
        updateInput.withGender(str)
        when (str) {
            "M" -> binding.maleGender.setBackgroundColor(Color.CYAN)
            "F" -> binding.femaleGender.setBackgroundColor(Color.MAGENTA)
            else -> binding.unknowGender.setBackgroundColor(Color.YELLOW)
        }
    }

    fun onChangeAvatar(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, RESQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESQUEST_CODE && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            Glide.with(this).load(uri).into(binding.userAvatar)
            val inputStream = contentResolver.openInputStream(uri) ?: return
            CoroutineScope(Dispatchers.IO).launch {
                updateInput.photo = uploadAvatar(inputStream.readBytes()) ?: updateInput.photo
            }
        }
    }
}
