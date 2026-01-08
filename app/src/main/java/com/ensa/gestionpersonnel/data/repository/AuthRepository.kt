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
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                preferencesManager.saveToken(body.token)
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error("Identifiants invalides")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Erreur de connexion")
        }
    }

    suspend fun logout() {
        try {
            // On appelle l'API pour invalider le token côté backend (si implémenté)
            authApi.logout()
        } finally {
            // On nettoie toujours la session en local
        preferencesManager.clearToken()
        }
    }
}
