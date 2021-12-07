package com.example.smartscheduler.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "schedule_database")
data class ScheduleInfo(
    @PrimaryKey(autoGenerate = true) var sId:Int = 0,// autoGenerate = true , 자동으로 PrimaryKey 생성해줌
    @ColumnInfo(name = "schedule_explain") var scheduleExplain: String, // 일정 내용
    @ColumnInfo(name = "schedule_start_year") var scheduleStartYear:Int, // 일정 년도
    @ColumnInfo(name = "schedule_start_month") var scheduleStartMonth:Int, // 일정 월
    @ColumnInfo(name = "schedule_start_day") var scheduleStartDay:Int, // 일정 일
    @ColumnInfo(name = "schedule_start_hour") var scheduleStartHour: Int, // 시작 시
    @ColumnInfo(name = "schedule_start_minute") var scheduleStartMinute: Int, // 시작 분
    @ColumnInfo(name = "schedule_finish_hour") var scheduleFinishHour: Int, // 종료 시
    @ColumnInfo(name = "schedule_finish_minute") var scheduleFinishMinute: Int, // 종료 분
    @ColumnInfo(name = "schedule_place_x") var schedulePlace_x: Int?, // 장소 x(쓰면안됨)
    @ColumnInfo(name = "schedule_place_y") var schedulePlace_y: Int?, // 장소 y(쓰면안됨)
    @ColumnInfo(name = "schedule_place_x_double") var schedulePlace_x_double: Double?, // 장소 x(double)
    @ColumnInfo(name = "schedule_place_y_double") var schedulePlace_y_double: Double?, // 장소 y(double)
    @ColumnInfo(name = "transportation") var transportation: Int?, //0: 대중교통, 1: 자동차, 2: 도보
    @ColumnInfo(name = "elapsed_time") var elapsedTime: Int?, // 예상이동시간
    @ColumnInfo(name = "alarm_hour") var alarmHour: Int?, //알람 시각(시)
    @ColumnInfo(name = "alarm_minute") var alarmMinute: Int?, //알람 시각(분)
    @ColumnInfo(name = "set_alarm") var setAlarm: Boolean, // 알람설정 여부
    @ColumnInfo(name = "sleep_alarm_hour") var sleepAlarmHour:Int?, //취침 알람 (시)
    @ColumnInfo(name = "sleep_alarm_minute") var sleepAlarmMinute: Int?, //취침 알람 (분)
    @ColumnInfo(name = "set_sleep_alarm") var setSleepAlarm: Boolean, // 취침 시간 알람설정 여부
    @ColumnInfo(name = "schedulePlace_name") var schedulePlace_name: String? // 약속 장소 이름(주소 아님)
):Serializable

