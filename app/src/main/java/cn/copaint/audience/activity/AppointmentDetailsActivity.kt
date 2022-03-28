package cn.copaint.audience.activity

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.GetProposalsDetailByIdQuery
import cn.copaint.audience.R
import cn.copaint.audience.adapter.VericalLinearPhotoAdapter
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.*
import cn.copaint.audience.type.ProposalWhereInput
import cn.copaint.audience.utils.*
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import cn.copaint.audience.viewmodel.AppointmentDetailsViewModel
import com.apollographql.apollo3.api.Optional
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppointmentDetailsActivity : BaseActivity() {
    lateinit var binding: ActivityAppointmentDetailsBinding

    val appointmentDetailsViewModel: AppointmentDetailsViewModel by lazy {
        ViewModelProvider(this)[AppointmentDetailsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        app = this
        initView()
    }

    override fun initView() {
        StatusBarUtils.initSystemBar(window, "#FAFBFF", true)
        appointmentDetailsViewModel.proposalId.value = intent.getStringExtra("proposalId") ?: ""
        appointmentDetailsViewModel.creatorNickName.value =
            intent.getStringExtra("creatorNickName") ?: ""
        appointmentDetailsViewModel.creatorAvatarUri.value =
            intent.getStringExtra("creatorAvatarUri") ?: ""
        appointmentDetailsViewModel.askData()

        binding.picRecyclerview.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )
        binding.picRecyclerview.adapter = VericalLinearPhotoAdapter(this)
        binding.swipeRefreshLayout.setProgressViewOffset(true, -100, 100)
        binding.swipeRefreshLayout.setDistanceToTriggerSync(100)
        binding.swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#B5A0FD"))
        binding.swipeRefreshLayout.setOnRefreshListener {
            appointmentDetailsViewModel.askData()
            binding.swipeRefreshLayout.isRefreshing = false
            toast("刷新成功")
        }
        val proposalIdObserver = Observer<String> {
            bindUiInfo()
        }
        val creatorNickNameObserver = Observer<String> {
            bindName()
        }
        val creatorAvatarUriObserver = Observer<String> {
            bindAvatar()
        }
        val picUrlListObserver = Observer<ArrayList<String>> {
            bindPic()
        }
        val myProposalDetailObserver = Observer<GetProposalsDetailByIdQuery.Data> {
            bindUiInfo()
        }
        appointmentDetailsViewModel.proposalId.observe(this, proposalIdObserver)
        appointmentDetailsViewModel.creatorNickName.observe(this, creatorNickNameObserver)
        appointmentDetailsViewModel.creatorAvatarUri.observe(this, creatorAvatarUriObserver)
        appointmentDetailsViewModel.picUrlList.observe(this, picUrlListObserver)
        appointmentDetailsViewModel.myProposalDetail.observe(this, myProposalDetailObserver)
    }

    fun onBackPress(view: View) {
        finish()
    }

    fun onMoreDialog(view: View) {
        DialogUtils.popupShareDialog(this, binding.root, window)
    }

    fun onApplyBtn(view: View) {
        toast("暂无操作")
    }

    fun onAssessmentPainterList(view: View) {
        toast("暂无操作")
    }

    private fun bindName() {
        binding.publisherName.text = appointmentDetailsViewModel.creatorNickName.value
    }

    private fun bindAvatar() {
        appointmentDetailsViewModel.creatorAvatarUri.value?.let {
            GlideEngine.loadGridImage(
                this,
                it,
                binding.avatar
            )
        }
    }

    private fun bindPic() {
        binding.picRecyclerview.adapter?.notifyDataSetChanged()
    }

    fun bindUiInfo() {
        val node =
            appointmentDetailsViewModel.myProposalDetail.value?.proposals?.edges?.get(0)?.node
        if (AuthingUtils.user.id == node?.id) {
            binding.onApplyBtn2.visibility = View.VISIBLE
            binding.onApplyBtn.visibility = View.GONE
        } else {
            binding.onApplyBtn2.visibility = View.GONE
            binding.onApplyBtn.visibility = View.VISIBLE
        }
        binding.title.text = node?.title
        binding.appointmentType.text = node?.colorModel
        binding.description.text = node?.description

        binding.createAt.text = DateUtils.rcfDateStr2StandardDateStr(node?.createdAt.toString())
        binding.expiredAt.text =
            DateUtils.rcfDateStr2StandardDateStrWithoutTime(node?.expiredAt.toString())
        binding.expiredLabel.visibility =
            if (DateUtils.judgeIsExpired(node?.expiredAt.toString())) View.VISIBLE else View.GONE
        binding.workType.text = node?.proposalType.toString()
        binding.colorMode.text = node?.colorModel
        binding.workSize.text = node?.size
        binding.workFormat.text = "JPG"
        if (node?.stock != null) {
            binding.countText.text = "买入份额 ${node?.stock}%,出价 ${node?.stock * node?.balance}元贝"
        }
        binding.assessmentPainterList.text = "已有${node?.waitingList?.size} 位画师应征"
    }

}
