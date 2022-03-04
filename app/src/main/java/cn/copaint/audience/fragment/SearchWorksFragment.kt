package cn.copaint.audience.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.*
import cn.copaint.audience.adapter.FragmentSearchWorkAdapter
import cn.copaint.audience.databinding.FragmentItemSearchWorksBinding
import cn.copaint.audience.type.*
import cn.copaint.audience.utils.AuthingUtils
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class SearchWorksFragment(val activity: SearchResultActivity) : Fragment() {
    lateinit var binding: FragmentItemSearchWorksBinding
    lateinit var adapter: FragmentSearchWorkAdapter
    lateinit var searchText: String
    val workList = arrayListOf<searchWorkInfo>()
    val first = 10
    var cursor = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentItemSearchWorksBinding.inflate(layoutInflater, container, false)
        binding.worksRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        adapter = FragmentSearchWorkAdapter(this)
        binding.worksRecyclerView.adapter = adapter
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        updateUiInfo()
    }

    fun updateUiInfo() {
        searchText = activity.binding.searchEdit.text.toString()
        val apolloclient = ApolloClient.Builder()
            .serverUrl("http://120.78.173.15:20000/query")
            .addHttpHeader("Authorization", "Bearer ${AuthingUtils.user.token}")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 获取到作品信息
                val response = apolloclient.query(
                    FindPaintingsQuery(
                        Optional.presentIfNotNull(PaintingOrder(OrderDirection.DESC)),
                        Optional.presentIfNotNull(
                            PaintingWhereInput(
                                or = Optional.presentIfNotNull(
                                    listOf(
                                        PaintingWhereInput(
                                            nameContains = Optional.presentIfNotNull(searchText)
                                        ),

                                        PaintingWhereInput(
                                            creator = Optional.presentIfNotNull(
                                                searchText
                                            )
                                        ),
                                        PaintingWhereInput(
                                            descriptionContains = Optional.presentIfNotNull(
                                                searchText
                                            )
                                        ),
                                        PaintingWhereInput(
                                            hasTagsWith = Optional.presentIfNotNull(
                                                listOf(
                                                    TagWhereInput(
                                                        name = Optional.presentIfNotNull(
                                                            searchText
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        first = Optional.presentIfNotNull(first),
                        after = Optional.presentIfNotNull(cursor)
                    )
                ).execute()
                Log.i("chenlin", response?.data.toString())
                // 批量查询关注状态
                workList.clear()
                response.data?.paintings?.edges?.forEach {
                    var tempInfo = it?.let { it ->
                        val followResponse = apolloclient.query(
                            FindIsFollowQuery(
                                Optional.presentIfNotNull(
                                    FollowerWhereInput(
                                        userID = Optional.presentIfNotNull(
                                            it.node?.creator
                                        ),
                                        followerID = Optional.presentIfNotNull(
                                            AuthingUtils.user.id ?: ""
                                        )
                                    )
                                )
                            )
                        ).execute()

                        val creatorInfoResponse = apolloclient.query(
                            GetAuthingUsersInfoQuery(
                                listOf(
                                    it.node?.creator ?: ""
                                )
                            )
                        ).execute()

                        val fansAccountResponse = apolloclient.query(
                            FindFollowersCountQuery(
                                Optional.presentIfNotNull(
                                    FollowInfoInput(
                                        userID = it.node?.creator ?: "",
                                    )
                                )
                            )
                        ).execute()


                        if (followResponse.data?.followers?.totalCount ?: 0 > 0) {
                            return@let searchWorkInfo(
                                it,
                                creatorInfoResponse.data?.authingUsersInfo?.get(0)?.photo,
                                creatorInfoResponse.data?.authingUsersInfo?.get(0)?.nickname,
                                fansAccountResponse.data?.followInfo?.followersCount?.toString(),
                                true
                            )
                        } else {
                            return@let searchWorkInfo(
                                it,
                                creatorInfoResponse.data?.authingUsersInfo?.get(0)?.photo,
                                creatorInfoResponse.data?.authingUsersInfo?.get(0)?.nickname,
                                fansAccountResponse.data?.followInfo?.followersCount?.toString(),
                                false
                            )
                        }
                    }
                    if (tempInfo != null) {
                        workList.add(tempInfo)
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
                adapter.notifyDataSetChanged()
            }
        }
    }


    data class searchWorkInfo(
        val work: FindPaintingsQuery.Edge,
        val avatar: String?,
        val userName: String?,
        val fansCount: String?,
        val isFollow: Boolean = false
    )
}