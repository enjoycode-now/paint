package cn.copaint.audience.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.*
import cn.copaint.audience.adapter.FollowAdapter
import cn.copaint.audience.adapter.FragmentSearchUserAdapter
import cn.copaint.audience.databinding.FragmentItemSearchUsersBinding
import cn.copaint.audience.interfaces.RecyclerListener
import cn.copaint.audience.listener.swipeRefreshListener.setListener
import cn.copaint.audience.type.BalanceRecordOrder
import cn.copaint.audience.type.FollowerWhereInput
import cn.copaint.audience.type.OrderDirection
import cn.copaint.audience.utils.AuthingUtils
import cn.copaint.audience.utils.ToastUtils
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class SearchUsersFragment(val activity: SearchResultActivity) : Fragment() {

    lateinit var binding: FragmentItemSearchUsersBinding
    val userList: ArrayList<searchUserInfo> = arrayListOf()
    var page: Int = 1
    val limit = 5
    var hasNextPage = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentItemSearchUsersBinding.inflate(inflater, container, false)
        binding.usersRecyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
        binding.usersRecyclerView.adapter = FragmentSearchUserAdapter(this)
        binding.usersRecyclerView.setListener(activity, object : RecyclerListener {
            override fun loadMore() {
                if (hasNextPage) {
                    ToastUtils.toast("加载更多...")
                    page += 1
                    updateUiInfo()
                } else {
                    ToastUtils.toast("拉到底了，客官哎...")
                }
            }

            override fun refresh() {
                ToastUtils.toast("刷新")
                binding.swipeRefreshLayout.isRefreshing = false
                onResume()
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        userList.clear()
        page = 1
        updateUiInfo()
    }

    private fun updateUiInfo() {
        binding.animationView.visibility = View.VISIBLE
        val apolloclient = ApolloClient.Builder()
            .serverUrl("http://120.78.173.15:20000/query")
            .addHttpHeader("Authorization", "Bearer ${AuthingUtils.user.token}")
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apolloclient.query(
                    AuthingSearchUsersQuery(
                        activity.binding.searchEdit.text.toString(),
                        page,
                        limit
                    )
                ).execute()
                hasNextPage = response.data?.authingSearchUsers?.size == limit
                response.data?.authingSearchUsers?.forEach {
                    val followResponse = apolloclient.query(
                        FindIsFollowQuery(
                            Optional.presentIfNotNull(
                                FollowerWhereInput(
                                    userID = Optional.presentIfNotNull(
                                        it.id
                                    ),
                                    followerID = Optional.presentIfNotNull(
                                        AuthingUtils.user.id ?: ""
                                    )
                                )
                            )
                        )
                    ).execute()
                    if (followResponse.data?.followers?.totalCount ?: 0 > 0) {
                        userList.add(searchUserInfo(it.id, it.photo, it.nickname, true))
                    } else {
                        userList.add(searchUserInfo(it.id, it.photo, it.nickname, false))
                    }
                }


            } catch (e: ApolloException) {
                Log.e("SearchUsersFragment", "Failure", e)
                return@launch
            } catch (e: Exception) {
                Log.e("SearchUsersFragment", "Failure", e)
                return@launch
            }


            activity.runOnUiThread {
                binding.animationView.visibility = View.GONE
                binding.usersRecyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    data class searchUserInfo(
        val id: String = "",
        val avatar: String?,
        val nickName: String?,
        val isFollow: Boolean = false
    )

}