package com.example.smartscheduler.Activity


import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.smartscheduler.R
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import android.widget.TextView

import android.location.LocationManager
import android.media.audiofx.BassBoost
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.*
import net.daum.mf.map.api.MapPOIItem





class UserInfoActivity : AppCompatActivity(), MapView.CurrentLocationEventListener, MapView.MapViewEventListener {

    lateinit var readyTimeEditText: EditText
    lateinit var sleepTimeEditText: EditText
    lateinit var saveButton: Button
    lateinit var map: ConstraintLayout
    lateinit var curloc : ImageButton
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private val PERMISSIONS_REQUEST_ACCESS_CALL_PHONE = 2
    // 중요...


    // 위치 정보
    var txtCurrentPositionInfo: TextView? = null

    // 위치 버튼
    var btnCurrentPositionInfo: TextView? = null
    var longitude = 1.0
    var latitude = 0.0
    var altitude //고도
            = 0.0
    var accuracy //정확도
            = 0f
    var provider //위치제공자
            : String? = null
    var currentLocation // 그래서 최종 위치
            : String? = null
    val mapView = MapView(this)
    companion object {
        const val PERMISSION_REQUEST_CODE = 1001
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userinfo)


        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted
        } else {
            // Permission is not granted
        }

        var lm: LocationManager? = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        var gpsListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude
                Log.d(
                    "Test", "GPS Location changed, Latitude: $latitude" +
                            ", Longitude: $longitude"
                )
            }
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            }
            override fun onProviderEnabled(provider: String) {
            }
            override fun onProviderDisabled(provider: String) {
            }
        }

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


        map = findViewById(R.id.clKakaoMapView)
        curloc = findViewById(R.id.currentLocationButton)
        map.addView(mapView)





        // 현재위치 클릭
       curloc.setOnClickListener {
           val isGPSEnabled = lm?.isProviderEnabled(LocationManager.GPS_PROVIDER)
           val isNetworkEnabled = lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
           mapView.setCurrentLocationEventListener(this)
//           mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
           lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, gpsListener)
           lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, gpsListener)
           Log.d(
               "Test", "GPS Location changed, Latitude: $latitude" +
                       ", Longitude: $longitude" + ", isGPSEnabled: $isGPSEnabled" + ", isNetworkEnabled: $isNetworkEnabled"
           )
           setDaumMapCurrentLocation(latitude, longitude)




        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {  // 1
                if (grantResults.isEmpty()) {  // 2
                    throw RuntimeException("Empty permission result")
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {  // 3
                    showDialog("Permission granted")
                } else {
                    if (shouldShowRequestPermissionRationale(
                            Manifest.permission.ACCESS_FINE_LOCATION)) { // 4
                        Log.d(TAG, "User declined, but i can still ask for more")
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            PERMISSION_REQUEST_CODE)
                    } else {
                        Log.d(TAG, "User declined and i can't ask")
                        showDialogToGetPermission()   // 5
                    }
                }
            }
        }
    }

    private fun showDialogToGetPermission() {
        val builder = Builder(this)
        builder.setTitle("Permisisons request")
            .setMessage("We need the location permission for some reason. " +
                    "You need to move on Settings to grant some permissions")

        builder.setPositiveButton("OK") { dialogInterface, i ->
            val intent = Intent(
                BassBoost.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)   // 6
        }
        builder.setNegativeButton("Later") { dialogInterface, i ->
            // ignore
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun setDaumMapCurrentLocation(latitude: Double, longitude: Double) {

        // 중심점 변경
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), true)

        // 줌 레벨 변경
        mapView.setZoomLevel(4, true)

        // 중심점 변경 + 줌 레벨 변경
        //mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(latitude, longitude), 9, true);

        // 줌 인
        mapView.zoomIn(true)

        // 줌 아웃
        //mapView.zoomOut(true);

        // 마커 생성
        setDaumMapCurrentMarker()
    }

    fun setDaumMapCurrentMarker() {
        val marker = MapPOIItem()
        marker.itemName = "현재 위치"
        marker.tag = 0
        marker.mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
        marker.markerType = MapPOIItem.MarkerType.BluePin // 기본으로 제공하는 BluePin 마커 모양.
        marker.selectedMarkerType =
            MapPOIItem.MarkerType.RedPin // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        mapView.addPOIItem(marker)
    }

    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {

    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
        TODO("Not yet implemented")
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
        TODO("Not yet implemented")
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
        TODO("Not yet implemented")
    }

    override fun onMapViewInitialized(p0: MapView?) {
        TODO("Not yet implemented")
    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {
        TODO("Not yet implemented")
    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
        TODO("Not yet implemented")
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
        TODO("Not yet implemented")
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
        TODO("Not yet implemented")
    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
        TODO("Not yet implemented")
    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
        TODO("Not yet implemented")
    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
        TODO("Not yet implemented")
    }


}

private fun LocationManager?.requestLocationUpdates(gpsProvider: String, i: Int, i1: Int, gpsListener: LocationListener) {

}
