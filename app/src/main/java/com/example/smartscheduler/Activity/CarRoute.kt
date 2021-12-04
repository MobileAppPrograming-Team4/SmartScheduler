package com.example.smartscheduler.Activity

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.smartscheduler.R
import com.kakao.sdk.navi.NaviClient
import com.kakao.sdk.navi.model.*
import com.kakao.sdk.*
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.navi.model.Location
import net.daum.mf.map.api.MapView

class CarRoute : AppCompatActivity() {
    lateinit var map: ConstraintLayout
    lateinit var curloc : ImageButton
    lateinit var startNaviBtn : Button

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        KakaoSdk.init(this, "5f9edbd5b9db541446f51c121146a651")
        setContentView(R.layout.activity_carroute)

        val mapView = MapView(this)
        map = findViewById(R.id.clKakaoMapView)
        curloc = findViewById(R.id.currentLocationButton)
        map.addView(mapView)

        startNaviBtn = findViewById(R.id.startNaviBtn)
        startNaviBtn.setOnClickListener {
            if (NaviClient.instance.isKakaoNaviInstalled(this)) {
                startActivity(
                    NaviClient.instance.navigateIntent(
                        Location("카카오 판교오피스", "127.108640", "37.402111"),
                        NaviOption(coordType = CoordType.WGS84,vehicleType = VehicleType.FIRST, rpOption = RpOption.RECOMMENDED)
                    )
                )
            }
            else {
                // 웹 브라우저에서 길안내
                // 카카오내비가 설치되지 않은 곳에서 활용할 수 있습니다.
                val uri = NaviClient.instance.navigateWebUrl(
                    Location("카카오 판교오피스", "127.108640", "37.402111"),
                    NaviOption(coordType = CoordType.WGS84),null
                )

                // CustomTabs로 길안내
                KakaoCustomTabsClient.openWithDefault(this, uri)

                // 또는 외부 브라우저
                //startActivity(Intent(ACTION_VIEW, uri))

                //웹뷰로 앱 내에서 내비 실행
//                var intent = Intent(this, CarNavigation::class.java)
//                intent.putExtra("uri",uri.toString())
//                startActivity(intent)
            }
        }
    }
}