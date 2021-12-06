package com.example.smartscheduler.Activity

import android.annotation.SuppressLint
import android.app.Activity
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
import kotlinx.android.synthetic.main.activity_addschedule.*
import net.daum.mf.map.api.MapView

import org.json.JSONObject;
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Intent as Intent

class AddScheduleActivity : AppCompatActivity(), BottomSetScheduleFragment.CompleteListener {
    lateinit var startTimeTextView: TextView
    lateinit var finishTimeTextView: TextView
    lateinit var destination: TextView
    lateinit var cal: Calendar
    lateinit var transportGroup: RadioGroup
    lateinit var searchButton: ImageButton
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
    var destLatitude: Double? = 0.0
    var destLongitude: Double? = 0.0

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

//        searchButton.setOnClickListener {
//            searchKeyword(location.text.toString())
//            val intent = Intent(this, DestinationSearchActivity::class.java)
//            intent.putExtra("destination", placeList)
//        }

        searchButton.setOnClickListener {
            val intent = Intent(this, DestinationSearchActivity::class.java)
            startActivityForResult(intent, 0)


        }


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
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, "일정 내용을 입력해주세요", Toast.LENGTH_LONG).show()
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

    private fun setNewDestination() {

        var intent = intent
        destName = intent.getSerializableExtra("destName").toString()
        destAddress = intent.getSerializableExtra("destAddress").toString()
        destRoad = intent.getSerializableExtra("destRoad").toString()
        destLatitude = intent.getDoubleExtra("destLatitude", 0.0)
        destLongitude = intent.getDoubleExtra("destLongitude", 0.0)

        destination = findViewById(R.id.locationText)
        locationText.setText("위치 : " + destName)

        Log.d("newdestination : ", "name : $destName \n address : $destAddress \n road : $destRoad \n lat : $destLatitude \n long : $destLongitude" )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            destName = data!!.getStringExtra("destName")
            destAddress = data!!.getStringExtra("destAddress")
            destRoad = data!!.getStringExtra("destRoad")
            destLatitude = data!!.getDoubleExtra("destLatitude", 0.0)
            destLongitude = data!!.getDoubleExtra("destLongitude", 0.0)
            locationText.setText("위치 : " + destName)
            Log.d("newdestination : ", "받은 로그 name : $destName \n address : $destAddress \n road : $destRoad \n lat : $destLatitude \n long : $destLongitude" )

        }
    }
}

