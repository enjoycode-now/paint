package cn.copaint.audience.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.GetRandomTagsQuery
import cn.copaint.audience.adapter.FlowAdapter
import cn.copaint.audience.adapter.SearchHistoryAdapter
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.ActivitySearchBinding
import cn.copaint.audience.databinding.ItemSearchRecommendBinding
import cn.copaint.audience.model.RecommendTag
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception


class SearchActivity : BaseActivity() {
    lateinit var binding: ActivitySearchBinding
    lateinit var recommendTagAdapter: MyFlowAdapter
    val searchHistoryAdapter = SearchHistoryAdapter(this)
    val searchHistoryList =
        mutableListOf<String>()
    val recommendList =
        mutableListOf<RecommendTag>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        initView()

    }

    override fun initView() {
        //防止弹出软键盘时将屏幕顶上去
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        getRecommendTagsList()
        binding.searchHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchHistoryRecyclerView.adapter = searchHistoryAdapter
        binding.recommendRecyclerView.setAdapter(MyFlowAdapter())
        binding.searchEdit.setOnEditorActionListener { textview, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (textview.text.toString() == "") {
                    toast("搜索内容不得为空")
                    return@setOnEditorActionListener false
                }
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
                searchHistoryList.remove(textview.text.toString())
                searchHistoryList.add(0,textview.text.toString())

                searchHistoryAdapter.notifyItemRemoved(0)
                startActivity(
                    Intent(
                        this,
                        SearchResultActivity::class.java
                    ).putExtra("SearchContent", textview.text.toString())
                )
            }
            false;
        }
        binding.changeRecommendIm.setOnClickListener { refreshRecommend() }
        binding.changeRecommend.setOnClickListener { refreshRecommend() }
    }

    private fun getRecommendTagsList() {
        recommendList.clear()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apolloClient(this@SearchActivity).query(
                    GetRandomTagsQuery()
                ).execute()
                response.data?.randomTags?.forEach {
                    recommendList.add(
                        RecommendTag(
                            it.id,
                            it.name,
                            it.createdAt.toString(),
                            false
                        )
                    )
                }
            } catch (e: Exception) {
                toast(e.toString())
            } finally {
                runOnUiThread {
                    binding.recommendRecyclerView.setAdapter(MyFlowAdapter())
                }
            }
        }

    }

    private fun getSearchHistory() {
        searchHistoryList.clear()
        val saveData =
            getSharedPreferences("search", MODE_PRIVATE).getStringSet("searchHistory", null)
        saveData?.forEach { s ->
            searchHistoryList.add(s)
        }
    }

    private fun saveSearchHistory() {
        val editor = getSharedPreferences("search", MODE_PRIVATE).edit()
        editor.putStringSet("searchHistory", searchHistoryList.toSet())
        editor.apply()
    }

    override fun onStop() {
        saveSearchHistory()
        super.onStop()
    }


    fun onBackPress(view: View) {
        finish()
    }

    override fun onResume() {
        super.onResume()
        getSearchHistory()
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
        getRecommendTagsList()
    }

    inner class MyFlowAdapter : FlowAdapter() {
        override val count: Int = recommendList.size

        override fun getView(position: Int, parent: ViewGroup?): View {
            val itemBinding = ItemSearchRecommendBinding.inflate(layoutInflater)
            val s: String = recommendList[position].name
            itemBinding.itemTextview.text = s
            itemBinding.root.setOnClickListener {
                searchHistoryList.add(0, itemBinding.itemTextview.text.toString())
                startActivity(
                    Intent(
                        this@SearchActivity,
                        SearchResultActivity::class.java
                    ).putExtra("SearchContent", itemBinding.itemTextview.text.toString())
                )
                toast(s)
            }
            return itemBinding.root
        }
    }
}



