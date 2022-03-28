package cn.copaint.audience.viewmodel

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import cn.copaint.audience.GetAuthingUsersInfoQuery
import cn.copaint.audience.GetFollowersListQuery
import cn.copaint.audience.adapter.FollowAdapter
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.databinding.ActivityFollowsBinding
import cn.copaint.audience.type.FollowerWhereInput
import cn.copaint.audience.utils.ToastUtils
import cn.copaint.audience.utils.ToastUtils.app
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FollowsViewModel : BaseViewModel(){

    var cursor: Any? = null
    var first = 20
    var hasNextPage = false
    lateinit var where: FollowerWhereInput
    val followList = MutableLiveData<ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo>>()
    var currentUserId: String? = null

    fun askData(){
        CoroutineScope(Dispatchers.IO).launch {
            // 获取关注列表
            val response = try {
                myApolloClient.apolloClient(app).query(
                    GetFollowersListQuery(
                        after = Optional.presentIfNotNull(
                            cursor
                        ),
                        first = Optional.presentIfNotNull(first),
                        where = Optional.presentIfNotNull(where)
                    )
                )
                    .execute().data
            } catch (e: ApolloException) {
                Log.d("FollowsActivity", "Failure", e)
                return@launch
            }
            response?.followers?.pageInfo?.endCursor?.let { it -> cursor = it }
            response?.followers?.pageInfo?.hasNextPage.let { hasNextPage = it ?: false }
            Log.i("FollowsActivity", response.toString())

            // 获取全部关注对象的userid
            val userIdList = mutableListOf<String>()
            response?.followers?.edges?.forEach {
                it?.node?.userID?.let { it1 -> userIdList.add(it1) }
            }

            val tempList = ArrayList< GetAuthingUsersInfoQuery.AuthingUsersInfo>()
            // 根据id调用authing接口获取用户信息
            try {
                apolloClient(app).query(GetAuthingUsersInfoQuery(userIdList))
                    .execute().data?.authingUsersInfo?.forEach {
                        tempList.add(it)
                    }
            } catch (e: Exception) {
                ToastUtils.toast(e.toString())
            }

            followList.postValue(tempList)
        }
    }
}