package cn.copaint.audience

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import cn.copaint.audience.databinding.ActivitySearchResultBinding
import cn.copaint.audience.fragment.SearchAppointmentFragment
import cn.copaint.audience.utils.StatusBarUtils
import com.bugsnag.android.Bugsnag

class SearchResultActivity : AppCompatActivity() {
    lateinit var binding: ActivitySearchResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bugsnag.start(this)
        StatusBarUtils.initSystemBar(window,"#FAFBFF",false)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(SearchAppointmentFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, fragment)
        transaction.commit()
    }

    fun onBackPress(view: View) {
        finish()
    }

    fun onFilterBtn(view: View) {

    }
    fun onUserBtn(view: View) {
        cancelHighLight()
        (view as TextView).setTextColor(Color.WHITE)
        view.background = resources.getDrawable(R.drawable.bg_purple_btn,null)
    }
    fun onWorkBtn(view: View) {
        cancelHighLight()
        (view as TextView).setTextColor(Color.WHITE)
        view.background = resources.getDrawable(R.drawable.bg_purple_btn,null)
    }
    fun onAppointmentsBtn(view: View) {
        cancelHighLight()
        (view as TextView).setTextColor(Color.WHITE)
        view.background = resources.getDrawable(R.drawable.bg_purple_btn,null)
    }

    fun cancelHighLight(){
        binding.work.setTextColor(Color.parseColor("#939393"))
        binding.appointments.setTextColor(Color.parseColor("#939393"))
        binding.user.setTextColor(Color.parseColor("#939393"))
        binding.work.background = null
        binding.appointments.background = null
        binding.user.background = null
    }
}