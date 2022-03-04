package cn.copaint.audience.apollo

import android.util.Log
import cn.copaint.audience.AuthingSearchUsersQuery
import cn.copaint.audience.GetFollowersListQuery
import cn.copaint.audience.selections.GetAuthingUsersInfoQuerySelections
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.ToastUtils
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

object Query {

    val apolloBuilder = ApolloClient.Builder()
        .serverUrl("http://120.78.173.15:20000/query")


    /**
     * 获取UserInfo
     * @param query 通用查询字符串
     * @param page 页数
     * @param limit 每页结果个数
     */
//    fun SearchResultActivityUser(query: String, page: Int, limit: Int) : ApolloResponse<AuthingSearchUsersQuery.Data>? {
//        if (AuthingUtils.user.token.isNullOrEmpty()) {
//            AuthingUtils.loginCheck()
//            return null
//        }
//
//        val apolloClient =
//            apolloBuilder.addHttpHeader("Authorization", "Bearer " + AuthingUtils.user.token)
//                .build()
//        CoroutineScope(Dispatchers.IO).launch {
//            val response = try {
//                apolloClient.query(
//                    AuthingSearchUsersQuery(query, page, limit)
//                ).execute().data
//            } catch (e: ApolloException) {
//                Log.e("SearchResultActivityUser", "Failure", e)
//            } catch (e: Exception) {
//                ToastUtils.toast(e.toString())
//                Log.e("SearchResultActivityUser", "Failure", e)
//            }
//
//            response
//        }
//    }


}



