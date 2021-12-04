package com.example.smartscheduler.Activity

data class ResultCarRouteSearch(
    var routes: List<Route>
)

data class Route(
    var summary : Summary
)

data class Summary(
    var duration: Int
)
