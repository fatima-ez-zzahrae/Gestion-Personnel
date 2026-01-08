package com.ensa.gestionpersonnel.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensa.gestionpersonnel.data.remote.dto.DashboardStats
import com.ensa.gestionpersonnel.data.repository.DashboardRepository
import com.ensa.gestionpersonnel.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _statsState = MutableLiveData<NetworkResult<DashboardStats>>()
    val statsState: LiveData<NetworkResult<DashboardStats>> = _statsState

    init {
        loadStats()
    }

    fun loadStats() {
        _statsState.value = NetworkResult.Loading()
        viewModelScope.launch {
            val result = dashboardRepository.getStats()
            _statsState.value = result
        }
    }
}

