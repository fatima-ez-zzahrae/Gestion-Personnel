package com.ensa.gestionpersonnel.data.remote.api

import com.ensa.gestionpersonnel.data.remote.dto.PersonnelDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PersonnelApi {
    
    @GET("personnel")
    suspend fun getAllPersonnel(): Response<List<PersonnelDto>>
    
    @GET("personnel/{id}")
    suspend fun getPersonnelById(@Path("id") id: Long): Response<PersonnelDto>
    
    @GET("personnel/ppr/{ppr}")
    suspend fun getPersonnelByPpr(@Path("ppr") ppr: String): Response<PersonnelDto>
    
    @POST("personnel")
    suspend fun createPersonnel(@Body personnel: PersonnelDto): Response<PersonnelDto>
    
    @PUT("personnel/{id}")
    suspend fun updatePersonnel(
        @Path("id") id: Long,
        @Body personnel: PersonnelDto
    ): Response<PersonnelDto>
    
    @DELETE("personnel/{id}")
    suspend fun deletePersonnel(@Path("id") id: Long): Response<Void>
    
    @GET("personnel/search")
    suspend fun searchPersonnel(@Query("query") query: String): Response<List<PersonnelDto>>
}
