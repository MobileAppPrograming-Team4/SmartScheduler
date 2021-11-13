package com.example.smartscheduler

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.*
import android.util.Log
import kotlin.properties.Delegates

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val mCalendar: Calendar = Calendar.getInstance()
    /* 값이 변경될 때 마다 {} 영역을 실행 https://way-code.tistory.com/18 */
    var year:Int by Delegates.observable(mCalendar.get(Calendar.YEAR),{property, oldValue, newValue ->
        getAllDate()
    })
    var month:Int by Delegates.observable(mCalendar.get(Calendar.MONTH) + 1,{property, oldValue, newValue ->
        getAllDate()
    })
    var date:Int by Delegates.observable(mCalendar.get(Calendar.DATE),{property, oldValue, newValue ->
        getAllDate()
    })

    val scheduleInfoDao = ScheduleDatabase.getDatabase(application).scheduleInfoDao()
    private val repository:ScheduleRepository = ScheduleRepository(scheduleInfoDao)

    fun insert(scheduleInfo: ScheduleInfo) = viewModelScope.launch {
        repository.insert(scheduleInfo)
    }
    fun getAllMonth(): LiveData<List<ScheduleInfo>>{
        return repository.getAllMonth(year, month)
    }
    fun getAllDate(): LiveData<List<ScheduleInfo>>{
        return repository.getAllDate(year, month, date)
    }
}