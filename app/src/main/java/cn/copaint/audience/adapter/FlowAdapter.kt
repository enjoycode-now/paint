package cn.copaint.audience.adapter

import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup

abstract class FlowAdapter {
    //有多少个条目
    abstract val count: Int

    //getView通过position
    abstract fun getView(position: Int, parent: ViewGroup?): View?

    //观察者模式通知更新
    fun unregisterDataSetObserver(observer: DataSetObserver?) {}
    fun registerDataSetObserver(observer: DataSetObserver?) {}
}