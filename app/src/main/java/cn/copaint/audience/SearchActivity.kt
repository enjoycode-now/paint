package cn.copaint.audience

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.adapter.FlowAdapter
import cn.copaint.audience.adapter.SearchHistoryAdapter
import cn.copaint.audience.databinding.ActivitySearchBinding
import cn.copaint.audience.databinding.ItemSearchRecommendBinding
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchActivity : AppCompatActivity() {
    lateinit var binding: ActivitySearchBinding
    val searchHistoryList =
        mutableListOf<String>("EVA剧场", "新世纪福音战士", "新世纪福音战士", "新世纪福音战士", "新世纪福音战士")
    val recommendList =
        mutableListOf<String>("一号机", "机甲", "绝对领域", "绝对领域剧场初雪", "最终机", "机甲格斗", "AOE", "无限世界拳击")
    val searchHistoryAdapter = SearchHistoryAdapter(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)

        //防止弹出软键盘时将屏幕顶上去
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        binding.searchHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchHistoryRecyclerView.adapter = searchHistoryAdapter
        binding.recommendRecyclerView.setAdapter(MyFlowAdapter())
        binding.searchEdit.setOnEditorActionListener { textview, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if(textview.text.toString() == ""){
                    toast("搜索内容不得为空")
                    return@setOnEditorActionListener false
                }
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
                searchHistoryList.add(0,textview.text.toString())
                startActivity(Intent(this, SearchResultActivity::class.java).putExtra("SearchContent",textview.text.toString()))
            }
            false;
        }
        binding.changeRecommendIm.setOnClickListener { refreshRecommend() }
        binding.changeRecommend.setOnClickListener { refreshRecommend() }
    }


    fun onBackPress(view: View) {
        finish()
    }

    override fun onResume() {
        super.onResume()
        searchHistoryAdapter.notifyDataSetChanged()
    }

    fun refreshRecommend() {
        CoroutineScope(Dispatchers.Default).launch {
            for (i in 0..36) {
                runOnUiThread {
                    binding.changeRecommendIm.rotation += 20
                    binding.changeRecommendIm.invalidate()
                }
                delay(50)
            }
        }

        // TODO 对接更新推荐内容的后端接口
    }

    inner class MyFlowAdapter :FlowAdapter(){
        override val count: Int = recommendList.size

        override fun getView(position: Int, parent: ViewGroup?): View {
            val itemBinding = ItemSearchRecommendBinding.inflate(layoutInflater)
            val s: String = recommendList[position]
            itemBinding.itemTextview.text = s
            itemBinding.root.setOnClickListener {
                ToastUtils.toast(s)
            }
            return itemBinding.root
        }
    }

}