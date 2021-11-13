package com.example.smartscheduler

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData


class ScheduleRepository(private val scheduleInfoDao: ScheduleInfoDao) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(scheduleInfo: ScheduleInfo) {
        scheduleInfoDao.insert(scheduleInfo)
    }
    fun getAllMonth(year:Int, month:Int): LiveData<List<ScheduleInfo>>{
        return scheduleInfoDao.getAllMonth(year, month)
    }
    fun getAllDate(year:Int, month:Int, date:Int): LiveData<List<ScheduleInfo>>{
        return scheduleInfoDao.getAllDate(year, month, date)
    }
}