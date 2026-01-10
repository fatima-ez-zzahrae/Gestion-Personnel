package com.ensa.gestionpersonnel.ui.avancement.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensa.gestionpersonnel.data.local.AvancementLocalStorage
import com.ensa.gestionpersonnel.data.local.PersonnelLocalStorage
import com.ensa.gestionpersonnel.domain.model.Avancement
import com.ensa.gestionpersonnel.domain.model.Personnel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AvancementFormViewModel @Inject constructor(
    private val avancementStorage: AvancementLocalStorage
) : ViewModel() {  // ← ENLEVER personnelStorage d'ici

    private val _avancement = MutableLiveData<Avancement?>()
    val avancement: LiveData<Avancement?> = _avancement

    private val _personnelList = MutableLiveData<List<Personnel>>()
    val personnelList: LiveData<List<Personnel>> = _personnelList

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadPersonnelList() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                PersonnelLocalStorage.getAllPersonnel()  // ← Accès direct
            }
            _personnelList.value = list.filter { it.estActif }
        }
    }

    fun loadAvancement(id: Long) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                avancementStorage.getAvancementById(id)
            }
            _avancement.value = result
        }
    }

    fun saveAvancement(
        id: Long,
        personnelId: Long,
        dateDecision: Date,
        dateEffet: Date,
        gradePrecedent: String,
        gradeNouveau: String,
        echellePrecedente: Int,
        echelleNouvelle: Int,
        echelonPrecedent: Int,
        echelonNouveau: Int,
        description: String
    ) {
        viewModelScope.launch {
            if (gradePrecedent.isBlank()) {
                _error.value = "Le grade précédent est obligatoire"
                return@launch
            }

            if (gradeNouveau.isBlank()) {
                _error.value = "Le nouveau grade est obligatoire"
                return@launch
            }

            if (personnelId == 0L) {
                _error.value = "Veuillez sélectionner un personnel"
                return@launch
            }

            val avancement = Avancement(
                id = id,
                personnelId = personnelId,
                dateDecision = dateDecision,
                dateEffet = dateEffet,
                gradePrecedent = gradePrecedent,
                gradeNouveau = gradeNouveau,
                echellePrecedente = echellePrecedente,
                echelleNouvelle = echelleNouvelle,
                echelonPrecedent = echelonPrecedent,
                echelonNouveau = echelonNouveau,
                description = description
            )

            if (!avancement.isValid()) {
                _error.value = "Les données de l'avancement sont invalides"
                return@launch
            }

            withContext(Dispatchers.IO) {
                avancementStorage.saveAvancement(avancement)
            }

            _saveSuccess.value = true
        }
    }
}