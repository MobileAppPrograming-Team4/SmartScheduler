package com.example.smartscheduler.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.smartscheduler.*
import com.example.smartscheduler.Database.ScheduleInfo
import java.util.*

import com.odsay.odsayandroidsdk.API
import com.odsay.odsayandroidsdk.ODsayData
import com.odsay.odsayandroidsdk.ODsayService
import com.odsay.odsayandroidsdk.OnResultCallbackListener
import kotlinx.android.synthetic.main.activity_addschedule.*
import java.net.URLEncoder
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.concurrent.TimeUnit

class AddScheduleActivity : AppCompatActivity(), BottomSetScheduleFragment.CompleteListener {
    lateinit var startTimeTextView: TextView
    lateinit var finishTimeTextView: TextView
    lateinit var cal: Calendar
    lateinit var transportGroup: RadioGroup
    lateinit var searchButton: ImageButton
    lateinit var expectedtimeTextView : TextView
    lateinit var expectedtime1: TextView
    lateinit var carRadioButton: RadioButton
    var startHour = 0
    var startMinute = 0
    var finishHour = 0
    var finishMinute = 0
    var year = 0
    var month = 0
    var date = 0
    var totalTime: Int? = null
    var alarmHour = 0
    var alarmMinute = 0
    var destName: String? = null
    var destAddress: String? = null
    var destRoad: String? = null
    var destLongitude: Double? = 0.0
    var destLatitude: Double? = 0.0
    var sleepAlarmHour: Int? = null
    var sleepAlarmMinute: Int? = null
    var isSleepAlarmOn = false
    var transportType: Int? = null
    var isAlarmOn: Boolean = false

    lateinit var odsayService: ODsayService
    lateinit var jsonObject: JSONObject

    val tmapKey: String = "l7xx6856c73aa91c41e480afc960d336d8c3"

    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 28f1a9b662dea4d3296bfaa59f4590b3"  // REST API 키
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addschedule)

        startTimeTextView = findViewById(R.id.startTimeTextView)
        finishTimeTextView = findViewById(R.id.finishTimeTextView)
        searchButton = findViewById(R.id.locationSearchButton)

        var sId: Int = 0
        val getIntent = getIntent()
        if (!TextUtils.isEmpty(getIntent.getStringExtra("mode"))) {
            //일정 변경
            val info = getIntent.getSerializableExtra("beforeModify") as ScheduleInfo
            sId = info.sId
            setInfo(info)
        } else {
            //일정 추가
            year = getIntent.getIntExtra("year", 0)
            month = getIntent.getIntExtra("month", 0)
            date = getIntent.getIntExtra("date", 0)

            cal = Calendar.getInstance()
            startHour = cal.get(Calendar.HOUR_OF_DAY)
            startMinute = cal.get(Calendar.MINUTE)
            finishHour = startHour + 1
            finishMinute = startMinute
            if (finishHour >= 24) {
                finishHour = 23
                finishMinute = 59
            }
        }

        searchButton.setOnClickListener {
            val intent = Intent(this, DestinationSearchActivity::class.java)
            startActivityForResult(intent, 0)
        }

        val scheduleExplain = findViewById<EditText>(R.id.scheduleExplain)
        scheduleTime()
        /* transportType */
        transportGroup = findViewById(R.id.transportGroup)
        transportGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                R.id.publicTransport -> {
                    transportType = 0
                    setPublicTime()
                }
                R.id.car -> {
                    transportType = 1
                    carRadioButton = findViewById(R.id.car)
                    carRadioButton.setOnClickListener{
                        setCarTime()        //자동차 선택시 예상소요시간에 시간출력
                    }
                }
                R.id.walk -> {
                    transportType = 2
                    setWalkTime()
                }
                else -> {
                    transportType = null
                }
            }
        }

        /* place Information */

        val setAlarmSwitch = findViewById<Switch>(R.id.setAlarm)
        setAlarmSwitch.isChecked = isAlarmOn
        setAlarmSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            isAlarmOn = isChecked
        }

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            try {
                // 저장하기 버튼을 누르면
                // 1. 소요시간 계산
                when (transportType) {
                    0 -> setPublicTime()
                    1 -> setCarTime()
                    2 -> setWalkTime()
                    else -> totalTime = 0
                }
                // 2. 출발 알람이 켜져있으면 알람이 울릴 시간 계산
                if (isAlarmOn) {
                    calculateAlarmClock(totalTime!!)
                }
                // 3. 일정내용이 비어있지 않으면 scheduleInfo를 MainActivity로 넘김
                if (scheduleExplain.text.toString().isNotEmpty()) {
                    val scheduleInfo = ScheduleInfo(
                        sId,
                        scheduleExplain.text.toString(),
                        year,
                        month,
                        date,
                        startHour,
                        startMinute,
                        finishHour,
                        finishMinute,
                        null, //수정 금지(int형)
                        null, //수정 금지(int형)
                        destLongitude, //좌표 입력(double)
                        destLatitude, //좌표 입력(double)
                        transportType,
                        totalTime,
                        alarmHour,
                        alarmMinute,
                        isAlarmOn,
                        sleepAlarmHour,
                        sleepAlarmMinute,
                        isSleepAlarmOn,
                        destName
                    )
                    Log.d(
                        "Addschedule",
                        "${scheduleExplain.text}, ${year}, ${month}, ${date}, ${startHour}:${startMinute},${transportType},${isAlarmOn},${destName}"
                    )
                    val intent = Intent()
                    intent.putExtra("scheduleInfo", scheduleInfo)
                    setResult(Activity.RESULT_OK, intent)
                    // 4. AddScheduleActivity 종료
                    finish()
                } else {
                    Toast.makeText(this, "일정 내용을 입력해주세요", Toast.LENGTH_LONG).show()
                }
            }
            catch (e: Exception) {
                Toast.makeText(this, "일정 정보를 입력해주세요", Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun setTime(hour: Int, minute: Int, startOrFinish: Int) {
        /* BottomSetScheduleFragment.kt CompleteListener 인터페이스 */
        if (startOrFinish == 0) { //시작 시각 설정
            startHour = hour
            startMinute = minute
            finishHour = hour + 1
            finishMinute = minute
            if (finishHour >= 24) {
                finishHour = 23
                finishMinute = 59
            }
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

    private fun setInfo(scheduleInfo: ScheduleInfo) {
        // 편집 눌렀을 때 기존 정보 불러오기
        findViewById<TextView>(R.id.scheduleExplain).text = scheduleInfo.scheduleExplain
        year = scheduleInfo.scheduleStartYear
        month = scheduleInfo.scheduleStartMonth
        date = scheduleInfo.scheduleStartDay
        startHour = scheduleInfo.scheduleStartHour
        startMinute = scheduleInfo.scheduleStartMinute
        finishHour = scheduleInfo.scheduleFinishHour
        finishMinute = scheduleInfo.scheduleFinishMinute
        when(scheduleInfo.transportation){
            //교통편  0: 대중교통, 1: 자동차, 2: 도보
            0 -> {
                transportType = scheduleInfo.transportation
                findViewById<RadioButton>(R.id.publicTransport).isChecked = true
            }
            1 -> {
                findViewById<RadioButton>(R.id.car).isChecked = true
                transportType = scheduleInfo.transportation
            }
            2 -> {
                findViewById<RadioButton>(R.id.walk).isChecked = true
                transportType = scheduleInfo.transportation
            }
        }
        // 장소
        if(scheduleInfo.schedulePlace_name != null) {
            destName = scheduleInfo.schedulePlace_name
            findViewById<TextView>(R.id.locationText).text = destName //장소 이름
            destLongitude = scheduleInfo.schedulePlace_x_double // 좌표
            destLatitude = scheduleInfo.schedulePlace_y_double  // 좌표
        }
        // 소요시간
        if(scheduleInfo.elapsedTime != null){
            totalTime = scheduleInfo.elapsedTime //단위: 분
            findViewById<TextView>(R.id.expectedtimeTextView).text = expectedtimetoString(totalTime!! * 60)
        }
        // 알람 on/off
        isAlarmOn = scheduleInfo.setAlarm
    }

    private fun setPublicTime(): Unit {
        val userInfo: SharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE)

        val depX = userInfo.getFloat("userLongitude", 0.0f)
        val depY = userInfo.getFloat("userLatitude", 0.0f)

        odsayService = ODsayService.init(
            this,
            "2hQo8uVDi/FJlk7TilYT8gwEYlBs89Wib0Pc93yLrus"
        )
        odsayService.setConnectionTimeout(5000)
        odsayService.setReadTimeout(5000)

        odsayService.requestSearchPubTransPath(
            depX.toString(),
            depY.toString(),
            destLongitude.toString(),
            destLatitude.toString(),
            null,
            null,
            null,
            object : OnResultCallbackListener {
                override fun onSuccess(odsayData: ODsayData, api: API) {
                    jsonObject = odsayData!!.json

                    val routeResult = jsonObject.getJSONObject("result")
                    val pathArray = routeResult.getJSONArray("path")
                    val firstPath = pathArray.getJSONObject(0)
                    val pathInfo = firstPath.getJSONObject("info")

                    totalTime = pathInfo.getInt("totalTime")

                    expectedtime1 = findViewById(R.id.expectedtimeTextView)
                    expectedtime1.setText(totalTime.toString() + "분")
                }

                override fun onError(code: Int, message: String, api: API) {
                    totalTime = -1
                }
            }
        )

        // return totalTime
    }

    private fun setWalkTime(): Unit {
        val BASE_URL_TMAP_API = tmapKey
        val API_KEY = "l7xx6856c73aa91c41e480afc960d336d8c3"

        val userInfo: SharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE)

        val startX = userInfo.getFloat("userLongitude", 0.0f)
        val startY = userInfo.getFloat("userLatitude", 0.0f)

        var endX: String = destLongitude.toString()
        var endY: String = destLatitude.toString()
        var reqCoordType: String = "WGS84GEO"
        var startName: String = URLEncoder.encode("출발", "UTF-8")
        var endName: String = URLEncoder.encode("도착", "UTF-8")
        var searchOption: String = "0"
        var resCoordType = "WGS84GEO"

        val api = TmapAPI.create()
        val callGetSearchWalkRoute = api.getSearchWalkRoute(
            "application/json",
            tmapKey,
            "application/json; charset=UTF-8",
            startX.toString(),
            startY.toString(),
            endX,
            endY,
            reqCoordType,
            startName,
            endName,
            searchOption,
            resCoordType
        )

        var data: String
        var tmp: Int = 0
        var myHandler = Handler()

        callGetSearchWalkRoute.enqueue(object : Callback<ResultWalkRouteSearch> {
            override fun onResponse(
                call: Call<ResultWalkRouteSearch>,
                response: Response<ResultWalkRouteSearch>
            ) {
                Log.d("결과", "성공 : ${response.raw()}")
                Log.d("결과", "성공 : ${response.body()}")
                tmp = response.body()!!.features[0].properties.totalTime

                totalTime = (tmp / 60) + 1

                myHandler.post {
                    expectedtime1 = findViewById(R.id.expectedtimeTextView)
                    expectedtime1.setText(totalTime.toString() + "분")
                }
            }

            override fun onFailure(call: Call<ResultWalkRouteSearch>, t: Throwable) {
                Log.d("결과", "실패 : ${t.message}")
            }
        })
    }

    //자동차 예상 소요 시간을 계산 후, 초 단위로 totalTime에 저장, 리턴
    private fun setCarTime(){

        val userInfo: SharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE)
        val userLongitude = userInfo.getFloat("userLongitude",0.0f)         //출발지 경도
        val userLatitude = userInfo.getFloat("userLatitude",0.0f)           //출발지 위도

        var origin = (userLongitude.toString() + "," + userLatitude.toString())  //출발지 좌표
        var dest = (destLongitude.toString() + "," + destLatitude.toString()) //목적지 좌표

        expectedtimeTextView = findViewById(R.id.expectedtimeTextView)
        if(origin.equals("0.0,0.0") && dest.equals("0.0,0.0")){
            expectedtimeTextView.setText("목적지와 출발지가 설정되지 않았습니다.")
            return
        }
        if(origin.equals("0.0,0.0")){
            expectedtimeTextView.setText("출발지가 설정되지 않았습니다.")
            return
        }
        if(dest.equals("0.0,0.0")){
            expectedtimeTextView.setText("목적지가 설정되지 않았습니다.")
            return
        }

        val api = kakaonaviAPI.create()
        val callGetSearchCarRoute = api.getSearchCarRoute(API_KEY,origin,dest)

        callGetSearchCarRoute.enqueue(object : Callback<ResultCarRouteSearch> {
            override fun onResponse(
                call: Call<ResultCarRouteSearch>,
                response: Response<ResultCarRouteSearch>
            ) {
                Log.d("결과","성공 : ${response.raw()}")
                Log.d("결과","성공 : ${response.body()}")
                totalTime = response.body()!!.routes[0].summary.duration / 60

                expectedtimeTextView.setText(expectedtimetoString(response.body()!!.routes[0].summary.duration))
                //duration(예상소요시간)을 초단위로 받아오기때문에 출력에 적합한 포맷으로 바꾼 후 텍스트뷰에 넣음
            }
            override fun onFailure(call: Call<ResultCarRouteSearch>, t: Throwable) {
                Log.d("결과","실패 : ${t.message}")
            }
        })
    }

    //Int형의 초 단위 에상 소요 시간을 포맷에 맞춰 계산
    fun expectedtimetoString(intseconds: Int): String {
        val seconds: Long = intseconds.toLong()
        val day = TimeUnit.SECONDS.toDays(seconds).toInt()
        val hours = TimeUnit.SECONDS.toHours(seconds) - day * 24
        val minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60
        val second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60
        val time = (day.toString() + "일 " + hours + "시간 " + minute + "분 " + second + "초")

        return time
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            destName = data!!.getStringExtra("destName")
            destAddress = data!!.getStringExtra("destAddress")
            destRoad = data!!.getStringExtra("destRoad")
            destLongitude = data!!.getDoubleExtra("destLongitude", 0.0)
            destLatitude = data!!.getDoubleExtra("destLatitude", 0.0)
            locationText.setText("위치 : " + destName)
            Log.d(
                "newdestination : ",
                "받은 로그 name : $destName \n address : $destAddress \n road : $destRoad \n lat : $destLatitude \n long : $destLongitude"
            )
        }
    }

    private fun calculateAlarmClock(elapsedTime: Int) {
        //알람 시간(나갈 준비를 해야 하는 시간)을 계산: 일정 시작 시간 - 이동 소요 시간 - 외출 준비 시간
        //elapsedTime ; 출발 ~ 도착지 소요시간(단위 : 분)
        Log.d("소요시간", "${elapsedTime}분")
        // 사용자 정보 불러오기
        val userInfo: SharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE)
        val readyTime = userInfo.getInt("readyTime", 0) //외출준비시간(단위: 시간)
        val sleepTime = userInfo.getInt("sleepTime", 0) //수면시간(단위: 시간)

        // 소요시간의 단위를 (분)에서 (시+분)으로 변환
        val elapsedTime_hour = elapsedTime / 60
        val elapsedTime_minute = elapsedTime % 60

        // 알람 시간 계산
        alarmMinute = startMinute - elapsedTime_minute
        alarmHour = startHour - elapsedTime_hour - readyTime
        if (alarmMinute < 0) {
            alarmMinute += 60
            alarmHour -= 1
        }

        // 외출준비 알람이 울리는 시간 - 수면시간이 선택한 날의 이전일 경우 취침 알람을 킴
        // 예시: 12월 3일에 외출준비 시간 알람이 7시에 울려야 하고, 수면 시간이 8시간일 때, 12월 2일 23시에 취침 시간을 알려줌
        if (alarmHour - sleepTime < 0) {
            // 취침 알람 시간 계산
            sleepAlarmHour = alarmHour - sleepTime + 24
            sleepAlarmMinute = alarmMinute
            Log.d("취침 알람", "${sleepAlarmHour}:${sleepAlarmMinute}")
            isSleepAlarmOn = true
        }
    }
}