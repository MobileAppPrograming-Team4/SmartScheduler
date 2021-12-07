package com.example.smartscheduler.Activity

data class ResultWalkRouteSearch(
    var features: List<features>
)

data class features(
    var properties : properties
)

data class properties(
    var totalTime: Int
)
