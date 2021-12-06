package com.example.smartscheduler.Activity

import android.content.Context
import android.app.Activity
import android.graphics.Color
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.smartscheduler.*

import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONObject;
// import net.daum.mf.map.api.MapView

class PublicRouteActivity : AppCompatActivity() {
    lateinit var totalTime: TextView
    lateinit var startAndFinal: TextView
    lateinit var stationCount: TextView

    val items = mutableListOf<ListViewItem>()

    lateinit var odsayService: ODsayService
    lateinit var jsonObject_path: JSONObject
    // lateinit var jsonObject_lane: JSONObject

    // lateinit var map: ConstraintLayout
    // val mapView = MapView(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicroute)

        val getIntent = getIntent()
        val arriX = getIntent.getDoubleExtra("x", 0.0)
        val arriY = getIntent.getDoubleExtra("y", 0.0)

        val userInfo: SharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE)
        val depX = userInfo.getString("userLongitude", null)
        val depY = userInfo.getString("userLatitude", null)

        val listView = findViewById<ListView>(R.id.publicRouteListView)

        totalTime = findViewById<TextView>(R.id.publicTime)
        startAndFinal = findViewById<TextView>(R.id.startAndFinal)
        stationCount = findViewById<TextView>(R.id.stationCount)

        // map = findViewById(R.id.publicRouteKakaoMapView)
        // map.addView(mapView)

        odsayService = ODsayService.init(
            this@PublicRouteActivity,
            BuildConfig.ODsay_API_KEY
        )
        odsayService.setConnectionTimeout(5000)
        odsayService.setReadTimeout(5000)

        odsayService.requestSearchPubTransPath(
            depX.toString(),
            depY.toString(),
            arriY.toString(),
            arriX.toString(),
            null,
            null,
            null,
            object : OnResultCallbackListener {
                override fun onSuccess(odsayData: ODsayData, api: API) {
                    jsonObject_path = odsayData!!.json

                    val routeResult = jsonObject_path.getJSONObject("result")
                    val pathArray = routeResult.getJSONArray("path")
                    val firstPath = pathArray.getJSONObject(0)
                    val pathInfo = firstPath.getJSONObject("info")

                    totalTime.setText(pathInfo.getInt("totalTime").toString() + "분")

                    // val mapObject = pathInfo.getString("mabObj")

                    // publicRouteView(mapObject)

                    startAndFinal.setText(pathInfo.getString("firstStartStation") + "→" + pathInfo.getString("lastEndStation"))
                    stationCount.setText(pathInfo.getInt("totalStationCount").toString() + "개 정류장(역) 이동")

                    val subPathInfo = firstPath.getJSONArray("subPath")
                    val transferCount = subPathInfo.length()

                    for (i in 0 until transferCount) {
                        var laneStr = ""
                        var stationStr = ""
                        val subPathObj = subPathInfo.getJSONObject(i)

                        if (subPathObj.getInt("trafficType") == 3) {
                            if (transferCount == 1) {
                                laneStr = "도보 이동"
                                items.add(ListViewItem(laneStr, stationStr))
                            }
                        }
                        else {
                            val laneArray = subPathObj.getJSONArray("lane")
                            val laneInfo = laneArray.getJSONObject(0)
                            val passStop = subPathObj.getJSONObject("passStopList")
                            val passStopArray = passStop.getJSONArray("stations")

                            for (i in 0 until passStopArray.length()) {
                                val stationInfo = passStopArray.getJSONObject(i)
                                if (i == passStopArray.length() - 1) {
                                    stationStr += stationInfo.getString("stationName")
                                    break;
                                }
                                stationStr = stationStr + stationInfo.getString("stationName") + "\n"
                            }

                            if (subPathObj.getInt("trafficType") == 1) {
                                laneStr = "지하철 " + laneInfo.getString("name")
                            }
                            else if (subPathObj.getInt("trafficType") == 2) {
                                laneStr = "버스 " + laneInfo.getString("busNo") + "번"
                            }

                            items.add(ListViewItem(laneStr, stationStr))
                        }

                    }

                    listView.adapter = MyCustomAdapter(items)
                }

                override fun onError(code: Int, message: String, api: API) {
                    totalTime.setText(api.name + " 호출 실패")
                }
            }
        )
    }

    /*
    private fun publicRouteView(mabObject: String) {
        odsayService.requestLoadLane(
            mabObject,
            object : OnResultCallbackListener {
                override fun onSuccess(odsayData: ODsayData?, api: API?) {
                    jsonObject_lane = odsayData!!.json

                    val laneResult = jsonObject_lane.getJSONObject("result")
                    val laneArray = laneResult.getJSONArray("lane")
                    val firstSection = laneArray.getJSONObject(0)
                    val graphArray = firstSection.getJSONArray("graphPos")

                    val polyline = MapPolyline()
                    polyline.setTag(1000)
                    polyline.setLineColor(Color.argb(128, 255, 51, 0))

                    for (i in 0 until graphArray.length()) {
                        val coordinate = graphArray.getJSONObject(i)

                        polyline.addPoint(MapPoint.mapPointWithGeoCoord(coordinate.getDouble("y"), coordinate.getDouble("x")));
                    }

                    mapView.addPolyline(polyline);

                    val mapPointBounds = MapPointBounds(polyline.getMapPoints())
                    val padding = 100
                    mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
                }

                override fun onError(code: Int, message: String, api: API) {
                    totalTime.setText(api.name + " 호출 실패")
                }
            }
        )
    }
    */

    private class MyCustomAdapter(private val items: MutableList<ListViewItem>) : BaseAdapter() {
        override fun getCount(): Int {
            return items.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return items[position]
        }

        override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
            val convertView = LayoutInflater.from(viewGroup?.context)
            val rowMain = convertView.inflate(R.layout.item_publicroute, viewGroup, false)

            val item: ListViewItem = items[position]

            val laneTextView = rowMain.findViewById<TextView>(R.id.publicNumber)
            laneTextView.text = item.lane
            val stationTextView = rowMain.findViewById<TextView>(R.id.routeStation)
            stationTextView.text = item.station

            return rowMain
        }
    }

    data class ListViewItem(val lane: String, val station: String)
}

