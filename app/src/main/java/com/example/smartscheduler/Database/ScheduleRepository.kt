package com.example.smartscheduler.Database

import androidx.annotation.WorkerThread


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
    suspend fun delete(scheduleInfo: ScheduleInfo) {
        scheduleInfoDao.delete(scheduleInfo)
    }
    suspend fun update(scheduleInfo: ScheduleInfo) {
        scheduleInfoDao.update(scheduleInfo)
    }
    fun getAllMonth(year:Int, month:Int): List<ScheduleInfo>{
        return scheduleInfoDao.getAllMonth(year, month)
    }
    fun getAllDate(year:Int, month:Int, date:Int): List<ScheduleInfo>{
        return scheduleInfoDao.getAllDate(year, month, date)
    }
    fun getAlarm(year:Int, month: Int, date:Int): List<ScheduleInfo> {
        return scheduleInfoDao.getAlarm(year, month, date)
    }
    fun getSleepAlarm(year: Int, month: Int, date: Int): List<ScheduleInfo> {
        return scheduleInfoDao.getSleepAlarm(year, month, date)
    }
    fun getTomorrowAlarm(year: Int, month: Int, date: Int):List<ScheduleInfo>{
        return scheduleInfoDao.getTomorrowAlarm(year, month, date)
    }

    fun getTomorrowSleepAlarm(year: Int, month: Int, date: Int): List<ScheduleInfo> {
        return scheduleInfoDao.getTomorrowSleepAlarm(year, month, date)
    }
}