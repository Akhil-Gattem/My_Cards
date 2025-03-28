package com.zimneos.mycards.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zimneos.mycards.model.Holding

class ListViewModel : ViewModel() {

    private val _cardData = MutableLiveData<MutableList<Holding>>()
    val cardData: LiveData<MutableList<Holding>> get() = _cardData

    private val holdingArrayList: MutableList<Holding> = mutableListOf()

    init {
        _cardData.value = holdingArrayList
    }

    fun refresh() {
        _cardData.value = holdingArrayList
    }

    fun addData(data: Holding) {
        val currentList = _cardData.value ?: mutableListOf()
        currentList.add(data)
        _cardData.value = currentList
    }

    fun clearData() {
        holdingArrayList.clear()
        _cardData.value = holdingArrayList
    }

    fun deleteData(position: Int) {
        val currentList = _cardData.value ?: mutableListOf()
        currentList.removeAt(position)
        _cardData.value = currentList
    }
}