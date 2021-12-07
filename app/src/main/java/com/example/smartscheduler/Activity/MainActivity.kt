package com.example.smartscheduler.Activity

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color.parseColor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartscheduler.*
import com.example.smartscheduler.Database.ScheduleInfo
import com.example.smartscheduler.Database.ScheduleViewModel
import com.example.smartscheduler.Decorator.*
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var selectedDateTextView: TextView
    private lateinit var addSchedule: Button
    private lateinit var calendarView: com.prolificinteractive.materialcalendarview.MaterialCalendarView
    private lateinit var settingBt : ImageButton
    var currentYear: Int? = null  //오늘 년
    var currentMonth: Int? = null //오늘 월
    var currentDate: Int? = null  //오늘 일
    var selectedYear: Int? = null
    var selectedMonth: Int? = null
    var selectedDate: Int? = null
    private var doubleBackToExit = false
    private lateinit var adapter: ScheduleInfoAdapter
    private lateinit var scheduleViewModel: ScheduleViewModel
    lateinit var scheduleList: LiveData<List<ScheduleInfo>>
    var scheduleMonth: ArrayList<CalendarDay> = ArrayList<CalendarDay>()
    val ADD_SCHEDULE:Int = 100
    val MODIFY_SCHEDULE:Int = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("onCreate","실행")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recyclerView =
            findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
        adapter = ScheduleInfoAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.setHasFixedSize(true)
        adapter.setScheduleClickListener(onScheduleListener)
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

        initCalendarView() //달력 설정

        /* 일정 추가하기 버튼 클릭*/
        addSchedule = findViewById(R.id.addScheduleButton)
        addSchedule.setOnClickListener {
            /* 일정 설정 화면으로 이동 */
            addOrModify(null, selectedYear, selectedMonth, selectedDate)
        }
        scheduleViewModel.currentData.observe(this, Observer{
            adapter.setData(it)
        })
        scheduleViewModel.monthData.observe(this, Observer{
            if (it.isNotEmpty()){
                scheduleMonth.clear()
                for ( i in it.indices){
                    val dotYear = it[i].scheduleStartYear
                    val dotMonth = it[i].scheduleStartMonth - 1
                    val dotDate = it[i].scheduleStartDay
                    val date = Calendar.getInstance()
                    date.set(dotYear, dotMonth, dotDate)
                    scheduleMonth.add(CalendarDay.from(date))
                }
            calendarView.addDecorator(EventDecorator(parseColor("#c88719"), scheduleMonth))
            }
        })

        /* setting button 클릭 */
        settingBt = findViewById(R.id.settingButton)
        settingBt.setOnClickListener {
            /* UserInfoActivity 화면으로 이동 */
            goToUserInfo()
        }

        scheduleViewModel.sleepAlarmData.observe(this, {
            setSleepAlarm(it, this)
        })
        scheduleViewModel.alarmData.observe(this, {
            setAlarm(it, this)
        })
        scheduleViewModel.tomorrowAlarmData.observe(this, {
            setAlarm(it, this)
        })
        getAlarm()

    }
    private fun getAlarm(){
        val cal = Calendar.getInstance(Locale.KOREA)
        currentYear = cal.get(Calendar.YEAR)
        currentMonth = cal.get(Calendar.MONTH) + 1
        currentDate = cal.get(Calendar.DATE)
        //내일 날짜
        val today = cal
        cal.add(Calendar.DATE, 1)
        val tomorrowYear = today.get(Calendar.YEAR)
        val tomorrowMonth = today.get(Calendar.MONTH) + 1
        val tomorrowDate = today.get(Calendar.DATE)
        //database에서 오늘 알람이 켜져있는 일정 모두 가져오기
        scheduleViewModel.getAlarm(currentYear!!, currentMonth!!, currentDate!!)
        //database에서 오늘 취침 알람을 울려야 하는 일정 모두 가져오기
        scheduleViewModel.getSleepAlarm(tomorrowYear, tomorrowMonth, tomorrowDate)
        //database에서 내일 일정이지만 오늘 알람을 울려야 하는 일정 모두 가져오기
        scheduleViewModel.getTomorrowAlarm(tomorrowYear, tomorrowMonth, tomorrowDate)
    }

    //private val M_ALARM_REQUEST_CODE = 1000
    @TargetApi(Build.VERSION_CODES.M)
    private fun setAlarm(list: List<ScheduleInfo>, context: Context) {
        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance(Locale.KOREA)

        for(item in list){
            if(item.alarmHour!!<0) {
                // 내일 일정인데 알람은 오늘 울려야 하는 경우
                // 12월 6일 -1시에 알람이 설정되어 있으면 12월 5일 23시에 알람이 울려야 한다
                item.alarmHour = item.alarmHour!! + 24
                Log.d("내일 일정인데","오늘 ${item.alarmHour}시${item.alarmMinute}분에 울릴 예정")
            }
            //알람이 울릴 시간 설정
            calendar.set(Calendar.HOUR_OF_DAY, item.alarmHour!!)
            calendar.set(Calendar.MINUTE, item.alarmMinute!!)
            calendar.set(Calendar.SECOND, 0)

            //AlarmReceiver에 값 전달
            val intent = Intent(context, AlarmReceiver::class.java) //알람 조건이 충족되었을 때, 리시버로 전달될 인텐트 설정

            if (calendar.before(Calendar.getInstance(Locale.KOREA))) {
                //현재시간보다 빠른 알람은 취소
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    item.sId,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                pendingIntent?.cancel()
                continue
            }
            // pendingIntent로 AlarmReceiver에 알람 정보를 알려줌
            val bundle = Bundle()
            bundle.putSerializable("alarmInfo",item)
            intent.putExtra("bundle", bundle)

            val alarmIntent = PendingIntent.getBroadcast(
                context,
                item.sId,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )

            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, alarmIntent),
                alarmIntent
            )
        }
    }

    val SLEEP_ALARM_ID = 3108
    @TargetApi(Build.VERSION_CODES.M)
    private fun setSleepAlarm(list: List<ScheduleInfo>, context: Context){
        for(item in list){
            val alarmManager: AlarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            //알람이 울릴 시간 설정
            val calendar = Calendar.getInstance(Locale.KOREA)
            calendar.set(Calendar.HOUR_OF_DAY, item.sleepAlarmHour!!)
            calendar.set(Calendar.MINUTE, item.sleepAlarmMinute!!)
            calendar.set(Calendar.SECOND, 0)
            Log.d("취침 알람 시간", "${item.sleepAlarmHour}시${item.sleepAlarmMinute}분")

            if (calendar.before(Calendar.getInstance(Locale.KOREA))) {
                //현재시간보다 빠른 알람은 취소
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    SLEEP_ALARM_ID,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                pendingIntent?.cancel()
                return
            }

            //SleepAlarmReceiver에 값 전달
            val intent =
                Intent(context, SleepAlarmReceiver::class.java) //알람 조건이 충족되었을 때, 리시버로 전달될 인텐트 설정
            val alarmIntent = PendingIntent.getBroadcast(
                context,
                SLEEP_ALARM_ID,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT //PendingIntent가 이미 존재할 경우, 기존 PendingIntent를 cancel하고 다시 생성
            )

            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, alarmIntent),
                alarmIntent
            )
            break //취침 알람은 한 번만 울린다
        }
    }

    private fun goToUserInfo() {
        val intent = Intent(this, UserInfoActivity::class.java)
        val userInfo: SharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = userInfo.edit()

        editor.putInt("readyTime", 0)
        editor.putInt("sleepTime", 0)
        editor.apply()
        startActivity(intent)
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
        calendarView.setTitleFormatter {
            val titleFormat =
                SimpleDateFormat("yyyy년 MM월")
            titleFormat.format(calendarView.selectedDate.date)
        }
        selectedYear = calendarView.selectedDate.year
        selectedMonth = calendarView.selectedDate.month + 1
        selectedDate = calendarView.selectedDate.date.date
        scheduleViewModel.getAllDate(selectedYear!!, selectedMonth!!, selectedDate!!)
        scheduleViewModel.getAllMonth(selectedYear!!, selectedMonth!!)
        showDate(selectedYear!!, selectedMonth!!, selectedDate!!)


        val sundayDecorator = SundayDecorator()          //일요일 글자 색을 빨간색으로 변경
        val saturdayDecorator = SaturdayDecorator()      //토요일 글자 색을 파란색으로 변경
        val todayDecorator = TodayDecorator(this) //오늘 글자 색 변경
        calendarView.setOnDateChangedListener { widget, date, selected ->

            selectedYear = date.year
            selectedMonth = date.month + 1
            selectedDate = date.date.date
            showDate(selectedYear!!, selectedMonth!!, selectedDate!!)
            scheduleViewModel.getAllDate(selectedYear!!, selectedMonth!!, selectedDate!!)
        }
        calendarView.setOnMonthChangedListener { widget, date ->
            Log.d("달력","달 변경")
            calendarView.setTitleFormatter {
                val titleFormat =
                    SimpleDateFormat("yyyy년 MM월")
                titleFormat.format(date.date)
            }
            scheduleViewModel.getAllMonth(date.year, date.month + 1)
        }

        calendarView.addDecorators(
            sundayDecorator,
            saturdayDecorator,
            todayDecorator,
        ) //decorator 추가

    }

    private fun showDate(selectedYear: Int, selectedMonth: Int, selectedDate: Int) {
        selectedDateTextView.setText("${selectedYear}년 ${selectedMonth}월 ${selectedDate}일")
    }

    private var onScheduleListener:OnScheduleClickListener = object: OnScheduleClickListener{
        override fun delete(position: Int) {
            scheduleList = scheduleViewModel.currentData
            scheduleViewModel.delete(scheduleList.value?.get(position)!!)
            scheduleViewModel.getAllDate(selectedYear!!, selectedMonth!!, selectedDate!!)
            scheduleViewModel.getAllMonth(selectedYear!!, selectedMonth!!)
            getAlarm()
        }
        override fun modify(position: Int) {
            scheduleList = scheduleViewModel.currentData
            addOrModify(scheduleList.value?.get(position)!!, null, null, null)
        }

        override fun route(position: Int) {
            scheduleList = scheduleViewModel.currentData
            goToRoute(scheduleList.value?.get(position)!!, selectedYear!!, selectedMonth!!, selectedDate!!)
        }

        override fun carroute(position: Int){
            scheduleList = scheduleViewModel.currentData
            goToCarRoute(scheduleList.value?.get(position)!!, selectedYear!!, selectedMonth!!, selectedDate!!)
        }
    }
    fun addOrModify(scheduleInfo: ScheduleInfo?, year:Int?, month:Int?, date:Int?){
        val intent = Intent(this, AddScheduleActivity::class.java)
        if(scheduleInfo == null){ // 일정 추가
            intent.putExtra("year", year!!)
            intent.putExtra("month", month!!)
            intent.putExtra("date", date!!)
            startActivityForResult(intent, ADD_SCHEDULE)
        }else{ // 일정 편집
            intent.putExtra("mode","modify")
            intent.putExtra("beforeModify",scheduleInfo)
            startActivityForResult(intent, MODIFY_SCHEDULE)
        }
    }

    fun goToRoute(scheduleInfo: ScheduleInfo?, year: Int?, month: Int?, date: Int?) {
        val intent = Intent(this, CarRouteActivity::class.java)
        startActivity(intent)
    }

    //차량 네비게이션 시작 화면으로 이동
    fun goToCarRoute(scheduleInfo: ScheduleInfo?, year: Int?, month: Int?, date: Int?) {
        val intent = Intent(this, CarRouteActivity::class.java)
        intent.putExtra("x",scheduleInfo!!.schedulePlace_x_double)
        intent.putExtra("y",scheduleInfo!!.schedulePlace_y_double)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /* onActivityResult -> onResume */
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ADD_SCHEDULE -> {
                    val newSchedule: ScheduleInfo =
                        data!!.getSerializableExtra("scheduleInfo") as ScheduleInfo
                    /* insert newSchedule into schedule_database */
                    scheduleViewModel.insert(newSchedule)
                    Toast.makeText(this, "일정을 추가했습니다", Toast.LENGTH_LONG).show()
                    selectedYear = newSchedule.scheduleStartYear
                    selectedMonth = newSchedule.scheduleStartMonth
                    selectedDate = newSchedule.scheduleStartDay
                    scheduleViewModel.getAllDate(selectedYear!!, selectedMonth!!, selectedDate!!)
                    scheduleViewModel.getAllMonth(selectedYear!!, selectedMonth!!)
                    getAlarm()
                }
                MODIFY_SCHEDULE -> {
                    val modifySchedule: ScheduleInfo = data!!.getSerializableExtra("scheduleInfo") as ScheduleInfo
                    /* update schedule_database */
                    scheduleViewModel.update(modifySchedule)
                    Toast.makeText(this, "일정을 변경했습니다", Toast.LENGTH_LONG).show()
                    selectedYear = modifySchedule.scheduleStartYear
                    selectedMonth = modifySchedule.scheduleStartMonth
                    selectedDate = modifySchedule.scheduleStartDay
                    scheduleViewModel.getAllDate(selectedYear!!, selectedMonth!!, selectedDate!!)
                    getAlarm()
                }
            }
        }
    }
}