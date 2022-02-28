package cn.copaint.audience.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import cn.copaint.audience.GetAuthingUsersInfoQuery
import cn.copaint.audience.adapter.FollowAdapter
import cn.copaint.audience.adapter.FragmentSearchUserAdapter
import cn.copaint.audience.databinding.FragmentItemSearchUsersBinding

class SearchUsersFragment : Fragment() {

    lateinit var binding: FragmentItemSearchUsersBinding
    val userList: ArrayList<GetAuthingUsersInfoQuery.AuthingUsersInfo> = arrayListOf()

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
        initData()
        return binding.root
    }


    private fun initData() {
        userList.clear()
        // 模拟数据
        userList.addAll(
            listOf(
                GetAuthingUsersInfoQuery.AuthingUsersInfo(
                    "6209bb697b69c7baab10cd25",
                    "用户1",
                    "男",
                    "",
                    "随便"
                ),
                GetAuthingUsersInfoQuery.AuthingUsersInfo(
                    "61e5378adc9e845d84b1ce95",
                    "用户2",
                    "男",
                    "",
                    "随便"
                )
            )
        )
    }

}