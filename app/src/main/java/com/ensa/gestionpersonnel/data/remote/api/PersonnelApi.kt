package com.ensa.gestionpersonnel.data.remote.api

import com.ensa.gestionpersonnel.data.remote.dto.PersonnelDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PersonnelApi {

    @GET("personnels")
    suspend fun getPersonnels(): List<PersonnelDto>

    @GET("personnels/{id}")
    suspend fun getPersonnel(@Path("id") id: Long): PersonnelDto

    @POST("personnels")
    suspend fun createPersonnel(@Body dto: PersonnelDto): PersonnelDto

    @PUT("personnels/{id}")
    suspend fun updatePersonnel(@Path("id") id: Long, @Body dto: PersonnelDto): PersonnelDto

    @DELETE("personnels/{id}")
    suspend fun deletePersonnel(@Path("id") id: Long)
}
