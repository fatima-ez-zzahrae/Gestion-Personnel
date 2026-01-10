package com.ensa.gestionpersonnel.data.repository

import com.ensa.gestionpersonnel.data.local.MissionLocalStorage
import com.ensa.gestionpersonnel.domain.model.Mission
import com.ensa.gestionpersonnel.domain.model.StatutMission
import com.ensa.gestionpersonnel.utils.NetworkResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * Repository pour la gestion des missions avec stockage local
 */
class MissionRepository @Inject constructor(
    private val localStorage: MissionLocalStorage
) {

    /**
     * Récupère toutes les missions
     */
    fun getAllMissions(): Flow<NetworkResult<List<Mission>>> = flow {
        emit(NetworkResult.Loading())

        try {
            // Simuler un délai réseau
            delay(300)

            val missions = localStorage.getAllMissions()
            emit(NetworkResult.Success(missions))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Erreur de chargement"))
        }
    }

    /**
     * Récupère les missions d'un personnel
     */
    fun getMissionsByPersonnel(personnelId: Long): Flow<NetworkResult<List<Mission>>> = flow {
        emit(NetworkResult.Loading())

        try {
            delay(300)

            val missions = localStorage.getMissionsByPersonnel(personnelId)
            emit(NetworkResult.Success(missions))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Erreur de chargement"))
        }
    }

    /**
     * Récupère les missions par statut
     */
    fun getMissionsByStatut(statut: StatutMission): Flow<NetworkResult<List<Mission>>> = flow {
        emit(NetworkResult.Loading())

        try {
            delay(300)

            val missions = localStorage.getMissionsByStatut(statut)
            emit(NetworkResult.Success(missions))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Erreur de chargement"))
        }
    }

    /**
     * Récupère une mission par son ID
     */
    fun getMissionById(id: Long): Flow<NetworkResult<Mission>> = flow {
        emit(NetworkResult.Loading())

        try {
            delay(300)

            val mission = localStorage.getMissionById(id)
            if (mission != null) {
                emit(NetworkResult.Success(mission))
            } else {
                emit(NetworkResult.Error("Mission introuvable"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Erreur de chargement"))
        }
    }

    /**
     * Crée une nouvelle mission
     */
    fun createMission(mission: Mission): Flow<NetworkResult<Mission>> = flow {
        emit(NetworkResult.Loading())

        try {
            delay(500) // Simuler un délai réseau

            val savedMission = localStorage.saveMission(mission)
            emit(NetworkResult.Success(savedMission))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Erreur de création"))
        }
    }

    /**
     * Met à jour une mission
     */
    fun updateMission(mission: Mission): Flow<NetworkResult<Mission>> = flow {
        emit(NetworkResult.Loading())

        try {
            delay(500)

            val updatedMission = localStorage.updateMission(mission)
            if (updatedMission != null) {
                emit(NetworkResult.Success(updatedMission))
            } else {
                emit(NetworkResult.Error("Mission introuvable"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Erreur de mise à jour"))
        }
    }

    /**
     * Clôture une mission
     */
    fun cloturerMission(missionId: Long): Flow<NetworkResult<Mission>> = flow {
        emit(NetworkResult.Loading())

        try {
            delay(500)

            val mission = localStorage.cloturerMission(missionId)
            if (mission != null) {
                emit(NetworkResult.Success(mission))
            } else {
                emit(NetworkResult.Error("Mission introuvable"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Erreur de clôture"))
        }
    }

    /**
     * Supprime une mission
     */
    fun deleteMission(missionId: Long): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading())

        try {
            delay(500)

            val deleted = localStorage.deleteMission(missionId)
            if (deleted) {
                emit(NetworkResult.Success(Unit))
            } else {
                emit(NetworkResult.Error("Mission introuvable"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Erreur de suppression"))
        }
    }

    /**
     * Upload un rapport de mission
     * Note: Dans le stockage local, on simule juste le stockage du chemin
     */
    fun uploadRapport(missionId: Long, rapportFile: MultipartBody.Part): Flow<NetworkResult<Mission>> = flow {
        emit(NetworkResult.Loading())

        try {
            delay(1000) // Simuler upload

            val mission = localStorage.getMissionById(missionId)
            if (mission != null) {
                // Simuler le stockage du rapport
                val updatedMission = mission.copy(
                    rapportUrl = "file://local/rapports/rapport_${missionId}.pdf"
                )
                localStorage.updateMission(updatedMission)
                emit(NetworkResult.Success(updatedMission))
            } else {
                emit(NetworkResult.Error("Mission introuvable"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Erreur d'upload"))
        }
    }
}