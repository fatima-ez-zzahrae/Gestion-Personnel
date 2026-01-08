package com.ensa.gestionpersonnel.data.repository

import com.ensa.gestionpersonnel.data.local.PreferencesManager
import com.ensa.gestionpersonnel.data.remote.api.AuthApi
import com.ensa.gestionpersonnel.data.remote.dto.LoginRequest
import com.ensa.gestionpersonnel.data.remote.dto.LoginResponse
import com.ensa.gestionpersonnel.utils.NetworkResult
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val preferencesManager: PreferencesManager
) {

    suspend fun login(username: String, password: String): NetworkResult<LoginResponse> {
        return try {
            val response = authApi.login(LoginRequest(username = username, password = password))
            preferencesManager.saveToken(response.token)
            NetworkResult.Success(response)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun logout() {
        preferencesManager.clearToken()
    }
}
