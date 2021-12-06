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

    private var _monthData = MutableLiveData<List<ScheduleInfo>>()
    val monthData : LiveData<List<ScheduleInfo>>
        get() = _monthData

    private var _alarmData = MutableLiveData<List<ScheduleInfo>>()
    val alarmData : LiveData<List<ScheduleInfo>>
        get() = _alarmData

    private var _sleepAlarmData = MutableLiveData<List<ScheduleInfo>>()
    val sleepAlarmData : LiveData<List<ScheduleInfo>>
        get() = _sleepAlarmData

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
    fun getAllMonth(year:Int, month:Int){
        viewModelScope.launch(Dispatchers.IO){
            val tmp = repository.getAllMonth(year, month)
            _monthData.postValue(tmp)
        }
    }
    fun getAllDate(year:Int, month:Int, date:Int){
        viewModelScope.launch(Dispatchers.IO){
            val tmp = repository.getAllDate(year, month, date)
            _currentData.postValue(tmp)
        }
    }
    fun getAlarm(year:Int, month:Int, date:Int){
        viewModelScope.launch(Dispatchers.IO){
            val tmp = repository.getAlarm(year, month, date)
            _alarmData.postValue(tmp)
        }
    }
    fun getSleepAlarm(year:Int, month:Int, date:Int){
        viewModelScope.launch(Dispatchers.IO){
            val tmp = repository.getSleepAlarm(year, month, date)
            _sleepAlarmData.postValue(tmp)
        }
    }
}