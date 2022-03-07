package cn.copaint.audience

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.databinding.ActivityPublishRequirementBinding
import cn.copaint.audience.databinding.ActivityPublishRequirementSecondBinding
import cn.copaint.audience.databinding.ActivityPublishedWorkSecondBinding
import cn.copaint.audience.type.*
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

class PublishRequirementSecondActivity : AppCompatActivity() {
    lateinit var bind: ActivityPublishRequirementSecondBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityPublishRequirementSecondBinding.inflate(layoutInflater)
        Bugsnag.start(this)
        setContentView(bind.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        ToastUtils.app = this
    }

    fun onMinusReleaseShareNum(view: View) {
        val currentNum = bind.shareEditText.text.toString().trimEnd('%').toInt()
        if (currentNum >= 1) {
            bind.shareEditText.text = "${currentNum - 1}"
        }
    }

    fun onAddReleaseShareNum(view: View) {
        val currentNum = bind.shareEditText.text.toString().trimEnd('%').toInt()
        if (currentNum <= 99) {
            bind.shareEditText.text = "${currentNum + 1}"
        }
    }

    fun onMinusEveryShareCost(view: View) {
        val currentNum = bind.priceEditText.text.toString().toInt()
        if (currentNum >= 1) {
            bind.priceEditText.setText("${currentNum - 1}")
        }
    }

    fun onAddEveryShareCost(view: View) {
        val currentNum = bind.priceEditText.text.toString().toInt()
        bind.priceEditText.setText("${currentNum + 1}")
    }

    fun onBackPress(view: View) {
        finish()
    }

    fun onSubmitBtn(view: View) {
        val proposalTitle = intent.getStringExtra("proposalTitle")?:""
        // Todo 后端接口对字段的最小和最大长度有要求
        val proposalDescription = intent.getStringExtra("proposalDescription")?:""
        val example = intent.getStringArrayListExtra("example")
        val blance = bind.priceEditText.text.toString().toInt()
        val stock = bind.shareEditText.text.toString().substring(0,bind.shareEditText.text.lastIndex).toInt()

        val apolloclient = ApolloClient.Builder()
            .serverUrl("http://120.78.173.15:20000/query")
            .addHttpHeader("Authorization", "Bearer " + AuthingUtils.user.token!!)
            .build()

        val job = CoroutineScope(Dispatchers.IO).async {
            val response = try {
                apolloclient.mutation(
                    CreateProposalMutation(
                        CreateProposalInput(
                            proposalType  = ProposalType.PUBLIC,
                            title = proposalTitle,
                            description = proposalDescription,
                            colorModel = "暖色调",
                            size = "1920x1080",
                            balance = blance,
                            stock = stock,
                            proposalUserID = Optional.Absent
                        ),
                        ExampleKeysInput(),
                        expiredAt = "2022-10-01" // 记住一定要是yyyy-MM-dd   不能是yyyy-M-d
                    )
                ).execute()

            } catch (e: Exception) {
                ToastUtils.toast(e.toString())
                return@async false
            }
            Log.i(packageName, response.toString())

            return@async true
        }
        CoroutineScope(Dispatchers.Default).launch {
            val str = job.await()
            if (str){
                runOnUiThread {
                    ToastUtils.toast("发布成功")
                }
            }
        }

    }
}