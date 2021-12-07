package com.example.smartscheduler.Activity


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.smartscheduler.R
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import android.os.Build
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_addschedule.*
import net.daum.mf.map.api.MapPOIItem
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class UserInfoActivity : AppCompatActivity() {

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var readyTimeEditText: EditText
    lateinit var sleepTimeEditText: EditText
    lateinit var searchButton: Button
    lateinit var saveButton: Button
    lateinit var startLocation: TextView
    var startpointName: String? = null
    var startpointAddress: String? = null
    var startpointRoad: String? = null
    var startpointLatitude: Double = 0.0
    var startpointLongitude: Double = 0.0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userinfo)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted
        } else {
            // Permission is not granted
        }

        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }

        // 1. Shared Preference 초기화
        val userInfo: SharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = userInfo.edit()

        //2. 저장된 값 불러오기
        var readyTime = userInfo.getInt("readyTime", 0)
        var sleepTime = userInfo.getInt("sleepTime", 0)

        if (readyTime > 0 && sleepTime > 0) {
            // 정보를 설정한 적이 있다면 activity_userinfo.xml을 보여주지 않음
            gotoMain()
        }

        searchButton = findViewById(R.id.searchBtn)
        startLocation = findViewById(R.id.startLocation)
        searchButton.setOnClickListener {
            var intent = Intent(applicationContext, StartpointSearchActivity::class.java)
            startActivityForResult(intent, 0)
            //3. 레이아웃 변수 초기화
            readyTimeEditText = findViewById<EditText>(R.id.scheduleExplain)
            sleepTimeEditText = findViewById<EditText>(R.id.sleepTime)
            //출발장소 위도
            //출발장소 경도

            //4. 앱 실행 시 저장해둔 값 표시
            readyTimeEditText.setText(readyTime.toString())
            sleepTimeEditText.setText(sleepTime.toString())

            //5. 저장 버튼 클릭 시 새로운 값 저장
            saveButton = findViewById(R.id.saveButton)
            saveButton.setOnClickListener {

                readyTime = Integer.parseInt(readyTimeEditText.text.toString())
                sleepTime = Integer.parseInt(sleepTimeEditText.text.toString())
                editor.putInt("readyTime", readyTime)
                editor.putInt("sleepTime", sleepTime)
                editor.putFloat("userLongitude", startpointLongitude.toFloat())
                editor.putFloat("userLatitude", startpointLatitude.toFloat())
                editor.apply()

                Log.d(
                    "item 선택 : ",
                    "name : $startpointName \n address : $startpointAddress \n road : $startpointRoad \n lat : $startpointLatitude \n long : $startpointLongitude"
                )
                if(startpointLatitude==0.0 && startpointLongitude==0.0){
                    Toast.makeText(this, "앱 사용을 위해 기본 출발 장소를 설정해주세요", Toast.LENGTH_LONG).show()
                }
                else{
                    gotoMain()
                }

            }

        }
    }

    private fun gotoMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            startpointName = data!!.getStringExtra("startpointName")
            startpointAddress = data!!.getStringExtra("startpointAddress")
            startpointRoad = data!!.getStringExtra("startpointRoad")
            startpointLongitude = data!!.getDoubleExtra("startpointLongitude", 0.0)
            startpointLatitude = data!!.getDoubleExtra("startpointLatitude", 0.0)
            Log.d(
                "newdestination : ",
                "받은 로그 name : $startpointName \n address : $startpointAddress \n road : $startpointRoad \n lat : $startpointLatitude \n long : $startpointLongitude"
            )
            startLocation.setText("주소 : " + startpointName)
        }
    }
}