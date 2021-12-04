package com.example.smartscheduler.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.smartscheduler.*
import com.example.smartscheduler.Database.ScheduleInfo
import java.util.*

import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;
import net.daum.mf.map.api.MapView

import org.json.JSONObject;
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AddScheduleActivity : AppCompatActivity(), BottomSetScheduleFragment.CompleteListener {
    lateinit var startTimeTextView: TextView
    lateinit var finishTimeTextView: TextView
    lateinit var cal: Calendar
    lateinit var transportGroup: RadioGroup
    lateinit var map: ConstraintLayout
    lateinit var location: EditText
    lateinit var searchButton: ImageButton
    lateinit var expectedtime1 : TextView
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

    lateinit var odsayService: ODsayService
    lateinit var jsonObject: JSONObject

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
        location = findViewById(R.id.locationString)
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
        }

        searchButton.setOnClickListener {
            searchKeyword(location.text.toString())
        }


        val mapView = MapView(this)
        map = findViewById(R.id.clKakaoMapView)
        map.addView(mapView)

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
                    setCarTime()
                }
                R.id.walk -> {
                    transportType = 2
                }
                else -> transportType = null
            }
        }

        when (transportType) {
            0 -> totalTime = setPublicTime()
            1 -> totalTime = 10
            2 -> totalTime = 10
            else -> totalTime = 0
        }

        val elapsedTime = totalTime

        alarmHour = startHour + (elapsedTime!! / 60)
        alarmMinute = startMinute + (elapsedTime!! % 60)

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
                    sId,
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
                    elapsedTime,
                    alarmHour,
                    alarmMinute,
                    isAlarmOn
                )
                Log.d(
                    "Addschedule",
                    "${scheduleExplain.text}, ${year}, ${month}, ${date}, ${startHour}:${startMinute},${transportType},${isAlarmOn}"
                )
                val intent = Intent()
                intent.putExtra("scheduleInfo", scheduleInfo)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, "일정 내용을 입력해주세요", Toast.LENGTH_LONG).show()
            }
        }
    }

    //키워드 검색 함
    private fun searchKeyword(keyword: String) {
        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(kakaoAPI::class.java)   // 통신 인터페이스를 객체로 생성
        val call = api.getSearchKeyword(API_KEY, keyword)   // 검색 조건 입력

        // API 서버에 요청
        call.enqueue(object: Callback<ResultSearchKeyword> {
            override fun onResponse(
                call: Call<ResultSearchKeyword>,
                response: Response<ResultSearchKeyword>
            ) {
                // 통신 성공 (검색 결과는 response.body()에 담겨있음)
                Log.d("Test", "Raw: ${response.raw()}")
                Log.d("Test", "Body: ${response.body()}")
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                // 통신 실패
                Log.w("MainActivity", "통신 실패: ${t.message}")
            }
        })
    }

    override fun setTime(hour: Int, minute: Int, startOrFinish: Int) {
        /* BottomSetScheduleFragment.kt CompleteListener 인터페이스 */
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
        findViewById<TextView>(R.id.scheduleExplain).text = scheduleInfo.scheduleExplain
        year = scheduleInfo.scheduleStartYear
        month = scheduleInfo.scheduleStartMonth
        date = scheduleInfo.scheduleStartDay
        startHour = scheduleInfo.scheduleStartHour
        startMinute = scheduleInfo.scheduleStartMinute
        finishHour = scheduleInfo.scheduleFinishHour
        finishMinute = scheduleInfo.scheduleFinishMinute

        when (scheduleInfo.transportation) {
            //0: 대중교통, 1: 자동차, 2: 도보
            0 -> {
                findViewById<RadioButton>(R.id.publicTransport).isChecked = true
            }
            1 -> {
                findViewById<RadioButton>(R.id.car).isChecked = true
            }
            2 -> {
                findViewById<RadioButton>(R.id.walk).isChecked = true
            }
        }
    }

    private fun setPublicTime(): Int {
        var totalTime: Int = -1

        odsayService = ODsayService.init(
            this,
            BuildConfig.ODsay_API_KEY
        )
        odsayService.setConnectionTimeout(5000)
        odsayService.setReadTimeout(5000)

        odsayService.requestSearchPubTransPath(
            "128.61027824041773",
            "35.88902720456651",
            "128.6017393692533",
            "35.87155237703856",
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
                }

                override fun onError(code: Int, message: String, api: API) {
                    totalTime = -1
                }
            }
        )

        return totalTime
    }

    //자동차 예상 소요 시간을 초 단위로 계산후 반환
    private fun setCarTime(): Int{
        val BASE_URL_KAKAONAVI_API = "https://apis-navi.kakaomobility.com"
        val API_KEY = "KakaoAK 28f1a9b662dea4d3296bfaa59f4590b3"

        val origin : String = "128.6112347669226,35.88546795750079" //출발지 좌표
        val destination : String = "128.61646900942918,35.88790748120179" //목적지 좌표

        val api = kakaonaviAPI.create()
        val callGetSearchCarRoute = api.getSearchCarRoute(CarRoute.API_KEY,origin,destination)

        var text:String
        var tmp:Int = 0

        callGetSearchCarRoute.enqueue(object : Callback<ResultCarRouteSearch> {
            override fun onResponse(
                call: Call<ResultCarRouteSearch>,
                response: Response<ResultCarRouteSearch>
            ) {
                Log.d("결과","성공 : ${response.raw()}")
                Log.d("결과","성공 : ${response.body()}")
                tmp = response.body()!!.routes[0].summary.duration
                text = expectedtimetoString(tmp)

                expectedtime1 = findViewById(R.id.expectedtime1)
                expectedtime1.setText(text)
            }
            override fun onFailure(call: Call<ResultCarRouteSearch>, t: Throwable) {
                Log.d("결과","실패 : ${t.message}")
            }
        })

        return tmp
    }

    //Int형의 초 단위 에상 소요 시간을 포맷에 맞춰 계산
    fun expectedtimetoString(intseconds:Int): String {
        val seconds : Long = intseconds.toLong()
        val day = TimeUnit.SECONDS.toDays(seconds).toInt()
        val hours = TimeUnit.SECONDS.toHours(seconds) - day * 24
        val minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60
        val second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60
        val time = (day.toString() + "일 " + hours + "시간 " + minute + "분 " + second + "초")

        return time
    }
}