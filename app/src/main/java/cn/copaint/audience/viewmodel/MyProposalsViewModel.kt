package cn.copaint.audience.viewmodel

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import cn.copaint.audience.fragment.MyOrdersFragment
import cn.copaint.audience.fragment.MyProposalsFragment

class MyProposalsViewModel : BaseViewModel() {
    val fragmentList : MutableLiveData<MutableList<Fragment>> = MutableLiveData(mutableListOf<Fragment>(MyProposalsFragment(),MyOrdersFragment()))
}