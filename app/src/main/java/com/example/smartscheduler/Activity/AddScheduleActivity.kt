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
        const val API_KEY = "KakaoAK 28f1a9b662dea4d3296bfaa59f4590b3"  // REST API ???
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
            //?????? ??????
            val info = getIntent.getSerializableExtra("beforeModify") as ScheduleInfo
            sId = info.sId
            setInfo(info)
        } else {
            //?????? ??????
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
                        setCarTime()        //????????? ????????? ????????????????????? ????????????
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
                // ???????????? ????????? ?????????
                // 1. ?????? ????????? ??????????????? ????????? ?????? ?????? ??????
                if (isAlarmOn) {
                    calculateAlarmClock(totalTime!!)
                }
                // 2. ??????????????? ???????????? ????????? scheduleInfo??? MainActivity??? ??????
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
                        null, //?????? ??????(int???)
                        null, //?????? ??????(int???)
                        destLongitude, //?????? ??????(double)
                        destLatitude, //?????? ??????(double)
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
                    // 3. AddScheduleActivity ??????
                    finish()
                } else {
                    Toast.makeText(this, "?????? ????????? ??????????????????", Toast.LENGTH_LONG).show()
                }
            }
            catch (e: Exception) {
                Toast.makeText(this, "?????? ????????? ??????????????????", Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun setTime(hour: Int, minute: Int, startOrFinish: Int) {
        /* BottomSetScheduleFragment.kt CompleteListener ??????????????? */
        if (startOrFinish == 0) { //?????? ?????? ??????
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
        } else if (startOrFinish == 1) { //?????? ?????? ??????
            finishHour = hour
            finishMinute = minute
            finishTimeTextView.text = showTimeTextView(month, date, finishHour, finishMinute)
        }
    }

    private fun scheduleTime() {
        /* ?????? ?????? */

        startTimeTextView.text = showTimeTextView(month, date, startHour, startMinute)
        finishTimeTextView.text = showTimeTextView(month, date, finishHour, finishMinute)

        /* activity -> fragment ????????? ?????? */
        val bottomSetSchedule = BottomSetScheduleFragment()
        val bundle = Bundle()
        bundle.putInt("year", year)
        bundle.putInt("month", month)
        bundle.putInt("date", date)
        startTimeTextView.setOnClickListener {  //?????? ?????? ??????
            bundle.putInt("startOrFinish", 0)
            bottomSetSchedule.arguments = bundle //fragment??? arguments??? ???????????? ?????? bundle??? ?????????
            bottomSetSchedule.show(supportFragmentManager, bottomSetSchedule.tag)
        }
        finishTimeTextView.setOnClickListener {  //?????? ?????? ??????
            bundle.putInt("startOrFinish", 1)
            bottomSetSchedule.arguments = bundle //fragment??? arguments??? ???????????? ?????? bundle??? ?????????
            bottomSetSchedule.show(supportFragmentManager, bottomSetSchedule.tag)
        }
    }

    private fun showTimeTextView(month: Int, date: Int, hour: Int, minute: Int): String {
        return "${month}??? ${date}??? ${hour}??? ${minute}???"
    }

    private fun setInfo(scheduleInfo: ScheduleInfo) {
        // ?????? ????????? ??? ?????? ?????? ????????????
        findViewById<TextView>(R.id.scheduleExplain).text = scheduleInfo.scheduleExplain
        year = scheduleInfo.scheduleStartYear
        month = scheduleInfo.scheduleStartMonth
        date = scheduleInfo.scheduleStartDay
        startHour = scheduleInfo.scheduleStartHour
        startMinute = scheduleInfo.scheduleStartMinute
        finishHour = scheduleInfo.scheduleFinishHour
        finishMinute = scheduleInfo.scheduleFinishMinute
        when(scheduleInfo.transportation){
            //?????????  0: ????????????, 1: ?????????, 2: ??????
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
        // ??????
        if(scheduleInfo.schedulePlace_name != null) {
            destName = scheduleInfo.schedulePlace_name
            findViewById<TextView>(R.id.locationText).text = destName //?????? ??????
            destLongitude = scheduleInfo.schedulePlace_x_double // ??????
            destLatitude = scheduleInfo.schedulePlace_y_double  // ??????
        }
        // ????????????
        if(scheduleInfo.elapsedTime != null){
            totalTime = scheduleInfo.elapsedTime //??????: ???
            findViewById<TextView>(R.id.expectedtimeTextView).text = expectedtimetoString(totalTime!! * 60)
        }
        // ?????? on/off
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
                    expectedtime1.setText(totalTime.toString() + "???")
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
        var startName: String = URLEncoder.encode("??????", "UTF-8")
        var endName: String = URLEncoder.encode("??????", "UTF-8")
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
                Log.d("??????", "?????? : ${response.raw()}")
                Log.d("??????", "?????? : ${response.body()}")
                tmp = response.body()!!.features[0].properties.totalTime

                totalTime = (tmp / 60) + 1

                myHandler.post {
                    expectedtime1 = findViewById(R.id.expectedtimeTextView)
                    expectedtime1.setText(totalTime.toString() + "???")
                }
            }

            override fun onFailure(call: Call<ResultWalkRouteSearch>, t: Throwable) {
                Log.d("??????", "?????? : ${t.message}")
            }
        })
    }

    //????????? ?????? ?????? ????????? ?????? ???, ??? ????????? totalTime??? ??????, ??????
    private fun setCarTime(){

        val userInfo: SharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE)
        val userLongitude = userInfo.getFloat("userLongitude",0.0f)         //????????? ??????
        val userLatitude = userInfo.getFloat("userLatitude",0.0f)           //????????? ??????

        var origin = (userLongitude.toString() + "," + userLatitude.toString())  //????????? ??????
        var dest = (destLongitude.toString() + "," + destLatitude.toString()) //????????? ??????

        expectedtimeTextView = findViewById(R.id.expectedtimeTextView)
        if(origin.equals("0.0,0.0") && dest.equals("0.0,0.0")){
            expectedtimeTextView.setText("???????????? ???????????? ???????????? ???????????????.")
            return
        }
        if(origin.equals("0.0,0.0")){
            expectedtimeTextView.setText("???????????? ???????????? ???????????????.")
            return
        }
        if(dest.equals("0.0,0.0")){
            expectedtimeTextView.setText("???????????? ???????????? ???????????????.")
            return
        }

        val api = kakaonaviAPI.create()
        val callGetSearchCarRoute = api.getSearchCarRoute(API_KEY,origin,dest)

        callGetSearchCarRoute.enqueue(object : Callback<ResultCarRouteSearch> {
            override fun onResponse(
                call: Call<ResultCarRouteSearch>,
                response: Response<ResultCarRouteSearch>
            ) {
                Log.d("??????","?????? : ${response.raw()}")
                Log.d("??????","?????? : ${response.body()}")
                totalTime = response.body()!!.routes[0].summary.duration / 60

                expectedtimeTextView.setText(expectedtimetoString(response.body()!!.routes[0].summary.duration))
                //duration(??????????????????)??? ???????????? ????????????????????? ????????? ????????? ???????????? ?????? ??? ??????????????? ??????
            }
            override fun onFailure(call: Call<ResultCarRouteSearch>, t: Throwable) {
                Log.d("??????","?????? : ${t.message}")
            }
        })
    }

    //Int?????? ??? ?????? ?????? ?????? ????????? ????????? ?????? ??????
    fun expectedtimetoString(intseconds: Int): String {
        val seconds: Long = intseconds.toLong()
        val day = TimeUnit.SECONDS.toDays(seconds).toInt()
        val hours = TimeUnit.SECONDS.toHours(seconds) - day * 24
        val minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60
        val second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60
        val time = (day.toString() + "??? " + hours + "?????? " + minute + "??? " + second + "???")

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
            locationText.setText("?????? : " + destName)
            Log.d(
                "newdestination : ",
                "?????? ?????? name : $destName \n address : $destAddress \n road : $destRoad \n lat : $destLatitude \n long : $destLongitude"
            )
        }
    }

    private fun calculateAlarmClock(elapsedTime: Int) {
        //?????? ??????(?????? ????????? ?????? ?????? ??????)??? ??????: ?????? ?????? ?????? - ?????? ?????? ?????? - ?????? ?????? ??????
        //elapsedTime ; ?????? ~ ????????? ????????????(?????? : ???)
        Log.d("????????????", "${elapsedTime}???")
        // ????????? ?????? ????????????
        val userInfo: SharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE)
        val readyTime = userInfo.getInt("readyTime", 0) //??????????????????(??????: ??????)
        val sleepTime = userInfo.getInt("sleepTime", 0) //????????????(??????: ??????)

        // ??????????????? ????????? (???)?????? (???+???)?????? ??????
        val elapsedTime_hour = elapsedTime / 60
        val elapsedTime_minute = elapsedTime % 60

        // ?????? ?????? ??????
        alarmMinute = startMinute - elapsedTime_minute
        alarmHour = startHour - elapsedTime_hour - readyTime
        if (alarmMinute < 0) {
            alarmMinute += 60
            alarmHour -= 1
        }

        // ???????????? ????????? ????????? ?????? - ??????????????? ????????? ?????? ????????? ?????? ?????? ????????? ???
        // ??????: 12??? 3?????? ???????????? ?????? ????????? 7?????? ????????? ??????, ?????? ????????? 8????????? ???, 12??? 2??? 23?????? ?????? ????????? ?????????
        if (alarmHour - sleepTime < 5) {
            // ?????? ?????? ?????? ??????
            sleepAlarmHour = alarmHour - sleepTime
            sleepAlarmMinute = alarmMinute
            Log.d("?????? ??????", "${sleepAlarmHour}:${sleepAlarmMinute}")
            isSleepAlarmOn = true
        }
    }
}