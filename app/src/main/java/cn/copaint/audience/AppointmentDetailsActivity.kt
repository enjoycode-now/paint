package cn.copaint.audience

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.copaint.audience.adapter.VericalLinearPhotoAdapter
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.*
import cn.copaint.audience.listener.swipeRefreshListener
import cn.copaint.audience.type.ProposalWhereInput
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.DateUtils
import cn.copaint.audience.utils.GlideEngine
import cn.copaint.audience.utils.StatusBarUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.mpp.currentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppointmentDetailsActivity : AppCompatActivity() {
    lateinit var binding:ActivityAppointmentDetailsBinding
    var proposalId:String = ""
    var creatorNickName :String?=""
    var creatorAvatarUri:String?=""
    var myProposalDetail : GetProposalsDetailByIdQuery.Data? = null
    var picUrlList = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this

        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)


        binding.picRecyclerview.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )
        binding.picRecyclerview.adapter = VericalLinearPhotoAdapter(this)
        binding.swipeRefreshLayout.setProgressViewOffset(true, -100, 100)
        binding.swipeRefreshLayout.setDistanceToTriggerSync(100)
        binding.swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#B5A0FD"))
        binding.swipeRefreshLayout.setOnRefreshListener {
            onResume()
            binding.swipeRefreshLayout.isRefreshing = false
            toast("refresh done")
        }
        proposalId = intent.getStringExtra("proposalId")?:""
        creatorNickName = intent.getStringExtra("creatorNickName")?:""
        creatorAvatarUri =intent.getStringExtra("creatorAvatarUri")?:""
    }

    override fun onResume() {
        super.onResume()
        if (proposalId.isBlank()){
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val response = apolloClient(this@AppointmentDetailsActivity).query(
                GetProposalsDetailByIdQuery(
                    where = Optional.presentIfNotNull(ProposalWhereInput(id = Optional.presentIfNotNull(proposalId)))
                )
            ).execute()

            response.data?.let {
                myProposalDetail = it
                picUrlList.clear()
                myProposalDetail?.proposals?.edges?.get(0)?.node?.examples?.forEach { it2->picUrlList.add(resources.getString(R.string.PicUrlPrefix).plus(it2.key)) }
            }
            bindUiInfo()
        }
    }
    fun onBackPress(view: View) {
        finish()
    }
    fun onMoreDialog(view: View) {
        popupShareDialog(window)
    }
    fun onApplyBtn(view: View) {
        toast("暂无操作")
    }
    fun onAssessmentPainterList(view: View) {
        toast("暂无操作")
    }

    fun bindUiInfo(){
        runOnUiThread{
            val node = myProposalDetail?.proposals?.edges?.get(0)?.node
            if (AuthingUtils.user.id == node?.id){
                binding.onApplyBtn2.visibility = View.VISIBLE
                binding.onApplyBtn.visibility = View.GONE
            }else{
                binding.onApplyBtn2.visibility = View.GONE
                binding.onApplyBtn.visibility = View.VISIBLE
            }
            binding.title.text = node?.title
            binding.appointmentType.text = node?.colorModel
            binding.description.text = node?.description

            binding.createAt.text = DateUtils.rcfDateStr2StandardDateStr(node?.createdAt.toString())
            binding.expiredAt.text = DateUtils.rcfDateStr2StandardDateStrWithoutTime(node?.expiredAt.toString())
            binding.workType.text = node?.proposalType.toString()
            binding.colorMode.text = node?.colorModel
            binding.workSize.text = node?.size
            binding.workFormat.text = "JPG"
            binding.picRecyclerview.adapter?.notifyDataSetChanged()
            binding.publisherName.text = creatorNickName
            creatorAvatarUri?.let { GlideEngine.loadGridImage(this, it,binding.avatar) }
            if (node?.stock != null ){
                binding.countText.text = "买入份额 ${node?.stock}%,出价 ${node?.stock * node?.balance}元贝"
            }
            binding.assessmentPainterList.text = "已有${node?.waitingList?.size} 位画师应征"
        }
    }

    private fun popupShareDialog(window: Window) {
        val popBind = DialogSharepageMoreBinding.inflate(LayoutInflater.from(this))
        // 弹出PopUpWindow
        val layerDetailWindow = PopupWindow(popBind.root, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true)
        layerDetailWindow.isOutsideTouchable = true

        // 设置弹窗时背景变暗
        var layoutParams = window.attributes
        layoutParams.alpha = 0.4f // 设置透明度
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams

        // 弹窗消失时背景恢复
        layerDetailWindow.setOnDismissListener {
            layoutParams = window.attributes
            layoutParams.alpha = 1f
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.attributes = layoutParams
        }

        layerDetailWindow.showAtLocation(binding.root, Gravity.BOTTOM, 0, 0)
    }
}
