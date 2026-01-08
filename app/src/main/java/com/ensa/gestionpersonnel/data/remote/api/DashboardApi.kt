package com.ensa.gestionpersonnel.data.remote.api

import com.ensa.gestionpersonnel.data.remote.dto.DashboardStats
import retrofit2.Response
import retrofit2.http.GET

interface DashboardApi {
    @GET("dashboard/stats")
    suspend fun getStats(): Response<DashboardStats>
}

