package com.example.smartscheduler.Activity

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import android.util.Log
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
import com.odsay.odsayandroidsdk.API
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.gson.GsonConverterFactory.*

class CarRoute : AppCompatActivity() {
    lateinit var map: ConstraintLayout
    lateinit var curloc : ImageButton
    lateinit var startNaviBtn : Button

    companion object {
        const val BASE_URL_KAKAONAVI_API = "https://apis-navi.kakaomobility.com"
        const val API_KEY = "KakaoAK 28f1a9b662dea4d3296bfaa59f4590b3"
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        KakaoSdk.init(this, "5f9edbd5b9db541446f51c121146a651")
        setContentView(R.layout.activity_carroute)


        var intent = Intent(this,gps::class.java)
        startActivity(intent)

        val mapView = MapView(this)
        map = findViewById(R.id.clKakaoMapView)
        curloc = findViewById(R.id.currentLocationButton)
        map.addView(mapView)

        val origin : String = "128.6112347669226,35.88546795750079"
        val destination : String = "128.61646900942918,35.88790748120179"

//        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL_KAKAONAVI_API)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
        val api = kakaonaviAPI.create()
        val callGetSearchCarRoute = api.getSearchCarRoute(API_KEY,origin,destination)

        callGetSearchCarRoute.enqueue(object : Callback<ResultCarRouteSearch> {
            override fun onResponse(
                call: Call<ResultCarRouteSearch>,
                response: Response<ResultCarRouteSearch>
            ) {
                Log.d("결과","성공 : ${response.raw()}")
                Log.d("결과","성공 : ${response.body()}")
            }

            override fun onFailure(call: Call<ResultCarRouteSearch>, t: Throwable) {
                Log.d("결과","실패 : ${t.message}")
            }
        })

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