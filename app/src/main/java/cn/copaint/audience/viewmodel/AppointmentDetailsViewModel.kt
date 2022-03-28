package cn.copaint.audience.viewmodel

import androidx.lifecycle.MutableLiveData
import cn.copaint.audience.ApplyProposalMutation
import cn.copaint.audience.GetProposalsDetailByIdQuery
import cn.copaint.audience.R
import cn.copaint.audience.UpdateProposalMutation
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.type.AttachmentKeysInput
import cn.copaint.audience.type.ProposalWhereInput
import cn.copaint.audience.type.UpdateProposalInput
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.apollographql.apollo3.api.Optional
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class AppointmentDetailsViewModel : BaseViewModel() {
    var proposalId: MutableLiveData<String> = MutableLiveData("")
    var creatorNickName: MutableLiveData<String> = MutableLiveData("")
    var creatorAvatarUri: MutableLiveData<String> = MutableLiveData("")
    var myProposalDetail: MutableLiveData<GetProposalsDetailByIdQuery.Data> = MutableLiveData(null)
    var picUrlList: MutableLiveData<ArrayList<String>> = MutableLiveData(arrayListOf())
    val isLoading = MutableLiveData(false)

    fun askData() {
        if (proposalId.value?.isBlank() == true) {
            return
        }
        isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apolloClient(app).query(
                    GetProposalsDetailByIdQuery(
                        where = Optional.presentIfNotNull(
                            ProposalWhereInput(
                                id = Optional.presentIfNotNull(
                                    proposalId.value ?: ""
                                )
                            )
                        )
                    )
                ).execute()
                response.data?.let {
                    myProposalDetail.postValue(it)
                    val tempPicList = arrayListOf<String>()
                    it.proposals?.edges?.get(0)?.node?.examples?.forEach { it2 ->
                        tempPicList.add(
                            app.resources.getString(
                                R.string.PicUrlPrefix
                            ).plus(it2.key)
                        )
                    }
                    picUrlList.postValue(tempPicList)
                }
            } catch (e: Exception) {
                toast(e.toString())
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun applyProposal() {

        isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apolloClient(app).mutation(
                    ApplyProposalMutation(proposalId = proposalId.value!!)
                ).execute()

                if (response.data != null) {
                    toast("应征成功！")
                } else {
                    toast("系统出小差了...")
                }
            } catch (e: Exception) {
                toast(e.message.toString())
            } finally {
                isLoading.postValue(false)
            }
        }

    }

    fun updateProposal() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val response = apolloClient(app).mutation(
                    UpdateProposalMutation(
                        updateProposalId = "",
                        input = UpdateProposalInput(),
                        expiredAt = Optional.presentIfNotNull(""),
                        addExampleKeys = AttachmentKeysInput(),
                    )
                ).execute()

                if (response.data != null) {
                    toast("取消应征成功！")
                } else {
                    toast("系统出小差了...")
                }
            }
        } catch (e: Exception) {
            toast(e.message.toString())
        }
    }
}