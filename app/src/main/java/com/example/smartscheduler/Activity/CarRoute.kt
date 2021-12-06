package com.example.smartscheduler.Activity

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.smartscheduler.R
import com.kakao.sdk.navi.NaviClient
import com.kakao.sdk.navi.model.*
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.navi.model.Location
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView


class CarRoute : AppCompatActivity() {
    lateinit var map: ConstraintLayout
    lateinit var startNaviBtn : Button
    lateinit var destBtn : ImageButton

    companion object {
        const val BASE_URL_KAKAONAVI_API = "https://apis-navi.kakaomobility.com"
        const val API_KEY = "KakaoAK 28f1a9b662dea4d3296bfaa59f4590b3"
    } // 카카오

    var destx : Double = 0.0
    var desty : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        KakaoSdk.init(this, "5f9edbd5b9db541446f51c121146a651")
        setContentView(R.layout.activity_carroute)

        val getIntent = getIntent()
        val destx = getIntent.getDoubleExtra("x",0.0)
        val desty = getIntent.getDoubleExtra("y",0.0)
        //val destname = getIntent.getStringExtra("destname")
        val destname = "약속장소"
        Log.d("x좌표","$destx")
        Log.d("y좌표","$desty")
        Log.d("장소 이름","$destname")

        val mapView = MapView(this)
        map = findViewById(R.id.clKakaoMapView)
        map.addView(mapView)

        setDaumMapDestLocation(desty,destx,mapView!!)


        destBtn = findViewById(R.id.DestinationButton)
        destBtn.setOnClickListener{
            setDaumMapDestLocation(desty,destx,mapView!!)
        }


        //내비게이션 시작 버튼
        startNaviBtn = findViewById(R.id.startNaviBtn)
        startNaviBtn.setOnClickListener {
            //카카오내비가 설치 되어 있을 경우 앱으로 연결
            if (NaviClient.instance.isKakaoNaviInstalled(this)) {
                startActivity(
                    NaviClient.instance.navigateIntent(
                        Location(name=destname!!,x=destx!!.toString(),y=desty!!.toString()),
                        NaviOption(coordType = CoordType.WGS84,vehicleType = VehicleType.FIRST, rpOption = RpOption.RECOMMENDED)
                    )
                )
            }
            else {
                // 카카오내비가 설치되지 않았을 경우 웹에서 연결
                val uri = NaviClient.instance.navigateWebUrl(
                    Location(name=destname!!,x=destx!!.toString(),y=desty!!.toString()),
                    NaviOption(coordType = CoordType.WGS84),null
                )

                // CustomTabs로 길안내
                KakaoCustomTabsClient.openWithDefault(this, uri)

                // 또는 외부 브라우저
                //startActivity(Intent(ACTION_VIEW, uri))

                //웹뷰로 앱 내에서 내비 실행 -> 웹뷰 스펙 문제로 불가능
//                var intent = Intent(this, CarNavigation::class.java)
//                intent.putExtra("uri",uri.toString())
//                startActivity(intent)
            }
        }
    }

    fun setDaumMapDestLocation(latitude: Double, longitude: Double, mapView: MapView) {

        // 중심점 변경
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), true)

        // 줌 레벨 변경
        mapView.setZoomLevel(1, true)

        // 줌 인
        mapView.zoomIn(true)

        // 마커 생성
        setDaumMapDestMarker(mapView)
    }

    fun setDaumMapDestMarker(mapView: MapView) {
        val marker = MapPOIItem()
        marker.itemName = "약속장소"
        marker.tag = 0
        marker.mapPoint = MapPoint.mapPointWithGeoCoord(desty,destx)
        marker.markerType = MapPOIItem.MarkerType.BluePin // 기본으로 제공하는 BluePin 마커 모양.
        marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        mapView.addPOIItem(marker)
    }
}