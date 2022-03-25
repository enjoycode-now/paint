package cn.copaint.audience.viewmodel

import android.view.View
import androidx.lifecycle.MutableLiveData
import cn.copaint.audience.UserPageInitQuery
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.type.FollowInfoInput
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.apollographql.apollo3.api.ApolloResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.math.abs
import kotlin.random.Random

object UserViewModel : BaseViewModel() {
    val picUrlList = MutableLiveData<ArrayList<String>>()

    var response =  MutableLiveData<ApolloResponse<UserPageInitQuery.Data>>()


    fun getUserInfo(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val networkResponse = apolloClient(app).query(
                    UserPageInitQuery(
                        input =
                        FollowInfoInput(userID = user.id)
                    )
                ).execute()
                response.postValue(networkResponse)//将任务发布到主线程以设置值
            } catch (e: Exception){
                toast(e.toString())
            }
        }
    }

    fun getUserWorksUrlList(){
        CoroutineScope(Dispatchers.Default).launch {
            val sponsorList = arrayListOf<String>()
            val count: Int = abs(Random(System.currentTimeMillis()).nextInt()) %10
            for (i in 0..count) {
                sponsorList.add("https://api.ghser.com/random/pe.php")
                delay(125)
            }
            picUrlList.postValue(sponsorList)
        }
    }
}
