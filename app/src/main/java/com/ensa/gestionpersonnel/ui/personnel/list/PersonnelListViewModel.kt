package com.ensa.gestionpersonnel.ui.personnel.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensa.gestionpersonnel.data.repository.PersonnelRepository
import com.ensa.gestionpersonnel.domain.model.Personnel
import com.ensa.gestionpersonnel.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonnelListViewModel @Inject constructor(
    private val personnelRepository: PersonnelRepository
) : ViewModel() {

    private val _personnelListState = MutableLiveData<NetworkResult<List<Personnel>>>()
    val personnelListState: LiveData<NetworkResult<List<Personnel>>> = _personnelListState

    private val _deleteState = MutableLiveData<NetworkResult<Boolean>>()
    val deleteState: LiveData<NetworkResult<Boolean>> = _deleteState

    init {
        loadPersonnelList()
    }

    fun loadPersonnelList() {
        _personnelListState.value = NetworkResult.Loading()
        viewModelScope.launch {
            val result = personnelRepository.getAllPersonnel()
            _personnelListState.value = result
        }
    }

    fun searchPersonnel(query: String) {
        if (query.isBlank()) {
            loadPersonnelList()
            return
        }

        _personnelListState.value = NetworkResult.Loading()
        viewModelScope.launch {
            val result = personnelRepository.searchPersonnel(query)
            _personnelListState.value = result
        }
    }

    fun deletePersonnel(id: Long) {
        _deleteState.value = NetworkResult.Loading()
        viewModelScope.launch {
            val result = personnelRepository.deletePersonnel(id)
            _deleteState.value = result

            if (result is NetworkResult.Success) {
                loadPersonnelList() // Recharger la liste
            }
        }
    }

    fun loadPersonnelById(personnelId: Long) {
        _personnelListState.value = NetworkResult.Loading()
        viewModelScope.launch {
            val result = personnelRepository.getPersonnelById(personnelId)
            when (result) {
                is NetworkResult.Success -> {
                    result.data?.let { personnel ->
                        _personnelListState.value = NetworkResult.Success(listOf(personnel))
                    }
                }
                is NetworkResult.Error -> {
                    _personnelListState.value = NetworkResult.Error(result.message ?: "Erreur")
                }
                is NetworkResult.Loading -> {
                    _personnelListState.value = NetworkResult.Loading()
                }
            }
        }
    }
}