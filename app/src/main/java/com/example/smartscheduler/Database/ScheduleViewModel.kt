package com.example.smartscheduler.Database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private var _currentData = MutableLiveData<List<ScheduleInfo>>()
    val currentData : LiveData<List<ScheduleInfo>>
        get() = _currentData
    private var repository:ScheduleRepository

    init {
        val scheduleInfoDao = ScheduleDatabase.getDatabase(application).scheduleInfoDao()
        repository = ScheduleRepository(scheduleInfoDao)
    }

    fun insert(scheduleInfo: ScheduleInfo) = viewModelScope.launch {
        repository.insert(scheduleInfo)
    }
    fun delete(scheduleInfo: ScheduleInfo) = viewModelScope.launch {
        repository.delete(scheduleInfo)
    }
    fun update(scheduleInfo: ScheduleInfo) = viewModelScope.launch {
        repository.update(scheduleInfo)
    }
    /*fun getAllMonth(): LiveData<List<ScheduleInfo>>{
        return repository.getAllMonth(year, month)
    }*/
    fun getAllDate(year:Int, month:Int, date:Int){
        viewModelScope.launch(Dispatchers.IO){
            val tmp = repository.getAllDate(year, month, date)
            _currentData.postValue(tmp)
        }
    }
}