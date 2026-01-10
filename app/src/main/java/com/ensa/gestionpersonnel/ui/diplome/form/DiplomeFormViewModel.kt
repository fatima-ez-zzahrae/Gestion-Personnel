package com.ensa.gestionpersonnel.ui.diplome.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensa.gestionpersonnel.data.local.DiplomeLocalStorage
import com.ensa.gestionpersonnel.data.local.PersonnelLocalStorage
import com.ensa.gestionpersonnel.domain.model.Diplome
import com.ensa.gestionpersonnel.domain.model.NiveauDiplome
import com.ensa.gestionpersonnel.domain.model.Personnel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DiplomeFormViewModel @Inject constructor(
    private val diplomeStorage: DiplomeLocalStorage
) : ViewModel() {

    private val _diplome = MutableLiveData<Diplome?>()
    val diplome: LiveData<Diplome?> = _diplome

    private val _personnelList = MutableLiveData<List<Personnel>>()
    val personnelList: LiveData<List<Personnel>> = _personnelList

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadPersonnelList() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                PersonnelLocalStorage.getAllPersonnel()
            }
            _personnelList.value = list.filter { it.estActif }
        }
    }

    fun loadDiplome(id: Long) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                diplomeStorage.getDiplomeById(id)
            }
            _diplome.value = result
        }
    }

    fun saveDiplome(
        id: Long,
        personnelId: Long,
        intitule: String,
        specialite: String,
        niveau: NiveauDiplome,
        etablissement: String,
        dateObtention: Date,
        fichierPreuve: String
    ) {
        viewModelScope.launch {
            if (intitule.isBlank()) {
                _error.value = "L'intitulé est obligatoire"
                return@launch
            }

            if (specialite.isBlank()) {
                _error.value = "La spécialité est obligatoire"
                return@launch
            }

            if (etablissement.isBlank()) {
                _error.value = "L'établissement est obligatoire"
                return@launch
            }

            if (personnelId == 0L) {
                _error.value = "Veuillez sélectionner un personnel"
                return@launch
            }

            val diplome = Diplome(
                id = id,
                personnelId = personnelId,
                intitule = intitule,
                specialite = specialite,
                niveau = niveau,
                etablissement = etablissement,
                dateObtention = dateObtention,
                fichierPreuve = fichierPreuve
            )

            if (!diplome.estValide()) {
                _error.value = "Les données du diplôme sont invalides"
                return@launch
            }

            withContext(Dispatchers.IO) {
                diplomeStorage.saveDiplome(diplome)
            }

            _saveSuccess.value = true
        }
    }
}