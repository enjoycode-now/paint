package cn.copaint.audience.viewmodel

import androidx.lifecycle.MutableLiveData
import cn.copaint.audience.GetProposalsDetailByIdQuery
import cn.copaint.audience.R
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.type.ProposalWhereInput
import cn.copaint.audience.utils.ToastUtils.app
import com.apollographql.apollo3.api.Optional
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppointmentDetailsViewModel : BaseViewModel() {
    var proposalId: MutableLiveData<String> = MutableLiveData("")
    var creatorNickName: MutableLiveData<String> = MutableLiveData("")
    var creatorAvatarUri: MutableLiveData<String> = MutableLiveData("")
    var myProposalDetail: MutableLiveData<GetProposalsDetailByIdQuery.Data> = MutableLiveData(null)
    var picUrlList: MutableLiveData<ArrayList<String>> = MutableLiveData(arrayListOf())

    fun askData() {
        if (proposalId.value?.isBlank() == true) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
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

        }
    }
}