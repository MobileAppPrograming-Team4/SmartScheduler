package com.example.smartscheduler.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.smartscheduler.*
import java.util.*

class AddScheduleActivity : AppCompatActivity(), BottomSetScheduleFragment.CompleteListener {
    lateinit var startTimeTextView: TextView
    lateinit var finishTimeTextView: TextView
    lateinit var cal: Calendar
    lateinit var transportGroup: RadioGroup
    var startHour = 0
    var startMinute = 0
    var finishHour = 0
    var finishMinute = 0
    var year = 0
    var month = 0
    var date = 0
    var elapsedTime: Int? = null

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addschedule)

        startTimeTextView = findViewById(R.id.startTimeTextView)
        finishTimeTextView = findViewById(R.id.finishTimeTextView)
        year = getIntent().getIntExtra("year", 0)
        month = getIntent().getIntExtra("month", 0)
        date = getIntent().getIntExtra("date", 0)

        val scheduleExplain = findViewById<EditText>(R.id.scheduleExplain)
        scheduleTime()
        /* transportType */
        var transportType: Int? = null
        transportGroup = findViewById(R.id.transportGroup)
        transportGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                R.id.publicTransport -> {
                    transportType = 0
                }
                R.id.car -> {
                    transportType = 1
                }
                R.id.walk -> {
                    transportType = 2
                }
                else -> transportType = null
            }
        }
        /* place Information */
        var isAlarmOn: Boolean = true
        val setAlarmSwitch = findViewById<Switch>(R.id.setAlarm)
        setAlarmSwitch.isChecked = true
        setAlarmSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            isAlarmOn = isChecked
        }

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            if (scheduleExplain.text.toString().isNotEmpty()) {
                val scheduleInfo = ScheduleInfo(
                    0,
                    scheduleExplain.text.toString(),
                    year,
                    month,
                    date,
                    startHour,
                    startMinute,
                    finishHour,
                    finishMinute,
                    null,
                    null,
                    transportType,
                    null,
                    isAlarmOn
                )
                Log.d(
                    "Addschedule",
                    "${scheduleExplain.text}, ${year}, ${month}, ${date}, ${startHour}:${startMinute},${transportType},${isAlarmOn}"
                )
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("newSchedule", scheduleInfo)
                finish()
                startActivity(intent)

            } else {
                Toast.makeText(this, "일정 내용을 입력해주세요", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun setTime(hour: Int, minute: Int, startOrFinish: Int) {
        /* interface CompleteListener의 함수 */
        if (startOrFinish == 0) { //시작 시각 설정
            startHour = hour
            startMinute = minute
            finishHour = hour + 1
            finishMinute = minute
            startTimeTextView.text = showTimeTextView(month, date, startHour, startMinute)
            finishTimeTextView.text = showTimeTextView(month, date, finishHour, finishMinute)
        } else if (startOrFinish == 1) { //종료 시각 설정
            finishHour = hour
            finishMinute = minute
            finishTimeTextView.text = showTimeTextView(month, date, finishHour, finishMinute)
        }
    }

    private fun scheduleTime() {
        /* 일정 시간 */
        cal = Calendar.getInstance()
        startHour = cal.get(Calendar.HOUR_OF_DAY)
        startMinute = cal.get(Calendar.MINUTE)
        finishHour = startHour + 1
        finishMinute = startMinute

        startTimeTextView.text = showTimeTextView(month, date, startHour, startMinute)
        finishTimeTextView.text = showTimeTextView(month, date, finishHour, finishMinute)

        /* activity -> fragment 데이터 전달 */
        val bottomSetSchedule = BottomSetScheduleFragment()
        val bundle = Bundle()
        bundle.putInt("year", year)
        bundle.putInt("month", month)
        bundle.putInt("date", date)
        startTimeTextView.setOnClickListener {  //시작 시각 설정
            bundle.putInt("startOrFinish", 0)
            bottomSetSchedule.arguments = bundle //fragment의 arguments에 데이터를 담은 bundle을 넘겨줌
            bottomSetSchedule.show(supportFragmentManager, bottomSetSchedule.tag)
        }
        finishTimeTextView.setOnClickListener {  //종료 시각 설정
            bundle.putInt("startOrFinish", 1)
            bottomSetSchedule.arguments = bundle //fragment의 arguments에 데이터를 담은 bundle을 넘겨줌
            bottomSetSchedule.show(supportFragmentManager, bottomSetSchedule.tag)
        }
    }

    private fun showTimeTextView(month: Int, date: Int, hour: Int, minute: Int): String {
        return "${month}월 ${date}일 ${hour}시 ${minute}분"
    }


}