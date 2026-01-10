package com.ensa.gestionpersonnel.ui.personnel.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensa.gestionpersonnel.data.remote.dto.PersonnelDto
import com.ensa.gestionpersonnel.data.repository.PersonnelRepository
import com.ensa.gestionpersonnel.domain.model.Personnel
import com.ensa.gestionpersonnel.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonnelFormViewModel @Inject constructor(
    private val personnelRepository: PersonnelRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personnelId: Long = savedStateHandle.get<Long>("personnelId") ?: 0L
    val isEditMode = personnelId > 0L

    private val _personnelState = MutableLiveData<NetworkResult<Personnel>>()
    val personnelState: LiveData<NetworkResult<Personnel>> = _personnelState

    private val _saveState = MutableLiveData<NetworkResult<Personnel>>()
    val saveState: LiveData<NetworkResult<Personnel>> = _saveState

    init {
        if (isEditMode) {
            loadPersonnel()
        }
    }

    private fun loadPersonnel() {
        _personnelState.value = NetworkResult.Loading()
        viewModelScope.launch {
            val result = personnelRepository.getPersonnelById(personnelId)
            _personnelState.value = result
        }
    }

    fun savePersonnel(personnelDto: PersonnelDto) {
        _saveState.value = NetworkResult.Loading()
        viewModelScope.launch {
            val result = if (isEditMode) {
                personnelRepository.updatePersonnel(personnelId, personnelDto)
            } else {
                personnelRepository.createPersonnel(personnelDto)
            }
            _saveState.value = result
        }
    }

    fun validateForm(
        ppr: String,
        cin: String,
        nomFr: String,
        prenomFr: String,
        email: String,
        telephone: String
    ): Pair<Boolean, String?> {
        
        if (ppr.isBlank() || ppr.length != 8) {
            return Pair(false, "PPR doit contenir 8 chiffres")
        }
        
        if (cin.isBlank() || cin.length < 6) {
            return Pair(false, "CIN invalide")
        }
        
        if (nomFr.isBlank()) {
            return Pair(false, "Nom obligatoire")
        }
        
        if (prenomFr.isBlank()) {
            return Pair(false, "Prénom obligatoire")
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Pair(false, "Email invalide")
        }
        
        if (telephone.isBlank() || telephone.length != 10) {
            return Pair(false, "Téléphone doit contenir 10 chiffres")
        }
        
        return Pair(true, null)
    }

}

