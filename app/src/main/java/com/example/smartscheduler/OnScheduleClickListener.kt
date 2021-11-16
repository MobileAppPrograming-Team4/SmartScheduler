package com.example.smartscheduler

interface OnScheduleClickListener {
    fun modify(position:Int)
    fun delete(position:Int)
}