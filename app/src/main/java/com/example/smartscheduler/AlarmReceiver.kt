package com.example.smartscheduler

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat.getSystemService
import com.example.smartscheduler.Activity.MainActivity
import com.example.smartscheduler.Database.ScheduleInfo

class AlarmReceiver : BroadcastReceiver() {
    // https://reakwon.tistory.com/m/184
    // https://hanyeop.tistory.com/217
    lateinit var info:ScheduleInfo
    companion object {
        /* 아이디 선언 */
        const val NOTIFICATION_CHANNEL_ID = "1000"
        const val NOTIFICATION_ID = 100
    }
    /* onReceive: 알람 시간이 되었을 때 동작 */
    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.getBundleExtra("bundle")
        if(bundle != null){
            info = bundle.getSerializable("alarmInfo") as ScheduleInfo
        }
        createNotificationChannel(context)
        notifyNotification(context)
    }
    private fun createNotificationChannel(context: Context) {
        /* notification을 띄우기 위해 channel을 등록 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, //채널의 아이디
                "준비 알람", //채널의 이름
                NotificationManager.IMPORTANCE_HIGH //IMPORTANCE_HIGH: 알림음이 울리고 헤드업 알림으로 표시
            )

            NotificationManagerCompat.from(context)
                .createNotificationChannel(notificationChannel)
        }
    }
    private fun notifyNotification(context: Context) {
        /* notification 등록 */
        with(NotificationManagerCompat.from(context)) {
            val contentIntent = Intent(context, MainActivity::class.java)
            val contentPendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                contentIntent, //알람 클릭 시 이동할 인텐트
                PendingIntent.FLAG_UPDATE_CURRENT //LAG_UPDATE_CURRENT : 현재 PendingIntent를 유지하고, 대신 인텐트의 extra data는 새로 전달된 Intent로 교체
            )

            val build = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("'${info.scheduleExplain}'을 준비할 시간입니다") //제목
                .setContentText("${info.scheduleStartHour}:${info.scheduleStartMinute}~${info.scheduleFinishHour}:${info.scheduleFinishMinute}" +
                        " 이동 시간 ${info.elapsedTime} 분 예상") //내용
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_alarm_on_24) //아이콘
                .setAutoCancel(true)

            /* 알람을 보여주기 위해 화면을 킨다 */
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK or
            PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,"My:Tag")
            wakeLock.acquire(5000)

            /* notification을 동작시킨다 */
            notify(NOTIFICATION_ID, build.build())
        }
    }
}