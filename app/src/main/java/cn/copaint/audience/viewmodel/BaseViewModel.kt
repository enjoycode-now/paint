package cn.copaint.audience.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    fun <T> request(
        onError: (error: Throwable) -> Unit = {}, // 不需要处理Error可以不传
        execute: suspend CoroutineScope.() -> T
    ) {
        viewModelScope.launch(errorHandler { onError.invoke(it) }) {
            launch(Dispatchers.IO) {
                execute()
            }
        }
    }

    private fun errorHandler(onError: (error: Throwable) -> Unit): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            onError.invoke(throwable)
        }
    }

}