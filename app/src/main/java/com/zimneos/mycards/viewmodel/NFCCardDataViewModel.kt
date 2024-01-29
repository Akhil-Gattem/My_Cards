package com.zimneos.mycards.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NFCCardDataViewModel :ViewModel() {

    private val _cardNumber = MutableLiveData<String>()
    val cardNumber: LiveData<String> get() = _cardNumber

    private val _cardExpiryMonth = MutableLiveData<Int>()
    val cardExpiryMonth: LiveData<Int> get() = _cardExpiryMonth

    private val _cardExpiryYear = MutableLiveData<Int>()
    val cardExpiryYear: LiveData<Int> get() = _cardExpiryYear



    fun addNFCCardData(cardNumber: String, month:Int, year:Int){
        _cardNumber.postValue(cardNumber)
        _cardExpiryMonth.postValue(month)
        _cardExpiryYear.postValue(year)
    }
}