package com.ensa.gestionpersonnel.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensa.gestionpersonnel.data.local.PreferencesManager
import com.ensa.gestionpersonnel.domain.model.ResponsableRH
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _profile = MutableLiveData<ResponsableRH>()
    val profile: LiveData<ResponsableRH> = _profile

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Initialiser avec une valeur par défaut
    init {
        _isLoading.value = false
        _updateSuccess.value = null
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Essayer de charger depuis le stockage local
                val savedProfile = preferencesManager.getRhProfile()
                if (savedProfile != null) {
                    _profile.value = savedProfile
                } else {
                    // Données par défaut si pas encore sauvegardé
                    val defaultProfile = ResponsableRH(
                        id = 1,
                        nom = "EL FAHSSI",
                        prenom = "Chaymae",
                        email = "chaymae@ensa.ma",
                        username = "chaymae.rh"
                    )
                    _profile.value = defaultProfile
                    // Sauvegarder les données par défaut
                    preferencesManager.saveRhProfile(defaultProfile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // En cas d'erreur, charger les valeurs par défaut
                val defaultProfile = ResponsableRH(
                    id = 1,
                    nom = "EL FAHSSI",
                    prenom = "Chaymae",
                    email = "chaymae@ensa.ma",
                    username = "chaymae.rh"
                )
                _profile.value = defaultProfile
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(newRH: ResponsableRH) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Sauvegarder dans le stockage local
                preferencesManager.saveRhProfile(newRH)
                _profile.value = newRH
                _updateSuccess.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _updateSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
}