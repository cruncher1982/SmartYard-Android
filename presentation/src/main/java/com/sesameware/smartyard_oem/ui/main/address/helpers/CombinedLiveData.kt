package com.sesameware.smartyard_oem.ui.main.address.helpers

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

@Suppress("UNCHECKED_CAST")
class CombinedLiveData<T>(
    vararg liveDatas: LiveData<out T>,
    private val reduce: (List<T>) -> T
) : LiveData<T>() {

    private val liveDatas: List<LiveData<out T>> = liveDatas.toList()
    private val observers: List<Observer<T>>

    init {
        val observers0 = mutableListOf<Observer<T>>()
        repeat(liveDatas.size) {
            observers0.add(getObserver())
        }
        observers = observers0
    }

    private fun getObserver() = Observer<T> { _ ->
        val values = liveDatas.mapNotNull { it.value }
        if (values.size != liveDatas.size) return@Observer

        val result = reduce(values)
        this@CombinedLiveData.value = result
    }

    override fun onActive() {
        liveDatas.forEach { liveData ->
            liveData.observeForever(getObserver())
        }
    }

    override fun onInactive() {
        liveDatas.forEach { liveData ->
            liveData.removeObserver(getObserver())
        }
    }
}