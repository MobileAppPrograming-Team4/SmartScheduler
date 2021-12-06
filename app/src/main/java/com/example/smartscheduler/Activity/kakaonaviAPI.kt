package com.example.smartscheduler.Activity

import com.odsay.odsayandroidsdk.API
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface kakaonaviAPI {

    @GET("/v1/directions")
    fun getSearchCarRoute(
        @Header("Authorization") key: String,     // 카카오 API 인증키 [필수]
        @Query("origin") origin: String,          // 출발지 경도 위도
        @Query("destination") destination: String //도착지 경도 위도

    ): Call<ResultCarRouteSearch>    // 받아온 정보가 ResultSearchKeyword 클래스의 구조로 담김

    companion object {
        const val BASE_URL_KAKAONAVI_API = "https://apis-navi.kakaomobility.com"
        const val API_KEY = "KakaoAK 28f1a9b662dea4d3296bfaa59f4590b3"

        fun create(): kakaonaviAPI {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_KAKAONAVI_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(kakaonaviAPI::class.java)
        }
    }
}