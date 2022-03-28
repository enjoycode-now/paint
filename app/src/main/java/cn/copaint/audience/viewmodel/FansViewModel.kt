package cn.copaint.audience.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import cn.copaint.audience.FindIsFollowQuery
import cn.copaint.audience.GetAuthingUsersInfoQuery
import cn.copaint.audience.GetFollowersListQuery
import cn.copaint.audience.adapter.FansAdapter
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.apollo.myApolloClient.apolloClient
import cn.copaint.audience.type.FollowerWhereInput
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.ToastUtils
import cn.copaint.audience.utils.ToastUtils.app
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class FansViewModel : BaseViewModel() {
    val fansList: MutableLiveData<ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo>> =
        MutableLiveData<ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo>>()

    // 对粉丝的关注状态 true-互相关注  false-单方面被关注
    val isFollowList = ArrayList<Boolean>()
    var cursor: Any? = null
    var first = 20
    lateinit var currentUserID: String
    var hasNextPage = false
    var where : FollowerWhereInput = FollowerWhereInput(Optional.presentIfNotNull(null))



    fun askData(){
        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                apolloClient(app).query(
                    GetFollowersListQuery(
                        after = Optional.presentIfNotNull(cursor),
                        first = Optional.presentIfNotNull(first),
                        where = Optional.presentIfNotNull(where)
                    )
                )
                    .execute().data
            } catch (e: ApolloException) {
                Log.d("FansActivity", "Failure", e)
                return@launch
            } catch (e: Exception){
                ToastUtils.toast(e.toString())
                return@launch
            }
            response?.followers?.pageInfo?.endCursor?.let { it -> cursor = it }
            response?.followers?.pageInfo?.hasNextPage.let { hasNextPage = it ?: false }
            Log.i("FollowActivity", response.toString())

            // 获取全部粉丝的userid
            val userIdList = mutableListOf<String>()
            response?.followers?.edges?.forEach {
                it?.node?.followerID?.let { followerId -> userIdList.add(followerId) }
            }

            // 根据列表获取每一个粉丝的个人信息，然后添加到List去，最后notifyChange
            val temp = ArrayList< GetAuthingUsersInfoQuery.AuthingUsersInfo>()
            apolloClient(app).query(GetAuthingUsersInfoQuery(userIdList))
                .execute().data?.authingUsersInfo?.forEach {
                    temp.add(it)
                }

            userIdList.forEach{
                val followResponse = apolloClient(app).query(
                    FindIsFollowQuery(where = Optional.presentIfNotNull(
                        FollowerWhereInput(userID = Optional.presentIfNotNull(it),followerID = Optional.presentIfNotNull(currentUserID))))
                ).execute().data
                if (followResponse?.followers?.totalCount == 1){
                    isFollowList.add(true)
                }else{
                    isFollowList.add(false)
                }
            }

            // 获得完整数据，通知livedata更改ui
            fansList.postValue(temp)
        }
    }

}