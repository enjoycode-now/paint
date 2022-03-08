package cn.copaint.audience

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import cn.copaint.audience.databinding.ActivityPublishRequirementSecondBinding
import cn.copaint.audience.type.*
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.*
import java.lang.Exception

class PublishRequirementSecondActivity : AppCompatActivity() {
    lateinit var bind: ActivityPublishRequirementSecondBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityPublishRequirementSecondBinding.inflate(layoutInflater)
        Bugsnag.start(this)
        setContentView(bind.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this


//        bind.shareEditText.doAfterTextChanged {
//            // 检查是否有%，没有就加上
//            if (it.isNullOrEmpty()) {
//                val currentNum: Int
//                val currentShare: Int
//                if (it?.endsWith('%') == true){
//                    currentNum = bind.shareEditText.text.toString().trimEnd('%').toInt()
//                    currentShare = bind.shareEditText.text.toString().toInt()
//                }else{
//                    currentNum = bind.shareEditText.text.toString().toInt()
//                    currentShare = bind.shareEditText.text.toString().toInt()
//                }
//                bind.totalBalance.text = "${currentNum * currentShare} 元贝"
//            }
//        }
//
//
//        bind.priceEditText.doAfterTextChanged {
//            // 检查是否有%，没有就加上
//            if (it.isNullOrEmpty()) {
//                val currentNum: Int
//                val currentShare: Int
//                if (bind.shareEditText.text?.endsWith('%') == true){
//                    currentNum = bind.shareEditText.text.toString().trimEnd('%').toInt()
//                    currentShare = bind.shareEditText.text.toString().toInt()
//                }else{
//                    currentNum = bind.shareEditText.text.toString().toInt()
//                    currentShare = bind.shareEditText.text.toString().toInt()
//                }
//                bind.totalBalance.text = "${currentNum * currentShare} 元贝"
//            }
//        }
    }

    fun onMinusReleaseShareNum(view: View) {
        val currentNum = bind.shareEditText.text.toString().trimEnd('%').toInt()
        if (currentNum >= 1) {
            bind.shareEditText.setText("${currentNum - 1}%")
        }
    }

    fun onAddReleaseShareNum(view: View) {
        val currentNum = bind.shareEditText.text.toString().trimEnd('%').toInt()
        if (currentNum <= 99) {
            bind.shareEditText.setText("${currentNum + 1}%")
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
        val proposalDescription = intent.getStringExtra("proposalDescription")?:""
        val example = intent.getStringArrayListExtra("example")
        val balance = bind.priceEditText.text.toString().toInt()
        val stock = bind.shareEditText.text.toString().substring(0,bind.shareEditText.text.lastIndex).toInt()

        val apolloClient = ApolloClient.Builder()
            .serverUrl("http://120.78.173.15:20000/query")
            .addHttpHeader("Authorization", "Bearer " + AuthingUtils.user.token!!)
            .build()

        val job = CoroutineScope(Dispatchers.IO).async {
            val response = try {
                apolloClient.mutation(
                    CreateProposalMutation(
                        CreateProposalInput(
                            proposalType  = ProposalType.PUBLIC,
                            title = proposalTitle,
                            description = proposalDescription,
                            colorModel = "暖色调",
                            size = "1920x1080",
                            balance = balance,
                            stock = stock,
                            proposalUserID = Optional.Absent
                        ),
                        ExampleKeysInput(Optional.presentIfNotNull(example)),
                        expiredAt = "2022-10-01" // 记住一定要是yyyy-MM-dd   不能是yyyy-M-d
                    )
                ).execute()

            } catch (e: Exception) {
                toast(e.toString())
                return@async false
            }
            Log.i(packageName, response.toString())

            return@async true
        }
        CoroutineScope(Dispatchers.Default).launch {
            val str = job.await()
            if (str){
                toast("发布成功")
                delay(500)
                runOnUiThread {
                    val intent = Intent(this@PublishRequirementSecondActivity,HomePageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }

    }
}