package cn.copaint.audience

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.copaint.audience.databinding.ActivityPublishedWorkBinding
import cn.copaint.audience.utils.ToastUtils.app
import com.bugsnag.android.Bugsnag
import java.util.regex.Matcher
import java.util.regex.Pattern

class PublishedWorkActivity : AppCompatActivity() {
    lateinit var bind: ActivityPublishedWorkBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        bind = ActivityPublishedWorkBinding.inflate(layoutInflater)
        setContentView(bind.root)
        app = this

        bind.ReleaseShareNum.addTextChangedListener(object : TextWatcher {
            var length = 0 // 记录字符串被删除字符之前，字符串的长度

            var location = 0 // 记录光标的位置

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                length = s.length
                location = bind.ReleaseShareNum.selectionStart
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val p: Pattern = Pattern.compile("^(100|[1-9]\\d|\\d)$")
                val m: Matcher = p.matcher(s.toString())
                if (m.find() || "" == s.toString()) {
                    print("OK!")
                } else {
                    bind.ReleaseShareNum.setText("100")
                    Toast.makeText(this@PublishedWorkActivity, "请输入正确的数值", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    fun onMinusReleaseShareNum(view: View) {
        val currentNum = bind.ReleaseShareNum.text.toString().toInt()
        if (currentNum > 1) {
            bind.ReleaseShareNum.setText((currentNum - 1).toInt().toString())
        }
    }

    fun onAddReleaseShareNum(view: View) {
        val currentNum = bind.ReleaseShareNum.text.toString().toInt()
        if (currentNum < 99) {
            bind.ReleaseShareNum.setText((currentNum + 1).toInt().toString())
        }
    }

    fun onMinusEveryShareCost(view: View) {
        val currentNum = bind.EveryShareCost.text.toString().toInt()
        if (currentNum > 1) {
            bind.EveryShareCost.setText((currentNum - 1).toInt().toString())
        }
    }

    fun onAddEveryShareCost(view: View) {
        val currentNum = bind.EveryShareCost.text.toString().toInt()
        bind.EveryShareCost.setText((currentNum + 1).toInt().toString())
    }
}
