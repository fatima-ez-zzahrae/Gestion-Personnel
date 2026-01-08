package com.ensa.gestionpersonnel.ui.personnel.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensa.gestionpersonnel.data.repository.PersonnelRepository
import com.ensa.gestionpersonnel.domain.model.Personnel
import com.ensa.gestionpersonnel.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonnelDetailViewModel @Inject constructor(
    private val personnelRepository: PersonnelRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personnelId: Long = savedStateHandle.get<Long>("personnelId") ?: 0L

    private val _personnelState = MutableLiveData<NetworkResult<Personnel>>()
    val personnelState: LiveData<NetworkResult<Personnel>> = _personnelState

    private val _deleteState = MutableLiveData<NetworkResult<Boolean>>()
    val deleteState: LiveData<NetworkResult<Boolean>> = _deleteState

    init {
        if (personnelId != 0L) {
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

    fun deletePersonnel() {
        _deleteState.value = NetworkResult.Loading()
        viewModelScope.launch {
            val result = personnelRepository.deletePersonnel(personnelId)
            _deleteState.value = result
        }
    }

    fun refreshPersonnel() {
        loadPersonnel()
    }
}

