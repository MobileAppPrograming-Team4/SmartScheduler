package com.example.smartscheduler.Activity

import com.odsay.odsayandroidsdk.API
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Query

interface TmapAPI {
    @POST("/tmap/routes/pedestrian")
    fun getSearchWalkRoute(
        @Header("Accept") accept: String,
        @Header("appKey") key: String,
        @Header("Content-Type") contentType: String,
        @Query("startX") startX: String,
        @Query("startY") startY: String,
        @Query("endX") endX: String,
        @Query("endY") endY: String,
        @Query("reqCoordType") reqCoordType: String,
        @Query("startName") startName: String,
        @Query("endName") endName: String,
        @Query("searchOption") searchOption: String,
        @Query("resCoordType") resCoordType: String

    ): Call<ResultWalkRouteSearch>

    companion object {
        const val BASE_URL_TMAP_API = "https://apis.openapi.sk.com"
        const val API_KEY = "l7xx6856c73aa91c41e480afc960d336d8c3"

        fun create(): TmapAPI {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_TMAP_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(TmapAPI::class.java)
        }
    }
}