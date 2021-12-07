package com.example.smartscheduler.Activity

import android.os.Bundle
import android.util.Log
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
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class CarRoute : AppCompatActivity() {
    lateinit var map: ConstraintLayout
    lateinit var curloc : ImageButton
    lateinit var startNaviBtn : Button
    lateinit var expectedtime : TextView
    var RouteInformation : ResultCarRouteSearch? = null

    companion object {
        const val BASE_URL_KAKAONAVI_API = "https://apis-navi.kakaomobility.com"
        const val API_KEY = "KakaoAK 28f1a9b662dea4d3296bfaa59f4590b3"
    } // 카카오

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        KakaoSdk.init(this, "5f9edbd5b9db541446f51c121146a651")
        setContentView(R.layout.activity_carroute)

        val getIntent = getIntent()
        val x = getIntent.getIntExtra("x", 0)
        val y = getIntent.getIntExtra("y", 0)

        val mapView = MapView(this)
        map = findViewById(R.id.clKakaoMapView)
        curloc = findViewById(R.id.currentLocationButton)
        map.addView(mapView)


        val origin : String = "128.6112347669226,35.88546795750079" //출발지 좌표
        val destination : String = "128.61646900942918,35.88790748120179" //목적지 좌표

        val api = kakaonaviAPI.create()
        val callGetSearchCarRoute = api.getSearchCarRoute(API_KEY,origin,destination)

        callGetSearchCarRoute.enqueue(object : Callback<ResultCarRouteSearch> {
            override fun onResponse(
                call: Call<ResultCarRouteSearch>,
                response: Response<ResultCarRouteSearch>
            ) {
                RouteInformation = response.body()
                Log.d("결과","성공 : ${response.raw()}")
                Log.d("결과","성공 : ${response.body()}")
                expectedtime = findViewById(R.id.expectedtime)
                expectedtime.setText(expectedtimetoString(RouteInformation!!.routes[0].summary.duration))
            }
            override fun onFailure(call: Call<ResultCarRouteSearch>, t: Throwable) {
                Log.d("결과","실패 : ${t.message}")
            }
        })

        //내비게이션 시작 버튼
        startNaviBtn = findViewById(R.id.startNaviBtn)
        startNaviBtn.setOnClickListener {
            //카카오내비가 설치 되어 있을 경우 앱으로 연결
            if (NaviClient.instance.isKakaoNaviInstalled(this)) {
                startActivity(
                    NaviClient.instance.navigateIntent(
                        Location("카카오 판교오피스", "127.108640", "37.402111"),
                        NaviOption(coordType = CoordType.WGS84,vehicleType = VehicleType.FIRST, rpOption = RpOption.RECOMMENDED)
                    )
                )
            }
            else {
                // 카카오내비가 설치되지 않았을 경우 웹에서 연결
                val uri = NaviClient.instance.navigateWebUrl(
                    Location("카카오 판교오피스", "127.108640", "37.402111"),
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
    fun expectedtimetoString(intseconds:Int): String {
        val seconds : Long = intseconds.toLong()
        val day = TimeUnit.SECONDS.toDays(seconds).toInt()
        val hours = TimeUnit.SECONDS.toHours(seconds) - day * 24
        val minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60
        val second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60
        val time = (day.toString() + "일" + hours + "시" + minute + "분" + second + "초")
        return time
    }
}