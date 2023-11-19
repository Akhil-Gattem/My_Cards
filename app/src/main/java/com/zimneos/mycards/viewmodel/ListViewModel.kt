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

    fun refresh(){
        _cardData.value = holdingArrayList
    }

    fun addData(data: Holding) {
        holdingArrayList.add(data)
    }

    fun clearData() {
        holdingArrayList.clear()
    }

    fun deleteData(
        cardNumber: String?,
        cardHolderName: String?,
        month: String?,
        year: String?,
        cvv: String?,
        cardType: String?,
        cardNote: String?
    ) {
        holdingArrayList.remove(
            Holding(
                cardNumber,
                cardHolderName,
                month,
                year,
                cvv,
                cardType,
                cardNote
            )
        )
    }
}