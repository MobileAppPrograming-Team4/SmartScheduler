package com.example.smartscheduler.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartscheduler.*
import com.example.smartscheduler.Database.ScheduleInfo
import com.example.smartscheduler.Database.ScheduleViewModel
import com.example.smartscheduler.Decorator.SaturdayDecorator
import com.example.smartscheduler.Decorator.SundayDecorator
import com.example.smartscheduler.Decorator.TodayDecorator
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var selectedDateTextView: TextView
    private lateinit var addSchedule: Button
    private lateinit var calendarView: com.prolificinteractive.materialcalendarview.MaterialCalendarView
    var selectedYear: Int? = null
    var selectedMonth: Int? = null
    var selectedDate: Int? = null
    private var doubleBackToExit = false
    private lateinit var adapter: ScheduleInfoAdapter
    private lateinit var scheduleViewModel: ScheduleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)
        val recyclerView =
            findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
        adapter = ScheduleInfoAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        val mCalendar = Calendar.getInstance()
        selectedYear = mCalendar.get(Calendar.YEAR)
        selectedMonth = mCalendar.get(Calendar.MONTH) + 1
        selectedDate = mCalendar.get(Calendar.DATE)

        /*
        // 사용자 정보 불러오는 법
        val userInfo: SharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE)

        var readyTime = userInfo.getInt("readyTime", -1) //외출준비시간
        var sleepTime = userInfo.getInt("sleepTime", -1) //수면시간


        Toast.makeText(this, "준비시간 $readyTime, 수면시간 $sleepTime", Toast.LENGTH_LONG).show()
        */
        selectedDateTextView = findViewById(R.id.todayTextView)
        calendarView = findViewById(R.id.calendarView)


        scheduleViewModel = ViewModelProvider(this).get(ScheduleViewModel::class.java)

        scheduleViewModel.getAllDate().observe(this, Observer { scheduleList ->
            scheduleList?.let { adapter.setData(it) }
        })


        initCalendarView() //달력 설정

        try {
            /* 일정 설정 화면에서 작성한 일정 정보들 받아오기 */
            val newSchedule: ScheduleInfo =
                getIntent()?.getSerializableExtra("newSchedule") as ScheduleInfo
            Log.d(
                "MainActivity",
                "${newSchedule.scheduleExplain}, ${newSchedule.scheduleStartYear}, ${newSchedule.scheduleStartMonth}, ${newSchedule.scheduleStartDay}, ${newSchedule.scheduleStartHour}:${newSchedule.scheduleStartMinute},${newSchedule.transportation},${newSchedule.setAlarm}"
            )
            /* insert newSchedule into schedule_database */
            scheduleViewModel.insert(newSchedule)

        } catch (e: NullPointerException) {
        }


        /* 일정 추가하기 버튼 클릭*/
        addSchedule = findViewById(R.id.addScheduleButton)
        addSchedule.setOnClickListener {
            /* 일정 설정 화면으로 이동 */
            val intent = Intent(this, AddScheduleActivity::class.java)
            intent.putExtra("year", selectedYear!!)
            intent.putExtra("month", selectedMonth!!)
            intent.putExtra("date", selectedDate!!)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        /* onBackPressed() 메소드가 최초 호출될 때 doubleBackToExit을 true으로 변경
        *  1.5초이내 재 클릭시에는 true로 인해 액티비티가 종료
        *  1.5초 이후에는 Handler에 의해 다시 false로 변경되어 다시 토스트 메시지가 나타납니다
        * */
        if (doubleBackToExit) {
            finishAffinity()
        } else {
            Toast.makeText(this, "뒤로가기를 한 번 더 눌러 종료", Toast.LENGTH_LONG).show()
            doubleBackToExit = true
            runDelayed { doubleBackToExit = false }
        }
    }

    private fun runDelayed(function: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(function, 1500L)
    }


    private fun initCalendarView() {
        calendarView.selectedDate = CalendarDay.today()
        calendarView.state().edit()
            .setFirstDayOfWeek(Calendar.SUNDAY)          // 일주일 시작을 일요일으로
            .setCalendarDisplayMode(CalendarMode.MONTHS) // 달력 모드: 월
            .commit()

        showDate(selectedYear!!, selectedMonth!!, selectedDate!!)


        val sundayDecorator = SundayDecorator()          //일요일 글자 색을 빨간색으로 변경
        val saturdayDecorator = SaturdayDecorator()      //토요일 글자 색을 파란색으로 변경
        val todayDecorator = TodayDecorator(this) //오늘 글자 색 변경
        calendarView.setOnDateChangedListener { widget, date, selected ->

            selectedYear = date.year
            selectedMonth = date.month + 1
            selectedDate = date.date.date
            showDate(selectedYear!!, selectedMonth!!, selectedDate!!)

            scheduleViewModel.year = selectedYear as Int
            scheduleViewModel.month = selectedMonth as Int
            scheduleViewModel.date = selectedDate as Int
            scheduleViewModel.getAllDate().observe(this, Observer { scheduleList ->
                scheduleList?.let { adapter.setData(it) }
            })
        }

        calendarView.addDecorators(
            sundayDecorator,
            saturdayDecorator,
            todayDecorator
        ) //decorator 추가

    }

    private fun showDate(selectedYear: Int, selectedMonth: Int, selectedDate: Int) {
        selectedDateTextView.setText("${selectedYear}년 ${selectedMonth}월 ${selectedDate}일")
    }


}