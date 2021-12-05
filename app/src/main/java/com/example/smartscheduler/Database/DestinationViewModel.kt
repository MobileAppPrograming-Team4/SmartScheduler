package com.example.smartscheduler.Database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DestinationViewModel(application: Application) : AndroidViewModel(application) {

    private var _destinationNameData = MutableLiveData<List<DestinationInfo>>()
    val destinationNameData : LiveData<List<DestinationInfo>>
        get() = _destinationNameData

    private var _destinationAddressData = MutableLiveData<List<DestinationInfo>>()
    val destinationAddressData : LiveData<List<DestinationInfo>>
        get() = _destinationAddressData



}