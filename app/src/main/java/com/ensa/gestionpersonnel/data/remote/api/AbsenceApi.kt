package com.ensa.gestionpersonnel.data.remote.api

import com.ensa.gestionpersonnel.data.remote.dto.AbsenceRequest
import com.ensa.gestionpersonnel.data.remote.dto.AbsenceResponse
import retrofit2.Response
import retrofit2.http.*

interface AbsenceApi {

    @GET("api/absences")
    suspend fun getAllAbsences(): List<AbsenceResponse>

    @GET("api/personnel/{personnelId}/absences")
    suspend fun getAbsencesByPersonnel(@Path("personnelId") personnelId: Long): List<AbsenceResponse>

    @POST("api/absences")
    suspend fun createAbsence(@Body request: AbsenceRequest): AbsenceResponse

    @PUT("api/absences/{id}")
    suspend fun updateAbsence(@Path("id") id: Long, @Body request: AbsenceRequest): AbsenceResponse

    @PATCH("api/absences/{id}/validate")
    suspend fun validateAbsence(@Path("id") id: Long, @Query("validate") validate: Boolean): AbsenceResponse

    @DELETE("api/absences/{id}")
    suspend fun deleteAbsence(@Path("id") id: Long): Response<Unit>
}