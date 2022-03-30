package cn.copaint.audience.activity

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.databinding.ActivityPublishRequirementSecondBinding
import cn.copaint.audience.type.*
import cn.copaint.audience.utils.MoneyInputFilter
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
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
    var formatErrorFlag = false


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

        // 重置标志位为true的话，需要用户点击两次按钮，作为确认
        if(formatErrorFlag){
            formatErrorFlag = false
            return
        }

        try {
            val perPrice = bind.priceEditText.text.toString().toInt()
            val stock =
                bind.shareEditText.text.toString().substring(0, bind.shareEditText.text.lastIndex)
                    .toInt()

            if (perPrice < 100 ){
                toast("注意：每1%份额价格不能小于100元贝")
                return
            }
            if(stock !in 1..100){
                toast("发布份额区间[1-100]")
                return
            }
            intent.setClass(this, PayOrderActivity::class.java)
            intent.putExtra("stock",stock)
            intent.putExtra("perPrice",perPrice)
            startActivity(intent)


        }catch (e : Exception){
            toast(e.toString())
        }
    }
    fun shareEditTextLostFocus(){

        try {
            currentShare = if (bind.shareEditText.text?.endsWith('%') == true) {
                bind.shareEditText.text.toString().trimEnd('%').toInt()
            } else {
                bind.shareEditText.text.toString().toInt()
            }
        } catch (e: Exception) {
            toast("输入格式有错，请输入数字1~100")
            currentShare = MIN_SHARE
            bind.shareEditText.setText("${currentNum}")
            formatErrorFlag = true
        }


        if (currentShare > MAX_SHARE) {
            bind.shareEditText.setText("${MAX_SHARE}%")
            currentShare = MAX_SHARE
            formatErrorFlag = true
            toast("份额设置超过100%,已经重置回100%")
        } else if (currentShare < MIN_SHARE) {
            bind.shareEditText.setText("${MIN_SHARE}%")
            currentShare = MIN_SHARE
            formatErrorFlag = true
            toast("份额设置低于1%,已经重置回1%")
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
            toast("输入格式有错，须为大于或等于100的数字")
            bind.priceEditText.setText("${MIN_NUM}")
            formatErrorFlag = true
            MIN_NUM
        }


        if (currentNum > MAX_NUM) {
            bind.priceEditText.setText("$MAX_NUM")
            currentNum = MAX_NUM
            formatErrorFlag = true
            toast("设置的价格超过上限，已重置")
        } else if (currentNum < MIN_NUM) {
            bind.priceEditText.setText("$MIN_NUM")
            currentNum = MIN_NUM
            formatErrorFlag = true
            toast("设置的价格低于下限，已重置")
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