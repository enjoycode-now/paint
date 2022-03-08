package cn.copaint.audience.listener

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.copaint.audience.myinterface.RecyclerListener

object swipeRefreshListener {

    //在布局中，RecyclerView外嵌套一个swipeRefreshLayout，即可实现下拉刷新上拉加载更多
    fun RecyclerView.setListener(l: RecyclerListener){
        setOnScrollListener(object : RecyclerView.OnScrollListener() {
            var lastVisibleItem: Int = 0
            val swipeRefreshLayout = this@setListener.parent
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 === recyclerView.adapter?.itemCount) {
                    //下拉刷新的时候不可以加载更多
                    if(swipeRefreshLayout is SwipeRefreshLayout){
                        if(!swipeRefreshLayout.isRefreshing){
                            l.loadMore()
                        }
                    }else{
                        l.loadMore()
                    }
                }
            }

        })

        val swipeRefreshLayout = this.parent
        if(swipeRefreshLayout is SwipeRefreshLayout){
            swipeRefreshLayout.setOnRefreshListener {
                l.refresh()
            }
        }

    }
}