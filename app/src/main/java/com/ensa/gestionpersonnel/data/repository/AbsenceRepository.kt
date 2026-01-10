package com.ensa.gestionpersonnel.data.repository

import com.ensa.gestionpersonnel.data.local.AbsenceLocalStorage
import com.ensa.gestionpersonnel.domain.model.Absence
import com.ensa.gestionpersonnel.utils.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AbsenceRepository @Inject constructor(
    private val absenceLocalStorage: AbsenceLocalStorage
) {

    suspend fun getAbsencesByPersonnel(personnelId: Long): Flow<NetworkResult<List<Absence>>> = flow {
        emit(NetworkResult.Loading())
        try {
            val absences = absenceLocalStorage.getAbsencesByPersonnel(personnelId)
            emit(NetworkResult.Success(absences))
        } catch (e: Exception) {
            emit(NetworkResult.Error("Erreur: ${e.message}"))
        }
    }

    suspend fun getAllAbsences(): Flow<NetworkResult<List<Absence>>> = flow {
        emit(NetworkResult.Loading())
        try {
            val absences = absenceLocalStorage.getAllAbsences()
            emit(NetworkResult.Success(absences))
        } catch (e: Exception) {
            emit(NetworkResult.Error("Erreur: ${e.message}"))
        }
    }

    suspend fun createAbsence(absence: Absence): Flow<NetworkResult<Absence>> = flow {
        emit(NetworkResult.Loading())
        try {
            // Sauvegarder et récupérer l'absence avec son ID généré
            val createdAbsence = absenceLocalStorage.saveAbsence(absence)
            emit(NetworkResult.Success(createdAbsence))
        } catch (e: Exception) {
            emit(NetworkResult.Error("Erreur: ${e.message}"))
        }
    }

    suspend fun updateAbsence(absence: Absence): Flow<NetworkResult<Absence>> = flow {
        emit(NetworkResult.Loading())
        try {
            val updatedAbsence = absenceLocalStorage.updateAbsence(absence)
            emit(NetworkResult.Success(updatedAbsence))
        } catch (e: Exception) {
            emit(NetworkResult.Error("Erreur: ${e.message}"))
        }
    }

    suspend fun validateAbsence(absenceId: Long, validate: Boolean): Flow<NetworkResult<Absence>> = flow {
        emit(NetworkResult.Loading())
        try {
            val absence = absenceLocalStorage.getAbsenceById(absenceId)
            if (absence != null) {
                val validatedAbsence = absence.copy(estValideeParAdmin = validate)
                absenceLocalStorage.updateAbsence(validatedAbsence)
                emit(NetworkResult.Success(validatedAbsence))
            } else {
                emit(NetworkResult.Error("Absence non trouvée"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Erreur: ${e.message}"))
        }
    }

    suspend fun deleteAbsence(absenceId: Long): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading())
        try {
            absenceLocalStorage.deleteAbsence(absenceId)
            emit(NetworkResult.Success(true))
        } catch (e: Exception) {
            emit(NetworkResult.Error("Erreur: ${e.message}"))
        }
    }
}