package com.ensa.gestionpersonnel.data.repository

import com.ensa.gestionpersonnel.data.remote.api.PersonnelApi
import com.ensa.gestionpersonnel.data.remote.dto.PersonnelDto
import com.ensa.gestionpersonnel.utils.NetworkResult
import javax.inject.Inject

class PersonnelRepository @Inject constructor(
    private val personnelApi: PersonnelApi
) {

    suspend fun getPersonnels(): NetworkResult<List<PersonnelDto>> {
        return try {
            NetworkResult.Success(personnelApi.getPersonnels())
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getPersonnel(id: Long): NetworkResult<PersonnelDto> {
        return try {
            NetworkResult.Success(personnelApi.getPersonnel(id))
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun createPersonnel(dto: PersonnelDto): NetworkResult<PersonnelDto> {
        return try {
            NetworkResult.Success(personnelApi.createPersonnel(dto))
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun updatePersonnel(id: Long, dto: PersonnelDto): NetworkResult<PersonnelDto> {
        return try {
            NetworkResult.Success(personnelApi.updatePersonnel(id, dto))
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun deletePersonnel(id: Long): NetworkResult<Unit> {
        return try {
            personnelApi.deletePersonnel(id)
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }
}
