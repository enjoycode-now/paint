package cn.copaint.audience.fragment

import android.graphics.Color
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
import cn.copaint.audience.apollo.myApolloClient
import cn.copaint.audience.apollo.myApolloClient.apolloClient
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

class SearchUsersFragment(val activity: SearchResultActivity) : Fragment() {

    lateinit var binding: FragmentItemSearchUsersBinding
    val userList: ArrayList<searchUserInfo> = arrayListOf()
    var page: Int = 1
    val limit = 20
    var hasNextPage = false
    lateinit var adapter: FragmentSearchUserAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentItemSearchUsersBinding.inflate(inflater, container, false)
        binding.usersRecyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
        binding.swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#B5A0FD"))
        adapter = FragmentSearchUserAdapter(this)
        binding.usersRecyclerView.adapter = adapter
        binding.usersRecyclerView.setListener(activity, object : RecyclerListener {
            override fun loadMore() {
                if (hasNextPage) {
                    ToastUtils.toast("加载更多...")
                    Snackbar.make(binding.root,"加载更多...",Snackbar.LENGTH_SHORT).show()
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

        val searchText = activity.binding.searchEdit.text.toString()
        if (searchText == ""){
            binding.animationView.visibility = View.GONE
            return
        }
        binding.animationView.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apolloClient(activity).query(
                    AuthingSearchUsersQuery(
                        searchText,
                        page,
                        limit
                    )
                ).execute()
                hasNextPage = response.data?.authingSearchUsers?.size == limit
                val tempUserList:ArrayList<searchUserInfo> = arrayListOf()
                response.data?.authingSearchUsers?.forEach {
                    val followResponse = apolloClient(activity).query(
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
                        tempUserList.add(searchUserInfo(it.id, it.photo, it.nickname, true))
                    } else {
                        tempUserList.add(searchUserInfo(it.id, it.photo, it.nickname, false))
                    }
                }

                activity.runOnUiThread{
                    userList.addAll(tempUserList)
                    binding.animationView.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                }
            } catch (e: ApolloException) {
                Log.e("SearchUsersFragment", "Failure", e)
                return@launch
            } catch (e: Exception) {
                Log.e("SearchUsersFragment", "Failure", e)
                return@launch
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