package com.ensa.gestionpersonnel.ui.absence

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensa.gestionpersonnel.data.repository.AbsenceRepository
import com.ensa.gestionpersonnel.domain.model.Absence
import com.ensa.gestionpersonnel.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AbsenceViewModel @Inject constructor(
    private val absenceRepository: AbsenceRepository
) : ViewModel() {

    private val _absences = MutableLiveData<NetworkResult<List<Absence>>>()
    val absences: LiveData<NetworkResult<List<Absence>>> = _absences

    private val _absenceOperation = MutableLiveData<NetworkResult<Absence?>>()
    val absenceOperation: LiveData<NetworkResult<Absence?>> = _absenceOperation

    private val _filteredAbsences = MutableLiveData<List<Absence>>(emptyList())
    val filteredAbsences: LiveData<List<Absence>> = _filteredAbsences

    private val _currentFilterType = MutableLiveData<String?>()
    val currentFilterType: LiveData<String?> = _currentFilterType

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadAllAbsences()
    }

    fun loadAllAbsences() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                absenceRepository.getAllAbsences().collect { result ->
                    _absences.value = result
                    when (result) {
                        is NetworkResult.Success -> {
                            _filteredAbsences.value = result.data ?: emptyList()
                            _isLoading.value = false
                        }
                        is NetworkResult.Error -> {
                            _filteredAbsences.value = emptyList()
                            _isLoading.value = false
                        }
                        is NetworkResult.Loading -> {
                            _isLoading.value = true
                        }
                    }
                }
            } catch (e: Exception) {
                _absences.value = NetworkResult.Error("Erreur: ${e.message}")
                _filteredAbsences.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun loadAbsencesByPersonnel(personnelId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                absenceRepository.getAbsencesByPersonnel(personnelId).collect { result ->
                    _absences.value = result
                    when (result) {
                        is NetworkResult.Success -> {
                            _filteredAbsences.value = result.data ?: emptyList()
                            _isLoading.value = false
                        }
                        is NetworkResult.Error -> {
                            _filteredAbsences.value = emptyList()
                            _isLoading.value = false
                        }
                        is NetworkResult.Loading -> {
                            _isLoading.value = true
                        }
                    }
                }
            } catch (e: Exception) {
                _absences.value = NetworkResult.Error("Erreur: ${e.message}")
                _filteredAbsences.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun createAbsence(absence: Absence) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                absenceRepository.createAbsence(absence).collect { result ->
                    _absenceOperation.value = result as NetworkResult<Absence?>?
                    _isLoading.value = false
                    if (result is NetworkResult.Success && result.data != null) {
                        // Recharger la liste après création
                        val currentAbsences = _absences.value
                        if (currentAbsences is NetworkResult.Success) {
                            val newList = currentAbsences.data?.toMutableList() ?: mutableListOf()
                            newList.add(result.data)
                            _absences.value = NetworkResult.Success(newList)
                            _filteredAbsences.value = newList
                        } else {
                            loadAllAbsences()
                        }
                    }
                }
            } catch (e: Exception) {
                _absenceOperation.value = NetworkResult.Error("Erreur: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    fun updateAbsence(absence: Absence) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                absenceRepository.updateAbsence(absence).collect { result ->
                    _absenceOperation.value = result as NetworkResult<Absence?>?
                    _isLoading.value = false
                    if (result is NetworkResult.Success && result.data != null) {
                        // Mettre à jour l'absence dans la liste
                        val currentAbsences = _absences.value
                        if (currentAbsences is NetworkResult.Success) {
                            val updatedList = currentAbsences.data?.map { existingAbsence ->
                                if (existingAbsence.id == absence.id) result.data else existingAbsence
                            } ?: emptyList()
                            _absences.value = NetworkResult.Success(updatedList)
                            _filteredAbsences.value = updatedList
                        }
                    }
                }
            } catch (e: Exception) {
                _absenceOperation.value = NetworkResult.Error("Erreur: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    fun validateAbsence(absenceId: Long, validate: Boolean) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                absenceRepository.validateAbsence(absenceId, validate).collect { result ->
                    _absenceOperation.value = result as NetworkResult<Absence?>?
                    _isLoading.value = false
                    if (result is NetworkResult.Success && result.data != null) {
                        // Mettre à jour l'absence dans la liste
                        val currentAbsences = _absences.value
                        if (currentAbsences is NetworkResult.Success) {
                            val updatedList = currentAbsences.data?.map { existingAbsence ->
                                if (existingAbsence.id == absenceId) result.data else existingAbsence
                            } ?: emptyList()
                            _absences.value = NetworkResult.Success(updatedList)
                            _filteredAbsences.value = updatedList
                        }
                    }
                }
            } catch (e: Exception) {
                _absenceOperation.value = NetworkResult.Error("Erreur: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    fun deleteAbsence(absenceId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                absenceRepository.deleteAbsence(absenceId).collect { result ->
                    _isLoading.value = false
                    _absenceOperation.value = when (result) {
                        is NetworkResult.Success -> NetworkResult.Success(null)
                        is NetworkResult.Error -> result
                        is NetworkResult.Loading -> result
                    } as NetworkResult<Absence?>?

                    if (result is NetworkResult.Success && result.data == true) {
                        // Supprimer l'absence de la liste
                        val currentAbsences = _absences.value
                        if (currentAbsences is NetworkResult.Success) {
                            val filteredList = currentAbsences.data?.filter { it.id != absenceId } ?: emptyList()
                            _absences.value = NetworkResult.Success(filteredList)
                            _filteredAbsences.value = filteredList
                        }
                    }
                }
            } catch (e: Exception) {
                _absenceOperation.value = NetworkResult.Error("Erreur: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    fun filterAbsencesByType(type: String?) {
        _currentFilterType.value = type
        applyFilters(type = type)
    }

    fun filterAbsencesByStatus(validated: Boolean?) {
        applyFilters(validated = validated)
    }

    fun searchAbsences(query: String) {
        applyFilters(searchQuery = query)
    }

    fun clearFilters() {
        _currentFilterType.value = null
        when (val currentAbsences = _absences.value) {
            is NetworkResult.Success -> {
                _filteredAbsences.value = currentAbsences.data ?: emptyList()
            }
            else -> {
                _filteredAbsences.value = emptyList()
            }
        }
    }

    private fun applyFilters(
        type: String? = _currentFilterType.value,
        validated: Boolean? = null,
        searchQuery: String? = null
    ) {
        when (val currentAbsences = _absences.value) {
            is NetworkResult.Success -> {
                val data = currentAbsences.data ?: emptyList()
                var filtered = data

                // Appliquer le filtre par type
                if (type != null) {
                    filtered = filtered.filter { it.type.name == type }
                }

                // Appliquer le filtre par statut de validation
                if (validated != null) {
                    filtered = filtered.filter { it.estValideeParAdmin == validated }
                }

                // Appliquer la recherche
                if (!searchQuery.isNullOrBlank()) {
                    filtered = filtered.filter { absence ->
                        absence.personnelNom.contains(searchQuery, ignoreCase = true) ||
                                absence.personnelPrenom.contains(searchQuery, ignoreCase = true) ||
                                absence.personnelPpr.contains(searchQuery, ignoreCase = true) ||
                                absence.motif?.contains(searchQuery, ignoreCase = true) == true
                    }
                }

                _filteredAbsences.value = filtered
            }
            else -> {
                _filteredAbsences.value = emptyList()
            }
        }
    }
}