package com.example.smartscheduler.Activity

import android.content.Context
import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.smartscheduler.*

import com.skt.Tmap.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.net.URLEncoder
import java.io.IOException
import org.json.JSONArray
import org.json.JSONObject

class WalkRouteActivity : AppCompatActivity() {
    lateinit var totalTime: TextView
    lateinit var totalDistance: TextView

    lateinit var tmapView : TMapView
    lateinit var tMapPolyLine: TMapPolyLine

    val tmapKey : String = "l7xx6856c73aa91c41e480afc960d336d8c3"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walkroute)

        val getIntent = getIntent()
        val arriX = getIntent.getDoubleExtra("x", 0.0)
        val arriY = getIntent.getDoubleExtra("y", 0.0)

        val userInfo: SharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE)
        val depX = userInfo.getFloat("userLongitude", 0.0f)
        val depY = userInfo.getFloat("userLatitude", 0.0f)

        totalTime = findViewById<TextView>(R.id.walkTime)
        totalDistance = findViewById<TextView>(R.id.walkLength)

        // Tmap 지도 생성 및 초기화
        var linearLayoutTmap = findViewById<LinearLayout>(R.id.walkTmapView)
        tmapView = TMapView(this)
        linearLayoutTmap.addView(tmapView)

        tmapView.setSKTMapApiKey(tmapKey)

        tmapView.zoomLevel = 17
        tmapView.mapType = TMapView.MAPTYPE_STANDARD
        tmapView.setLanguage(TMapView.LANGUAGE_KOREAN)

        var walkDep = TMapPoint(depY!!.toDouble(), depX!!.toDouble())
        var walkArrival = TMapPoint(arriY, arriX)

        tmapView.setCenterPoint(depX!!.toDouble(), depY!!.toDouble()) // 지도 중심점 출발지로 변경

        // OKhttp 초기화
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

        var url = "https://apis.openapi.sk.com/tmap/routes/pedestrian?"
        val client = OkHttpClient()

        url += "startX=" + depX
        url += "&startY=" + depY
        url += "&endX=" + arriX.toString()
        url += "&endY=" + arriY.toString()
        url += "&reqCoordType=WGS84GEO"
        url += "&startName=" + URLEncoder.encode("시작", "UTF-8")
        url += "&endName=" + URLEncoder.encode("끝", "UTF-8")
        url += "&searchOption=0&resCoordType=WGS84GEO"

        // OKhttp를 이용해 Tmap Rest API 요청
        val request = Request.Builder()
            .header("Accept", "application/json")
            .addHeader("appKey", tmapKey)
            .addHeader("Content-Type", "application/json; charset=UTF-8")
            .url(url)
            .build()

        Log.d("url 확인 ", "url : $url")
        var myHandler = Handler() // Handler 생성

        val response = client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { // Rest API 호출 실패
                e.printStackTrace()
            }
            override fun onResponse(call: Call, response: Response) { // Rest API 호출 성공
                Thread {
                    val data = response.body?.string()

                    val jsonObj = JSONObject(data)
                    val featureArray = jsonObj.getJSONArray("features")
                    val firstFeature = featureArray.getJSONObject(0)
                    val property = firstFeature.getJSONObject("properties")
                    val walkTime = (property.getInt("totalTime") / 60) + 1
                    val walkDistance = property.getInt("totalDistance")

                    // Thread 내부에서 UI를 바꿔 주기 위한 Handler
                    myHandler.post {
                        totalTime.setText(walkTime.toString() + "분")
                        totalDistance.setText(walkDistance.toString() + "m")
                    }
                }.start()
            }
        })



        // Tmap 지도에 경로 Polyline 띄우기
        TMapData().findPathDataWithType(
            TMapData.TMapPathType.PEDESTRIAN_PATH, walkDep, walkArrival,
            TMapData.FindPathDataListenerCallback { polyLine ->
                polyLine.lineColor = Color.RED
                polyLine.lineWidth = 10f
                tmapView.addTMapPath(polyLine)
            })
    }
}