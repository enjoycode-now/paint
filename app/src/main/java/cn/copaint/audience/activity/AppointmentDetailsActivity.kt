package cn.copaint.audience.activity

import android.content.Context
import android.content.Intent
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
import cn.copaint.audience.utils.AuthingUtils.user
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
        binding.swipeRefreshLayout.setProgressViewOffset(false, -50, 200)
        binding.swipeRefreshLayout.setDistanceToTriggerSync(1000)
        binding.swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#B5A0FD"))
        binding.swipeRefreshLayout.setOnRefreshListener {
            appointmentDetailsViewModel.askData()
        }
        binding.avatar.setOnClickListener { checkPublisher() }
        binding.publisherName.setOnClickListener { checkPublisher() }
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
        val loadingObserver = Observer<Boolean> {
            binding.swipeRefreshLayout.isRefreshing = it
        }
        appointmentDetailsViewModel.proposalId.observe(this, proposalIdObserver)
        appointmentDetailsViewModel.creatorNickName.observe(this, creatorNickNameObserver)
        appointmentDetailsViewModel.creatorAvatarUri.observe(this, creatorAvatarUriObserver)
        appointmentDetailsViewModel.picUrlList.observe(this, picUrlListObserver)
        appointmentDetailsViewModel.myProposalDetail.observe(this, myProposalDetailObserver)
        appointmentDetailsViewModel.isLoading.observe(this,loadingObserver)
    }

    fun onBackPress(view: View) {
        finish()
    }

    fun onMoreDialog(view: View) {
        DialogUtils.popupShareDialog(this, binding.root, window)
    }

    fun onApplyBtn(view: View) {
        when(binding.onApplyBtn.text){
            "立即应征" ->{
                appointmentDetailsViewModel.applyProposal()
                appointmentDetailsViewModel.askData()
            }
            "需求变更" ->{
                toast("需求变更的具体逻辑还在施工...")
                startActivity(Intent(this,PublishRequirementActivity::class.java))
            }
            "取消应征" ->{
                toast("取消应征目前没有接口,请耐心等待...")
            }
            else ->{}
        }
    }

    fun onAssessmentPainterList(view: View) {
        val waitingUserList : ArrayList<String> = arrayListOf()
        appointmentDetailsViewModel.myProposalDetail?.value?.proposals?.edges?.get(0)?.node?.waitingList?.forEach { waitingUserList.add(it.userID) }
        DialogUtils.checkWaitingUserListDialog(waitingUserList,this,binding.root,window)
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

    private fun checkPublisher(){
        appointmentDetailsViewModel.myProposalDetail.value?.proposals?.edges?.get(0)?.node?.creator?.let {
            startActivity(Intent(this,UserPageCreatorActivity::class.java).putExtra("creatorId",it))
        }
    }

    private fun bindUiInfo() {
        val node =
            appointmentDetailsViewModel.myProposalDetail.value?.proposals?.edges?.get(0)?.node
        // 判断身份
        if (user.id == node?.creator) {
            binding.onApplyBtn.text = "需求变更"
        } else {
            binding.onApplyBtn.text = "立即应征"
            node?.waitingList?.forEach { item -> if(item.userID == user.id) binding.onApplyBtn.text = "取消应征"}
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
