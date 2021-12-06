package com.example.smartscheduler

interface OnScheduleClickListener {
    fun modify(position:Int)
    fun route(position: Int)
    fun delete(position:Int)
    fun carroute(position:Int)
}