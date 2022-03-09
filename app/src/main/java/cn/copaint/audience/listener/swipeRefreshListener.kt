package cn.copaint.audience.listener

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.copaint.audience.interfaces.RecyclerListener
import com.bumptech.glide.Glide

object swipeRefreshListener {

    var sIsScrolling = false

    // 实现下拉刷新上拉加载更多
    fun RecyclerView.setListener(activity: AppCompatActivity, l: RecyclerListener){
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var lastVisibleItem: Int = 0
            val swipeRefreshLayout = this@setListener.parent
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    sIsScrolling = true;
                    Glide.with(activity).pauseRequests()
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (sIsScrolling) {
                        Glide.with(activity).resumeRequests();
                    }
                    sIsScrolling = false;

                    if (lastVisibleItem + 1 === recyclerView.adapter?.itemCount){
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