package com.example.smartscheduler.Activity

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.smartscheduler.R
import net.daum.mf.map.api.MapView

class UserInfoActivity : AppCompatActivity() {

    lateinit var readyTimeEditText: EditText
    lateinit var sleepTimeEditText: EditText
    lateinit var saveButton: Button
    lateinit var map: ConstraintLayout
    lateinit var curloc : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userinfo)


        // 1. Shared Preference 초기화
        val userInfo: SharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = userInfo.edit()

        //2. 저장된 값 불러오기
        var readyTime = userInfo.getInt("readyTime", 0)
        var sleepTime = userInfo.getInt("sleepTime", 0)
        //출발장소 위도
        //출발장소 경도


        if(readyTime>0 && sleepTime>0){
        // 정보를 설정한 적이 있다면 activity_userinfo.xml을 보여주지 않음
            gotoMain()
            finish()
        }

        /*val mapView = MapView(this)
        map = findViewById(R.id.clKakaoMapView)
        curloc = findViewById(R.id.currentLocationButton)
        map.addView(mapView)*/

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
            editor.apply()

            gotoMain()

        }

    }
    private fun gotoMain(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}