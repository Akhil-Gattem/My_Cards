package com.zimneos.mycards.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zimneos.mycards.model.Holding

open class ListViewModel : ViewModel() {

    val cardData = MutableLiveData<ArrayList<Holding>>()
    private val sizeData = MutableLiveData<Int>()

    fun refresh() {
        fetchCountries()
    }

    private fun fetchCountries() {
        cardData.value = mockData
        sizeData.value = mockData.size
    }

    companion object {
        val mockData = ArrayList<Holding>()
    }
}