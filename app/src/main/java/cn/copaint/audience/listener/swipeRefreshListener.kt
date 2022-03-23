package cn.copaint.audience.listener

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.copaint.audience.interfaces.RecyclerListener
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideContext

object swipeRefreshListener {

    // 实现下拉刷新上拉加载更多
    fun RecyclerView.setListener(context: Context, l: RecyclerListener) {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var lastVisibleItem: Int = 0
            val swipeRefreshLayout = this@setListener.parent
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastVisibleItem =
                    (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {

                    if (lastVisibleItem + 1 === recyclerView.adapter?.itemCount) {
                        //下拉刷新的时候不可以加载更多
                        if (swipeRefreshLayout is SwipeRefreshLayout) {
                            if (!swipeRefreshLayout.isRefreshing) {
                                l.loadMore()
                            }
                        } else {
                            l.loadMore()
                        }
                    }
                }else{
                    if (lastVisibleItem + 10 >= recyclerView.adapter?.itemCount ?: Int.MAX_VALUE) {
                        //下拉刷新的时候不可以加载更多
                        if (swipeRefreshLayout is SwipeRefreshLayout) {
                            if (!swipeRefreshLayout.isRefreshing) {
                                l.loadMoreSilent()
                            }
                        } else {
                            l.loadMoreSilent()
                        }
                    }
                }
            }

        })

        val swipeRefreshLayout = this.parent
        if (swipeRefreshLayout is SwipeRefreshLayout) {
            swipeRefreshLayout.setOnRefreshListener {
                l.refresh()
            }
        }

    }
}