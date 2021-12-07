package com.example.smartscheduler.Activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartscheduler.R
import com.example.smartscheduler.ResultSearchKeywordAdapter
import com.example.smartscheduler.StartpointSearchAdapter
import com.example.smartscheduler.xyStartpointSearchAdapter
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class StartpointSearchActivity : AppCompatActivity(), MapView.CurrentLocationEventListener {

    private var keyword = ""
    private var pageNumber = 1
    lateinit var map: ConstraintLayout
    lateinit var location: EditText
    lateinit var searchButton: Button
    lateinit var prevButton: Button
    lateinit var nextButton: Button
    lateinit var confirmButton: Button
    lateinit var curbtn: ImageButton
    lateinit var pageNum: TextView
    private val listItems = arrayListOf<StartpointListLayout>()   // 리사이클러 뷰 아이템
    private val listAdapter = StartpointSearchAdapter(listItems)    // 리사이클러 뷰 어댑터
    private val xyListItems = arrayListOf<xyStartpointListLayout>()
    private val xyListAdapter = xyStartpointSearchAdapter(xyListItems)

    var addressName: String? = null
    var placeList: ResultSearchKeyword? = null
    var startpointName: String? = null
    var startpointAddress: String? = null
    var startpointRoad: String? = null
    var startpointLatitude: Double = 0.0
    var startpointLongitude: Double = 0.0

    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 28f1a9b662dea4d3296bfaa59f4590b3"  // REST API 키
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startpointsearch)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_list)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = listAdapter
        location = findViewById(R.id.et_search_field)
        searchButton = findViewById(R.id.btn_search)
        prevButton = findViewById(R.id.btn_prevPage)
        nextButton = findViewById(R.id.btn_nextPage)
        confirmButton = findViewById(R.id.btn_select)
        curbtn = findViewById(R.id.curbtn)
        pageNum = findViewById(R.id.tv_pageNumber)
        var lm: LocationManager? = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val mapView = MapView(this)
        map = findViewById(R.id.clKakaoMapView)
        map.addView(mapView)
        // 리스트 아이템 클릭 시 해당 위치로 이동

        listAdapter.setItemClickListener(object : StartpointSearchAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val mapPoint =
                    MapPoint.mapPointWithGeoCoord(listItems[position].y, listItems[position].x)
                mapView.setMapCenterPointAndZoomLevel(mapPoint, 1, true)
                startpointName = listItems[position].name
                startpointAddress = listItems[position].address
                startpointRoad = listItems[position].road
                startpointLatitude = listItems[position].x
                startpointLongitude = listItems[position].y

                Log.d(
                    "item 선택 : ",
                    "name : $startpointName \n address : $startpointAddress \n road : $startpointRoad \n lat : $startpointLatitude \n long : $startpointLongitude"
                )
            }
        })

        xyListAdapter.setItemClickListener(object : xyStartpointSearchAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val mapPoint =
                    MapPoint.mapPointWithGeoCoord(xyListItems[position].y, xyListItems[position].x)
                mapView.setMapCenterPointAndZoomLevel(mapPoint, 1, true)
                startpointName = xyListItems[position].name
                startpointAddress = xyListItems[position].address
                startpointRoad = xyListItems[position].road
                startpointLatitude = xyListItems[position].x
                startpointLongitude = xyListItems[position].y
            }
        })

        // 검색 버튼
        searchButton.setOnClickListener {
            recyclerView.adapter = listAdapter
            keyword = location.text.toString()
            pageNumber = 1
            searchKeyword(keyword, pageNumber, mapView)
        }

        // 이전 페이지 버튼
        prevButton.setOnClickListener {
            pageNumber--
            pageNum.text = pageNumber.toString()
            searchKeyword(keyword, pageNumber, mapView)
        }

        // 다음 페이지 버튼
        nextButton.setOnClickListener {
            pageNumber++
            pageNum.text = pageNumber.toString()
            searchKeyword(keyword, pageNumber, mapView)
        }

        //현재 위치 조회 버튼
        curbtn.setOnClickListener {
            val isGPSEnabled = lm?.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            mapView.setCurrentLocationEventListener(this)

            recyclerView.adapter = xyListAdapter
            //매니페스트에 권한이 추가되어 있다해도 여기서 다시 한번 확인해야함
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
            } else {
                when { //프로바이더 제공자 활성화 여부 체크
                    isNetworkEnabled == true -> {
                        val location =
                            lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) //인터넷기반으로 위치를 찾음
                        var getLongitude = location?.longitude!!
                        var getLatitude = location.latitude
                        startpointLatitude = getLatitude
                        startpointLongitude = getLongitude


                    }
                    isGPSEnabled == true -> {
                        val location =
                            lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER) //GPS 기반으로 위치를 찾음
                        var getLongitude = location?.longitude!!
                        var getLatitude = location.latitude
                        startpointLatitude = getLatitude
                        startpointLongitude = getLongitude
                    }
                    else -> {

                    }
                }
                Log.d("lat,lng : ", "Lat : $startpointLatitude, Long : $startpointLongitude")
                pageNumber = 1

                getAddress(startpointLatitude, startpointLongitude)
                addItemsAndMarkersforXY(mapView,
                    addressName.toString(), startpointLatitude, startpointLongitude)

                setDaumMapCurrentLocation(startpointLatitude, startpointLongitude, mapView)

                startpointName = addressName
                startpointAddress = addressName
                startpointRoad = addressName
            }

        }
        //입력 버튼
        confirmButton.setOnClickListener {
            var outintent = Intent(applicationContext, UserInfoActivity::class.java)
            outintent.putExtra("startpointName", startpointName)
            outintent.putExtra("startpointAddress", startpointName)
            outintent.putExtra("startpointRoad", startpointRoad)
            outintent.putExtra("startpointLatitude", startpointLatitude)
            outintent.putExtra("startpointLongitude", startpointLongitude)
            Log.d(
                "newdestination : ",
                "name : $startpointName \n address : $startpointAddress \n road : $startpointRoad \n lat : $startpointLatitude \n long : $startpointLongitude"
            )
            setResult(Activity.RESULT_OK, outintent)
            map.removeView(mapView)
            finish()
        }
    }

    private fun getAddress(latitude: Double, longitude: Double) {
        val geoCoder = Geocoder(this@StartpointSearchActivity, Locale.getDefault())
        val address = geoCoder.getFromLocation(latitude, longitude, 1).first().getAddressLine(0)
        addressName = address
        Log.e("Address", address)
    }

    fun setDaumMapCurrentLocation(latitude: Double, longitude: Double, mapView: MapView) {

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
        setDaumMapCurrentMarker(mapView)
    }

    fun setDaumMapCurrentMarker(mapView: MapView) {
        val marker = MapPOIItem()
        marker.itemName = "현재 위치"
        marker.tag = 0
        marker.mapPoint = MapPoint.mapPointWithGeoCoord(startpointLatitude, startpointLongitude)
        marker.markerType = MapPOIItem.MarkerType.BluePin // 기본으로 제공하는 BluePin 마커 모양.
        marker.selectedMarkerType =
            MapPOIItem.MarkerType.RedPin // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        mapView.addPOIItem(marker)
    }


//    private fun searchLatlng(Lat: Double, Long: Double, mapView: MapView) {
//        val retrofit = Retrofit.Builder()   // Retrofit 구성
//            .baseUrl(AddScheduleActivity.BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        val api = retrofit.create(kakaoAPI::class.java)   // 통신 인터페이스를 객체로 생성
//        val call = api.getSearchLatLng(StartpointSearchActivity.API_KEY, Lat, Long)
//
//        // API 서버에 요청
//        call.enqueue(object : Callback<xySearchResult> {
//            override fun onResponse(
//                call: Call<xySearchResult>,
//                response: Response<xySearchResult>
//            ) {
//                // 통신 성공 (검색 결과는 response.body()에 담겨있음)
//                Log.d("Test", "Raw: ${response.raw()}")
//                Log.d("Test", "Body: ${response.body()}")
//                addItemsAndMarkersforXY(response.body(), mapView, Lat, Long)
//                xyPlaceList = response.body()
//            }
//
//            override fun onFailure(call: Call<xySearchResult>, t: Throwable) {
//                // 통신 실패
//                Log.w("MainActivity", "통신 실패: ${t.message}")
//            }
//        })
//
//    }

    private fun searchKeyword(keyword: String, page: Int, mapView: MapView) {
        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl(AddScheduleActivity.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(kakaoAPI::class.java)   // 통신 인터페이스를 객체로 생성
        val call =
            api.getSearchKeyword(StartpointSearchActivity.API_KEY, keyword, page)   // 검색 조건 입력

        // API 서버에 요청
        call.enqueue(object : Callback<ResultSearchKeyword> {
            override fun onResponse(
                call: Call<ResultSearchKeyword>,
                response: Response<ResultSearchKeyword>
            ) {
                // 통신 성공 (검색 결과는 response.body()에 담겨있음)
                Log.d("Test", "Raw: ${response.raw()}")
                Log.d("Test", "Body: ${response.body()}")
                addItemsAndMarkers(response.body(), mapView)
                placeList = response.body()
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                // 통신 실패
                Log.w("MainActivity", "통신 실패: ${t.message}")
            }
        })
    }

    private fun addItemsAndMarkers(searchResult: ResultSearchKeyword?, mapView: MapView) {
        if (!searchResult?.documents.isNullOrEmpty()) {
            // 검색 결과 있음
            listItems.clear()                   // 리스트 초기화
            mapView.removeAllPOIItems()         // 지도의 마커 모두 제거
            for (document in searchResult!!.documents) {
                // 결과를 리사이클러 뷰에 추가
                val item = StartpointListLayout(
                    document.place_name,
                    document.road_address_name,
                    document.address_name,
                    document.x.toDouble(),
                    document.y.toDouble()
                )
                listItems.add(item)

                // 지도에 마커 추가
                val point = MapPOIItem()
                point.apply {
                    itemName = document.place_name
                    mapPoint = MapPoint.mapPointWithGeoCoord(
                        document.y.toDouble(),
                        document.x.toDouble()
                    )
                    markerType = MapPOIItem.MarkerType.BluePin
                    selectedMarkerType = MapPOIItem.MarkerType.RedPin
                }
                mapView.addPOIItem(point)
            }
            listAdapter.notifyDataSetChanged()

            nextButton.isEnabled = !searchResult.meta.is_end // 페이지가 더 있을 경우 다음 버튼 활성화
            prevButton.isEnabled = pageNumber != 1             // 1페이지가 아닐 경우 이전 버튼 활성화

        } else {
            // 검색 결과 없음
            Toast.makeText(this, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addItemsAndMarkersforXY(mapView: MapView, Address: String, Latitude: Double, Longitude: Double) {

        listItems.clear()
        xyListItems.clear()                   // 리스트 초기화
        mapView.removeAllPOIItems()         // 지도의 마커 모두 제거
        val item = xyStartpointListLayout(
            Address,
            Address,
            Address,
            Longitude,
            Latitude
        )
        xyListItems.add(item)

        // 지도에 마커 추가
        val point = MapPOIItem()
        point.apply {
            itemName = Address
            mapPoint = MapPoint.mapPointWithGeoCoord(Longitude, Latitude)
            markerType = MapPOIItem.MarkerType.BluePin
            selectedMarkerType = MapPOIItem.MarkerType.RedPin
        }
        mapView.addPOIItem(point)
        listAdapter.notifyDataSetChanged()
        xyListAdapter.notifyDataSetChanged()
    }




    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {
        TODO("Not yet implemented")
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
}