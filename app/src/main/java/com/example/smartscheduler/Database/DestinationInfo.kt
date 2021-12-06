package com.example.smartscheduler.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "destination_database")
data class DestinationInfo(
    @PrimaryKey(autoGenerate = true) var sId:Int = 0,// autoGenerate = true , 자동으로 PrimaryKey 생성해줌
    @ColumnInfo(name = "destination_name") var destinationName: String, // 일정 내용
    @ColumnInfo(name = "destination_address") var destinationAddress: String // 일정 년도

):Serializable