package com.example.smartscheduler.Activity

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.smartscheduler.*

import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONObject;

class PublicRouteActivity : AppCompatActivity() {

    lateinit var depPlace: TextView
    lateinit var arrivalPlace: TextView
    lateinit var totalTime: TextView

    lateinit var odsayService: ODsayService
    lateinit var jsonObject: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicroute)

        depPlace = findViewById<TextView>(R.id.publicDepPlace)
        arrivalPlace = findViewById<TextView>(R.id.publicArrivalPlace)
        totalTime = findViewById<TextView>(R.id.publicTime)

        depPlace.setText("경북대학교")
        arrivalPlace.setText("대구광역시청")

        odsayService = ODsayService.init(
            this@PublicRouteActivity,
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

                    totalTime.setText(pathInfo.getInt("totalTime").toString() + "분")
                }

                override fun onError(code: Int, message: String, api: API) {
                    totalTime.setText(api.name + " 호출 실패")
                }
            }
        )
    }
}

