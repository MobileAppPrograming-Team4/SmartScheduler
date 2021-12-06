package com.example.smartscheduler.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartscheduler.R
import com.example.smartscheduler.ResultSearchKeywordAdapter
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class DestinationSearchActivity : AppCompatActivity() {

    private var keyword = ""
    private var pageNumber = 1
    lateinit var map: ConstraintLayout
    lateinit var location: EditText
    lateinit var searchButton: Button
    lateinit var prevButton: Button
    lateinit var nextButton: Button
    lateinit var confirmButton: Button
    lateinit var pageNum : TextView
    private val listItems = arrayListOf<DestinationListLayout>()   // 리사이클러 뷰 아이템
    private val listAdapter = ResultSearchKeywordAdapter(listItems)    // 리사이클러 뷰 어댑터

    var placeList: ResultSearchKeyword? = null
    var destName: String? = null
    var destAddress: String? = null
    var destRoad: String? = null
    var destLatitude: Double = 0.0
    var destLongitude: Double = 0.0

    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 28f1a9b662dea4d3296bfaa59f4590b3"  // REST API 키
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destinationsearch)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_list)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = listAdapter
        location = findViewById(R.id.et_search_field)
        searchButton = findViewById(R.id.btn_search)
        prevButton = findViewById(R.id.btn_prevPage)
        nextButton = findViewById(R.id.btn_nextPage)
        confirmButton = findViewById(R.id.btn_select)
        pageNum = findViewById(R.id.tv_pageNumber)

        val mapView = MapView(this)
        map = findViewById(R.id.clKakaoMapView)
        map.addView(mapView)
        // 리스트 아이템 클릭 시 해당 위치로 이동

        listAdapter.setItemClickListener(object: ResultSearchKeywordAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val mapPoint = MapPoint.mapPointWithGeoCoord(listItems[position].y, listItems[position].x)
                mapView.setMapCenterPointAndZoomLevel(mapPoint, 1, true)
                destName = listItems[position].name
                destAddress = listItems[position].address
                destRoad = listItems[position].road
                destLatitude = listItems[position].y
                destLongitude = listItems[position].x

            }
        })

        // 검색 버튼
        searchButton.setOnClickListener {
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

        //입력 버튼
        confirmButton.setOnClickListener {
            var outintent = Intent(applicationContext, AddScheduleActivity::class.java)
            outintent.putExtra("destName", destName)
            outintent.putExtra("destAddress", destName)
            outintent.putExtra("destName", destName)
            outintent.putExtra("destLatitude", destLatitude)
            outintent.putExtra("destLongitude", destLongitude)
            Log.d("newdestination : ", "name : $destName \n address : $destAddress \n road : $destRoad \n lat : $destLatitude \n long : $destLongitude" )
            setResult(Activity.RESULT_OK, outintent)
            finish()
        }

    }

    private fun searchKeyword(keyword: String, page: Int, mapView: MapView) {
        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl(AddScheduleActivity.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(kakaoAPI::class.java)   // 통신 인터페이스를 객체로 생성
        val call = api.getSearchKeyword(DestinationSearchActivity.API_KEY, keyword, page)   // 검색 조건 입력

        // API 서버에 요청
        call.enqueue(object: Callback<ResultSearchKeyword> {
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
                val item = DestinationListLayout(document.place_name,
                    document.road_address_name,
                    document.address_name,
                    document.x.toDouble(),
                    document.y.toDouble())
                listItems.add(item)

                // 지도에 마커 추가
                val point = MapPOIItem()
                point.apply {
                    itemName = document.place_name
                    mapPoint = MapPoint.mapPointWithGeoCoord(document.y.toDouble(),
                        document.x.toDouble())
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

}
