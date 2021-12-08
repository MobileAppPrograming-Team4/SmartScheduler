package com.example.smartscheduler.Database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.smartscheduler.Database.ScheduleInfo


@Dao //Room에서는 SQL을 이용한 직접적인 쿼리 접근 대신에 DAO(Data Access Object)를 이용하여 데이터베이스에 접근해야합니다.
interface ScheduleInfoDao {

    //같은 달에 있는 모든 일정들 가져오기
    @Query("SELECT * FROM schedule_database WHERE schedule_start_year=:year and schedule_start_month=:month")
    fun getAllMonth(year:Int, month:Int): List<ScheduleInfo>

    //같은 일에 있는 모든 일정들을 가져오기
    @Query("SELECT * FROM schedule_database WHERE schedule_start_year=:year and schedule_start_month=:month and schedule_start_day=:day ORDER BY schedule_start_hour, schedule_start_minute")
    fun getAllDate(year:Int, month:Int, day:Int): List<ScheduleInfo>

    //set_alarm이 true인 일정 가져오기
    @Query("SELECT * FROM schedule_database WHERE schedule_start_year=:year and schedule_start_month=:month and schedule_start_day=:day and set_alarm=1")
    fun getAlarm(year:Int, month:Int, day:Int):List<ScheduleInfo>

    //set_sleep_alarm이 true인 일정 가져오기
    @Query("SELECT * FROM schedule_database WHERE schedule_start_year=:year and schedule_start_month=:month and schedule_start_day=:day and set_sleep_alarm=1 ORDER BY sleep_alarm_hour, sleep_alarm_minute")
    fun getSleepAlarm(year:Int, month:Int, day:Int):List<ScheduleInfo>

    //내일 일정인데 오늘 알람이 울려야 하는 일정 가져오기
    @Query("SELECT * FROM schedule_database WHERE schedule_start_year=:year and schedule_start_month=:month and schedule_start_day=:day and set_alarm=1 and alarm_hour<0")
    fun getTomorrowAlarm(year:Int, month:Int, day:Int):List<ScheduleInfo>

    //내일 일정인데 오늘 취침알람이 울려야 하는 일정 가져오기
    @Query("SELECT * FROM schedule_database WHERE schedule_start_year=:year and schedule_start_month=:month and schedule_start_day=:day and set_sleep_alarm=1 and sleep_alarm_hour<0")
    fun getTomorrowSleepAlarm(year:Int, month: Int, day: Int):List<ScheduleInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE) //충돌이 발생하면 기존데이터와 입력데이터를 교체
    suspend fun insert(scheduleInfo: ScheduleInfo)

    //전달받은 매개변수의 기본키 값에 매칭되는 entity를 찾아 삭제
    @Delete
    suspend fun delete(scheduleInfo: ScheduleInfo)

    //전달받은 매개변수의 기본키 값에 매칭되는 entity를 찾아 갱신
    @Update
    suspend fun update(scheduleInfo: ScheduleInfo)

}