package com.jcibravo.mtr_viewer.api

import com.jcibravo.mtr_viewer.classes.DataResponse
import com.jcibravo.mtr_viewer.classes.StationTimetableData
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("arrivals")
    suspend fun getTimetables(
        @Query("worldIndex") worldIndex: Int,
        @Query("stationId") stationId: String?
    ): List<StationTimetableData>

    @GET("data")
    suspend fun getData(): List<DataResponse>
}
