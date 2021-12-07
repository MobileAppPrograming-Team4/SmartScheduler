package com.example.smartscheduler.Activity


data class xySearchResult(
    var documents: List<Places>
)

data class documents(
    var address: Address,
    var road_address: RoadAddress
)

data class Address(
    var address_name: String
)

data class RoadAddress(
    var address_name: String
)

data class Places(
    var address_name: String,           // 전체 지번 주소
    var road_address_name: String,      // 전체 도로명 주소
    var x: String,                      // X 좌표값 혹은 longitude
    var y: String,                      // Y 좌표값 혹은 latitude
)