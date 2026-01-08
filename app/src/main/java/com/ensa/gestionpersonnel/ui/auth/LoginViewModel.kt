package com.ensa.gestionpersonnel.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensa.gestionpersonnel.data.local.PreferencesManager
import com.ensa.gestionpersonnel.data.remote.dto.LoginResponse
import com.ensa.gestionpersonnel.data.repository.AuthRepository
import com.ensa.gestionpersonnel.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // Mode test : mettre à true pour activer le mode test (contourne le backend)
    private val TEST_MODE = true

    private val _loginState = MutableLiveData<NetworkResult<LoginResponse>>()
    val loginState: LiveData<NetworkResult<LoginResponse>> = _loginState

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = NetworkResult.Error("Veuillez remplir tous les champs")
            return
        }

        _loginState.value = NetworkResult.Loading()

        viewModelScope.launch {
            if (TEST_MODE) {
                // Mode test : simule une connexion réussie
                delay(1000) // Simule un délai réseau
                
                // Génère un token factice
                val fakeToken = "test_token_${System.currentTimeMillis()}"
                
                // Sauvegarde le token
                preferencesManager.saveToken(fakeToken)
                
                // Crée une réponse de test
                val testResponse = LoginResponse(
                    token = fakeToken,
                    userId = 1L,
                    nom = "Test",
                    prenom = "User",
                    email = "$username@ensa.ac.ma"
                )
                
                _loginState.value = NetworkResult.Success(testResponse)
            } else {
                // Mode production : utilise le vrai backend
                val result = authRepository.login(username, password)
                _loginState.value = result
            }
        }
    }
}


