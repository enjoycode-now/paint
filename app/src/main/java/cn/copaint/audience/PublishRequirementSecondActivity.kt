package cn.copaint.audience

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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
    val MAX_SHARE = 100
    val MIN_SHARE = 1
    val MAX_NUM = Int.MAX_VALUE
    val MIN_NUM = 100

    var currentNum: Int = 100 // 每1%份额的元贝价格 [0-INF],默认100
    var currentShare: Int = 10 // 份额 [1-100]，默认10


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityPublishRequirementSecondBinding.inflate(layoutInflater)
        Bugsnag.start(this)
        setContentView(bind.root)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        app = this

        bind.shareEditText.setOnFocusChangeListener { _, hasFocus ->
            when (hasFocus) {
                false -> {
                    // 失去焦点
                    shareEditTextLostFocus()
                }
                else->{}
            }
        }

        bind.priceEditText.setOnFocusChangeListener { _, hasFocus ->
            when (hasFocus) {
                false -> {
                    // 失去焦点
                    priceEditTextLostFocus()
                }
                else ->{}
            }
        }
    }


    override fun onResume() {
        super.onResume()
    }

    fun onMinusReleaseShareNum(view: View) {
        if (currentShare >= MIN_SHARE + 1) {
            currentShare -= 1
            bind.shareEditText.setText("${currentShare}%")
            bind.totalBalance.text = "${currentNum * currentShare} 元贝"
        }else{
            toast("不能继续减少")
        }
    }

    fun onAddReleaseShareNum(view: View) {
        if (currentShare <= MAX_SHARE - 1) {
            currentShare += 1
            bind.shareEditText.setText("${currentShare}%")
            bind.totalBalance.text = "${currentNum * currentShare} 元贝"
        }else{
            toast("不能继续增加")
        }
    }

    fun onMinusEveryShareCost(view: View) {
        if (currentNum >= MIN_NUM + 1) {
            currentNum -= 1
            bind.priceEditText.setText("$currentNum")
            bind.totalBalance.text = "${currentNum * currentShare} 元贝"
        }else{
            toast("不能继续减少")
        }
    }

    fun onAddEveryShareCost(view: View) {
        if (currentNum <= MAX_NUM -1){
            currentNum += 1
            bind.priceEditText.setText("$currentNum")
            bind.totalBalance.text = "${currentNum * currentShare} 元贝"
        }else{
            toast("不能继续增加")
        }
    }

    fun onBackPress(view: View) {
        finish()
    }

    fun onSubmitBtn(view: View) {
        val proposalTitle = intent.getStringExtra("proposalTitle") ?: ""
        val proposalDescription = intent.getStringExtra("proposalDescription") ?: ""
        val example = intent.getStringArrayListExtra("example")
        val balance = bind.priceEditText.text.toString().toInt()
        val stock =
            bind.shareEditText.text.toString().substring(0, bind.shareEditText.text.lastIndex)
                .toInt()

        val apolloClient = ApolloClient.Builder()
            .serverUrl("http://120.78.173.15:20000/query")
            .addHttpHeader("Authorization", "Bearer " + AuthingUtils.user.token!!)
            .build()

        val job = CoroutineScope(Dispatchers.IO).async {
            val response = try {
                apolloClient.mutation(
                    CreateProposalMutation(
                        CreateProposalInput(
                            proposalType = ProposalType.PUBLIC,
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
            if (str) {
                toast("发布成功")
                delay(500)
                runOnUiThread {
                    // PublishRequirementActivity的启动模式是singleTop,如果处于栈顶，会调用onNewIntent，在那里finish该任务栈
                    val intent = Intent(
                        this@PublishRequirementSecondActivity,
                        PublishRequirementActivity::class.java
                    )
                    intent.putExtra("isFinish", true)
                    finish()
                    startActivity(intent)
                }
            }
        }

    }
    fun shareEditTextLostFocus(){
        if (bind.shareEditText.text.isNullOrEmpty()) {
            bind.shareEditText.setText("${MIN_SHARE}%")
            currentShare = MIN_SHARE
        }
        try {
            currentShare = if (bind.shareEditText.text?.endsWith('%') == true) {
                bind.shareEditText.text.toString().trimEnd('%').toInt()
            } else {
                bind.shareEditText.text.toString().toInt()
            }
        } catch (e: Exception) {
            toast("输入只能为数字")
            currentShare = MIN_SHARE
        }


        if (currentShare > MAX_SHARE) {
            bind.shareEditText.setText("${MAX_SHARE}%")
            currentShare = MAX_SHARE
        } else if (currentShare < MIN_SHARE) {
            bind.shareEditText.setText("${MIN_SHARE}%")
            currentShare = MIN_SHARE
        }

        // 保证格式为xx%
        if (!bind.shareEditText.text.endsWith('%')){
            bind.shareEditText.setText("${bind.shareEditText.text}%")
        }
        bind.totalBalance.text = "${currentNum * currentShare} 元贝"
    }
    fun priceEditTextLostFocus(){
        if (bind.priceEditText.text.isNullOrEmpty()) {
            bind.priceEditText.setText("$MIN_NUM")
            currentNum = MIN_NUM
        }
        currentNum = try {
            bind.priceEditText.text.toString().toInt()
        } catch (e: Exception) {
            toast("输入只能为数字")
            100
        }


        if (currentNum > MAX_NUM) {
            bind.priceEditText.setText("$MAX_NUM")
            currentNum = MAX_NUM
        } else if (currentNum < MIN_NUM) {
            bind.priceEditText.setText("$MIN_NUM")
            currentNum = MIN_NUM
        }
        bind.totalBalance.text = "${currentNum * currentShare} 元贝"
    }

    // 点击屏幕其他地方，使 EditText 失去焦点
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                // 获取当前焦点所在的控件；
                val view = currentFocus
                if (view != null && view is EditText) {
                    val r = Rect();
                    view.getGlobalVisibleRect(r);
                    val rawX : Int = ev.rawX.toInt()
                    val rawY : Int = ev.rawY.toInt()

                    // 判断点击的点是否落在当前焦点所在的 view 上；
                    if (!r.contains(rawX, rawY)) {
                        view.clearFocus()
                        val imm: InputMethodManager =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        // 隐藏软键盘
                        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}