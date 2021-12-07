package com.example.smartscheduler.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = arrayOf(ScheduleInfo::class), version = 5)
abstract class ScheduleDatabase : RoomDatabase() {
    //데이터베이스를 매번 생성하는건 리소스를 많이 사용하므로 싱글톤 권장
    abstract fun scheduleInfoDao(): ScheduleInfoDao


    companion object{
        /* @Volatile = 접근가능한 변수의 값을 cache를 통해 사용하지 않고
        thread가 직접 main memory에 접근 하게하여 동기화. */
        @Volatile
        private var INSTANCE: ScheduleDatabase? = null

        fun getDatabase(context:Context): ScheduleDatabase {
            /* 데이터베이스를 만들어 Room의 데이터베이스 빌더를 사용하여 ScheduleDatabase 클래스의 애플리케이션 컨텍스트에서
             * ScheduleDatabase 객체를 만들고 이름을 "schedule_database"로 지정 */
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScheduleDatabase::class.java,
                    "schedule_database"
                )
                    .addMigrations(MIGRATION_1_2) //addMigrations 추가
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration
        // https://developer.android.com/training/data-storage/room/migrating-db-versions?hl=ko
        // 알람 시간 추가
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE 'schedule_database' ADD COLUMN 'alarm_hour' INTEGER")
                database.execSQL("ALTER TABLE 'schedule_database' ADD COLUMN 'alarm_minute' INTEGER")
            }
        }
        // 취침 알람 시간 추가
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE 'schedule_database' ADD COLUMN 'sleep_alarm_hour' INTEGER")
                database.execSQL("ALTER TABLE 'schedule_database' ADD COLUMN 'sleep_alarm_minute' INTEGER")
                database.execSQL("ALTER TABLE 'schedule_database' ADD COLUMN 'set_sleep_alarm' INTEGER NOT NULL DEFAULT 0")
            }
        }
        // 장소 Double로 변경
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE 'schedule_database' ADD COLUMN 'schedule_place_x_double' DOUBLE")
                database.execSQL("ALTER TABLE 'schedule_database' ADD COLUMN 'schedule_place_y_double' DOUBLE")
            }
        }
        // 일정 장소 이름 추가
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE 'schedule_database' ADD COLUMN 'schedulePlace_name' TEXT")
            }
        }
    }

}
