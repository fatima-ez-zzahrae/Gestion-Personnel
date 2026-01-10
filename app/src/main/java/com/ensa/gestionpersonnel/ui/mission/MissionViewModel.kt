package com.ensa.gestionpersonnel.ui.mission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensa.gestionpersonnel.data.repository.MissionRepository
import com.ensa.gestionpersonnel.domain.model.Mission
import com.ensa.gestionpersonnel.domain.model.StatutMission
import com.ensa.gestionpersonnel.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class MissionViewModel @Inject constructor(
    private val missionRepository: MissionRepository
) : ViewModel() {

    private val _missionsState = MutableStateFlow<NetworkResult<List<Mission>>>(NetworkResult.Loading())
    val missionsState: StateFlow<NetworkResult<List<Mission>>> = _missionsState.asStateFlow()

    private val _missionDetailState = MutableStateFlow<NetworkResult<Mission>?>(null)
    val missionDetailState: StateFlow<NetworkResult<Mission>?> = _missionDetailState.asStateFlow()

    private val _operationState = MutableStateFlow<NetworkResult<Mission>?>(null)
    val operationState: StateFlow<NetworkResult<Mission>?> = _operationState.asStateFlow()

    private val _deleteState = MutableStateFlow<NetworkResult<Unit>?>(null)
    val deleteState: StateFlow<NetworkResult<Unit>?> = _deleteState.asStateFlow()

    // Garde une copie de toutes les missions pour le filtrage
    private val _allMissions = MutableStateFlow<List<Mission>>(emptyList())

    fun loadAllMissions() {
        viewModelScope.launch {
            missionRepository.getAllMissions().collect { result ->
                _missionsState.value = result
                // Sauvegarder toutes les missions quand on les charge
                if (result is NetworkResult.Success) {
                    _allMissions.value = result.data ?: emptyList()
                }
            }
        }
    }

    fun loadMissionsByPersonnel(personnelId: Long) {
        viewModelScope.launch {
            missionRepository.getMissionsByPersonnel(personnelId).collect { result ->
                _missionsState.value = result
                if (result is NetworkResult.Success) {
                    _allMissions.value = result.data ?: emptyList()
                }
            }
        }
    }

    fun loadMissionsByStatut(statut: StatutMission) {
        viewModelScope.launch {
            missionRepository.getMissionsByStatut(statut).collect { result ->
                _missionsState.value = result
                if (result is NetworkResult.Success) {
                    _allMissions.value = result.data ?: emptyList()
                }
            }
        }
    }

    fun loadMissionDetail(missionId: Long) {
        viewModelScope.launch {
            missionRepository.getMissionById(missionId).collect { result ->
                _missionDetailState.value = result
            }
        }
    }

    fun createMission(mission: Mission) {
        viewModelScope.launch {
            missionRepository.createMission(mission).collect { result ->
                _operationState.value = result
                if (result is NetworkResult.Success) {
                    loadAllMissions()
                }
            }
        }
    }

    fun updateMission(mission: Mission) {
        viewModelScope.launch {
            missionRepository.updateMission(mission).collect { result ->
                _operationState.value = result
                if (result is NetworkResult.Success) {
                    loadAllMissions()
                }
            }
        }
    }

    fun cloturerMission(missionId: Long) {
        viewModelScope.launch {
            missionRepository.cloturerMission(missionId).collect { result ->
                _operationState.value = result
                if (result is NetworkResult.Success) {
                    loadAllMissions()
                }
            }
        }
    }

    fun deleteMission(missionId: Long) {
        viewModelScope.launch {
            missionRepository.deleteMission(missionId).collect { result ->
                _deleteState.value = result
                if (result is NetworkResult.Success) {
                    loadAllMissions()
                }
            }
        }
    }

    fun uploadRapport(missionId: Long, rapportFile: MultipartBody.Part) {
        viewModelScope.launch {
            missionRepository.uploadRapport(missionId, rapportFile).collect { result ->
                _operationState.value = result
                if (result is NetworkResult.Success) {
                    loadMissionDetail(missionId)
                }
            }
        }
    }

    fun searchMissions(query: String) {
        // Si la recherche est vide, afficher toutes les missions
        if (query.isBlank()) {
            _missionsState.value = NetworkResult.Success(_allMissions.value)
            return
        }

        // Filtrer depuis la liste complète sauvegardée
        val filteredMissions = _allMissions.value.filter { mission ->
            mission.destination.contains(query, ignoreCase = true) ||
                    mission.objetMission.contains(query, ignoreCase = true) ||
                    mission.personnelNom?.contains(query, ignoreCase = true) == true ||
                    mission.personnelPrenom?.contains(query, ignoreCase = true) == true
        }
        _missionsState.value = NetworkResult.Success(filteredMissions)
    }

    fun resetOperationState() {
        _operationState.value = null
    }

    fun resetDeleteState() {
        _deleteState.value = null
    }
}