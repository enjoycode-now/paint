package cn.copaint.audience.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.copaint.audience.FollowUserMutation
import cn.copaint.audience.UnfollowUserMutation
import cn.copaint.audience.UserPageCreatorActivityInitQuery
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.type.FollowInfoInput
import cn.copaint.audience.type.FollowerWhereInput
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils
import cn.copaint.audience.utils.ToastUtils.app
import cn.copaint.audience.utils.ToastUtils.toast
import com.apollographql.apollo3.api.Optional
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserCreatorViewModel : BaseViewModel() {

    val userPageCreatorData = MutableLiveData<UserPageCreatorActivityInitQuery.Data>()

    var is_follow: MutableLiveData<Boolean> = MutableLiveData(false)

    fun askUserPageCreatorData(creatorId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                myApolloClient.apolloClient(app).query(
                    UserPageCreatorActivityInitQuery(
                        input =
                        FollowInfoInput(userID = user.id),
                        listOf(creatorId),
                        where = Optional.presentIfNotNull(
                            FollowerWhereInput(
                                userID = Optional.presentIfNotNull(creatorId),
                                followerID = Optional.presentIfNotNull(AuthingUtils.user.id)
                            )
                        )
                    )
                ).execute().data
            } catch (e: Exception) {
                toast(e.toString())
                return@launch
            }

            userPageCreatorData.postValue(response)
            is_follow.postValue(response?.followers?.totalCount!! > 0)
        }
    }

    fun unFollow(creatorId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                myApolloClient.apolloClient(app).mutation(
                    UnfollowUserMutation(creatorId)
                ).execute().data
            } catch (e: Exception) {
                toast(e.toString())
                return@launch
            }
            if (response != null) {
                is_follow.postValue(false)
            } else {
                toast("取消关注失败")
            }

        }
    }

    fun follow(creatorId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                myApolloClient.apolloClient(app).mutation(
                    FollowUserMutation(creatorId)
                ).execute().data
            } catch (e: Exception) {
                toast(e.toString())
                return@launch
            }
            if (response != null) {
                is_follow.postValue(true)
            } else {
                toast("关注失败")
            }
        }
    }
}