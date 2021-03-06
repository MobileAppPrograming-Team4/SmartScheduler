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

        // 1. Shared Preference ?????????
        val userInfo: SharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = userInfo.edit()

        //2. ????????? ??? ????????????
        var readyTime = userInfo.getInt("readyTime", 0)
        var sleepTime = userInfo.getInt("sleepTime", 0)

        if (readyTime > 0 && sleepTime > 0) {
            // ????????? ????????? ?????? ????????? activity_userinfo.xml??? ???????????? ??????
            gotoMain()
        }

        searchButton = findViewById(R.id.searchBtn)
        startLocation = findViewById(R.id.startLocation)
        searchButton.setOnClickListener {
            var intent = Intent(applicationContext, StartpointSearchActivity::class.java)
            startActivityForResult(intent, 0)
            //3. ???????????? ?????? ?????????
            readyTimeEditText = findViewById<EditText>(R.id.scheduleExplain)
            sleepTimeEditText = findViewById<EditText>(R.id.sleepTime)
            //???????????? ??????
            //???????????? ??????

            //4. ??? ?????? ??? ???????????? ??? ??????
            readyTimeEditText.setText(readyTime.toString())
            sleepTimeEditText.setText(sleepTime.toString())

            //5. ?????? ?????? ?????? ??? ????????? ??? ??????
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
                    "item ?????? : ",
                    "name : $startpointName \n address : $startpointAddress \n road : $startpointRoad \n lat : $startpointLatitude \n long : $startpointLongitude"
                )
                if(startpointLatitude==0.0 && startpointLongitude==0.0){
                    Toast.makeText(this, "??? ????????? ?????? ?????? ?????? ????????? ??????????????????", Toast.LENGTH_LONG).show()
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
                "?????? ?????? name : $startpointName \n address : $startpointAddress \n road : $startpointRoad \n lat : $startpointLatitude \n long : $startpointLongitude"
            )
            startLocation.setText("?????? : " + startpointName)
        }
    }
}