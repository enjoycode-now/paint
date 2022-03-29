package cn.copaint.audience.activity

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import cn.copaint.audience.R
import cn.copaint.audience.databinding.ActivitySearchResultBinding
import cn.copaint.audience.fragment.SearchAppointmentFragment
import cn.copaint.audience.fragment.SearchFilterFragment
import cn.copaint.audience.fragment.SearchUsersFragment
import cn.copaint.audience.fragment.SearchWorksFragment
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils
import cn.copaint.audience.utils.ToastUtils.app
import com.bugsnag.android.Bugsnag

class SearchResultActivity : BaseActivity() {
    lateinit var binding: ActivitySearchResultBinding
    val fragmentList = mutableListOf<Fragment>(SearchWorksFragment(this),SearchAppointmentFragment(this),SearchUsersFragment(this))
    var currentFragment = 0
    private val searchHistoryList = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        setContentView(binding.root)
        app = this
        initView()

    }

    override fun initView() {
        //防止弹出软键盘时将屏幕顶上去
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        binding.searchEdit.setText(intent.getStringExtra("SearchContent"))
        //默认页为作品页
        replaceFragment(fragmentList[currentFragment])
        getSearchHistory()

        binding.searchEdit.setOnEditorActionListener { textview, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if(textview.text.toString() == ""){
                    ToastUtils.toast("搜索内容不得为空")
                    return@setOnEditorActionListener false
                }
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
                searchHistoryList.remove(textview.text.toString())
                searchHistoryList.add(0,textview.text.toString())
                fragmentList[currentFragment].onResume()
            }
            false;
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, fragment).commit()
    }

    fun onBackPress(view: View) {
        finish()
    }

    fun onFilterBtn(view: View) {
        if(view.isSelected){
            (view as ImageView).setImageResource(R.drawable.ic_filter_unselected)
            replaceFragment(fragmentList[currentFragment])
            view.isSelected = false
        }else{
            (view as ImageView).setImageResource(R.drawable.ic_filter_selected)
            replaceFragment(SearchFilterFragment())
            view.isSelected = true
        }
    }
    fun onUserBtn(view: View) {
        currentFragment = 2
        cancelHighLight()
        (view as TextView).setTextColor(Color.WHITE)
        view.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_purple_btn,null)
        replaceFragment(fragmentList[currentFragment])
    }
    fun onWorkBtn(view: View) {
        currentFragment = 0
        cancelHighLight()
        (view as TextView).setTextColor(Color.WHITE)
        view.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_purple_btn,null)
        replaceFragment(fragmentList[currentFragment])
    }
    fun onAppointmentsBtn(view: View) {
        currentFragment = 1
        cancelHighLight()
        (view as TextView).setTextColor(Color.WHITE)
        view.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_purple_btn,null)
        replaceFragment(fragmentList[currentFragment])
    }

    fun cancelHighLight(){
        binding.work.setTextColor(Color.parseColor("#939393"))
        binding.appointments.setTextColor(Color.parseColor("#939393"))
        binding.user.setTextColor(Color.parseColor("#939393"))
        binding.work.background = null
        binding.appointments.background = null
        binding.user.background = null

        if(binding.filter.isSelected){
            binding.filter.setImageResource(R.drawable.ic_filter_unselected)
            binding.filter.isSelected = false
        }
    }

    private fun getSearchHistory() {
        searchHistoryList.clear()
        val saveData = getSharedPreferences("search", MODE_PRIVATE).getStringSet("searchHistory",null)
        saveData?.forEach { s -> searchHistoryList.add(s) }
    }

    private fun saveSearchHistory(){
        var editor = getSharedPreferences("search",MODE_PRIVATE).edit()
        editor.putStringSet("searchHistory", searchHistoryList.toSet())
        editor.apply()
    }

    override fun onStop() {
        saveSearchHistory()
        super.onStop()
    }
}