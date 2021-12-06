package com.example.smartscheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.smartscheduler.Activity.MainActivity

class SleepAlarmReceiver : BroadcastReceiver() {
    companion object {
        /* 아이디 선언 */
        const val NOTIFICATION_CHANNEL_ID = "1001"
        const val SLEEP_ALARM_ID = 3108
    }
    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)
        notifyNotification(context)
    }
    private fun createNotificationChannel(context: Context) {
        /* notification을 띄우기 위해 channel을 등록 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, //채널의 아이디
                "취침 알람", //채널의 이름
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
                SLEEP_ALARM_ID,
                contentIntent, //알람 클릭 시 이동할 인텐트
                PendingIntent.FLAG_UPDATE_CURRENT //FLAG_UPDATE_CURRENT : 현재 PendingIntent를 유지하고, 대신 인텐트의 extra data는 새로 전달된 Intent로 교체
            )

            val build = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("취침 알람") //제목
                .setContentText("내일 일정을 위해 취침해야 할 시간입니다") //내용
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_baseline_alarm_on_24) //아이콘
                .setAutoCancel(true)

            /* notification을 동작시킨다 */
            notify(SLEEP_ALARM_ID, build.build())
        }
    }

}