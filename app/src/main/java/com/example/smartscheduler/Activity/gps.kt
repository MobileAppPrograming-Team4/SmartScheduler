package com.example.smartscheduler.Activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartscheduler.R
import kotlinx.android.synthetic.main.activity_main.*



class gps : AppCompatActivity() {
    lateinit var button:Button
    lateinit var textView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps)
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        button = findViewById(R.id.button)
        textView = findViewById(R.id.TV_Result)
        button?.setOnClickListener {
            val isGPSEnabled: Boolean = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled: Boolean = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            //매니페스트에 권한이 추가되어 있다해도 여기서 다시 한번 확인해야함
            if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@gps, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            } else {
                when { //프로바이더 제공자 활성화 여부 체크
                    isNetworkEnabled -> {
                        val location =
                            lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) //인터넷기반으로 위치를 찾음
                        var getLongitude = location?.longitude!!
                        var getLatitude = location.latitude
                        textView.setText(getLatitude.toString()+","+getLongitude.toString())
                    }
                    isGPSEnabled -> {
                        val location =
                            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) //GPS 기반으로 위치를 찾음
                        var getLongitude = location?.longitude!!
                        var getLatitude = location.latitude
                        textView.setText(getLatitude.toString()+","+getLongitude.toString())
                    }
                    else -> {

                    }
                }
                //몇초 간격과 몇미터를 이동했을시에 호출되는 부분 - 주기적으로 위치 업데이트를 하고 싶다면 사용
                // ****주기적 업데이트를 사용하다가 사용안할시에는 반드시 해제 필요****
                /*lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        1000, //몇초
                        1F,   //몇미터
                        gpsLocationListener)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        1000,
                        1F,
                        gpsLocationListener)
                //해제부분. 상황에 맞게 잘 구현하자
                lm.removeUpdates(gpsLocationListener)*/
            }
        }
        lm.removeUpdates(gpsLocationListener)
    }

    //위에 *몇초 간격과 몇미터를 이동했을시에 호출되는 부분* 에 필요한 정보
    //주기적으로 위치 업데이트 안할거면 사용하지 않음
    val gpsLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val provider: String = location.provider
            val longitude: Double = location.longitude
            val latitude: Double = location.latitude
            val altitude: Double = location.altitude
        }

        //아래 3개함수는 형식상 필수부분
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
}