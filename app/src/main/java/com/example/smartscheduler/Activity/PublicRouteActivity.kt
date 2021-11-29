package com.example.smartscheduler.Activity

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.smartscheduler.*

import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONObject;
import net.daum.mf.map.api.MapView

class PublicRouteActivity : AppCompatActivity() {

    lateinit var depPlace: TextView
    lateinit var arrivalPlace: TextView
    lateinit var totalTime: TextView

    lateinit var odsayService: ODsayService
    lateinit var jsonObject_path: JSONObject
    lateinit var jsonObject_lane: JSONObject

    lateinit var map: ConstraintLayout
    val mapView = MapView(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicroute)

        depPlace = findViewById<TextView>(R.id.publicDepPlace)
        arrivalPlace = findViewById<TextView>(R.id.publicArrivalPlace)
        totalTime = findViewById<TextView>(R.id.publicTime)

        map = findViewById(R.id.publicRouteKakaoMapView)
        map.addView(mapView)

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
                    jsonObject_path = odsayData!!.json

                    val routeResult = jsonObject_path.getJSONObject("result")
                    val pathArray = routeResult.getJSONArray("path")
                    val firstPath = pathArray.getJSONObject(0)
                    val pathInfo = firstPath.getJSONObject("info")

                    totalTime.setText(pathInfo.getInt("totalTime").toString() + "분")

                    val mapObject = pathInfo.getString("mabObj")

                    publicRouteView(mapObject)
                }

                override fun onError(code: Int, message: String, api: API) {
                    totalTime.setText(api.name + " 호출 실패")
                }
            }
        )
    }

    private fun publicRouteView(mabObject: String) {
        odsayService.requestLoadLane(
            mabObject,
            object : OnResultCallbackListener {
                override fun onSuccess(odsayData: ODsayData?, api: API?) {
                    jsonObject_lane = odsayData!!.json

                    val laneResult = jsonObject_lane.getJSONObject("result")
                    val laneArray = laneResult.getJSONArray("lane")
                    val firstSection = laneArray.getJSONObject(0)
                    val graphArray = firstSection.getJSONArray("graphPos")

                    val polyline = MapPolyline()
                    polyline.setTag(1000)
                    polyline.setLineColor(Color.argb(128, 255, 51, 0))

                    for (i in 0 until graphArray.length()) {
                        val coordinate = graphArray.getJSONObject(i)

                        polyline.addPoint(MapPoint.mapPointWithGeoCoord(coordinate.getDouble("y"), coordinate.getDouble("x")));
                    }

                    mapView.addPolyline(polyline);

                    val mapPointBounds = MapPointBounds(polyline.getMapPoints())
                    val padding = 100
                    mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
                }

                override fun onError(code: Int, message: String, api: API) {
                    totalTime.setText(api.name + " 호출 실패")
                }
            }
        )
    }
}

