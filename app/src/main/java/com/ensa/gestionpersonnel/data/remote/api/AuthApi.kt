package com.ensa.gestionpersonnel.data.remote.api

import com.ensa.gestionpersonnel.data.remote.dto.LoginRequest
import com.ensa.gestionpersonnel.data.remote.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Void>
}
