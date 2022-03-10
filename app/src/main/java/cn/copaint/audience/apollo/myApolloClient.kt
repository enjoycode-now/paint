package cn.copaint.audience.apollo

import android.content.Context
import android.util.Log
import cn.authing.core.types.User
import cn.copaint.audience.AuthingSearchUsersQuery
import cn.copaint.audience.GetFollowersListQuery
import cn.copaint.audience.R
import cn.copaint.audience.selections.GetAuthingUsersInfoQuerySelections
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.AuthingUtils.user
import cn.copaint.audience.utils.ToastUtils
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.apollographql.apollo3.network.http.ApolloClientAwarenessInterceptor
import com.apollographql.apollo3.network.okHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import java.lang.Exception

object myApolloClient {
    private var instance: ApolloClient? = null

    fun apolloClient(context: Context): ApolloClient {
        if (instance != null) {
            return instance!!
        }
        instance = ApolloClient.Builder()
            .serverUrl("http://120.78.173.15:20000/query")
            .addHttpInterceptor(ApolloClientAwarenessInterceptor(context.getString(R.string.client_name), context.getString(R.string.version_code)))
            .okHttpClient(myOkHttpClient.myOkHttpClient(context))
            .build()

        return instance!!
    }
}



