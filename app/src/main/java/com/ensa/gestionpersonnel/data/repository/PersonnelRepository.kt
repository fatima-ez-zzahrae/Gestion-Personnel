package com.ensa.gestionpersonnel.data.repository

import com.ensa.gestionpersonnel.data.local.PersonnelLocalStorage
import com.ensa.gestionpersonnel.data.remote.api.PersonnelApi
import com.ensa.gestionpersonnel.data.remote.dto.PersonnelDto
import com.ensa.gestionpersonnel.domain.model.Personnel
import com.ensa.gestionpersonnel.utils.NetworkResult
import kotlinx.coroutines.delay
import javax.inject.Inject

class PersonnelRepository @Inject constructor(
    private val personnelApi: PersonnelApi
) {
    
    // Mode test : mettre à false pour utiliser le vrai backend
    private val TEST_MODE = true
    
    suspend fun getAllPersonnel(): NetworkResult<List<Personnel>> {
        if (TEST_MODE) {
            delay(500) // Simule un délai réseau
            return NetworkResult.Success(PersonnelLocalStorage.getAllPersonnel())
        }
        
        return try {
            val response = personnelApi.getAllPersonnel()
            if (response.isSuccessful && response.body() != null) {
                val personnelList = response.body()!!.map { it.toDomain() }
                NetworkResult.Success(personnelList)
            } else {
                NetworkResult.Error("Impossible de charger la liste du personnel")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Erreur réseau")
        }
    }
    
    suspend fun getPersonnelById(id: Long): NetworkResult<Personnel> {
        if (TEST_MODE) {
            delay(300)
            val personnel = PersonnelLocalStorage.getPersonnelById(id)
            return if (personnel != null) {
                NetworkResult.Success(personnel)
            } else {
                NetworkResult.Error("Personnel introuvable")
            }
        }
        
        return try {
            val response = personnelApi.getPersonnelById(id)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.toDomain())
            } else {
                NetworkResult.Error("Personnel introuvable")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Erreur réseau")
        }
    }
    
    suspend fun getPersonnelByPpr(ppr: String): NetworkResult<Personnel> {
        return try {
            val response = personnelApi.getPersonnelByPpr(ppr)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.toDomain())
            } else {
                NetworkResult.Error("PPR introuvable")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Erreur réseau")
        }
    }
    
    suspend fun createPersonnel(personnel: PersonnelDto): NetworkResult<Personnel> {
        if (TEST_MODE) {
            delay(800) // Simule un délai réseau
            val createdPersonnel = PersonnelLocalStorage.createPersonnel(personnel)
            return NetworkResult.Success(createdPersonnel)
        }
        
        return try {
            val response = personnelApi.createPersonnel(personnel)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.toDomain())
            } else {
                NetworkResult.Error("Échec de la création")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Erreur réseau")
        }
    }
    
    suspend fun updatePersonnel(id: Long, personnel: PersonnelDto): NetworkResult<Personnel> {
        if (TEST_MODE) {
            delay(800)
            val updatedPersonnel = PersonnelLocalStorage.updatePersonnel(id, personnel)
            return if (updatedPersonnel != null) {
                NetworkResult.Success(updatedPersonnel)
            } else {
                NetworkResult.Error("Personnel introuvable")
            }
        }
        
        return try {
            val response = personnelApi.updatePersonnel(id, personnel)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.toDomain())
            } else {
                NetworkResult.Error("Échec de la modification")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Erreur réseau")
        }
    }
    
    suspend fun deletePersonnel(id: Long): NetworkResult<Boolean> {
        if (TEST_MODE) {
            delay(500)
            val deleted = PersonnelLocalStorage.deletePersonnel(id)
            return if (deleted) {
                NetworkResult.Success(true)
            } else {
                NetworkResult.Error("Personnel introuvable")
            }
        }
        
        return try {
            val response = personnelApi.deletePersonnel(id)
            if (response.isSuccessful) {
                NetworkResult.Success(true)
            } else {
                NetworkResult.Error("Échec de la suppression")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Erreur réseau")
        }
    }
    
    suspend fun searchPersonnel(query: String): NetworkResult<List<Personnel>> {
        if (TEST_MODE) {
            delay(300)
            val results = PersonnelLocalStorage.searchPersonnel(query)
            return NetworkResult.Success(results)
        }
        
        return try {
            val response = personnelApi.searchPersonnel(query)
            if (response.isSuccessful && response.body() != null) {
                val personnelList = response.body()!!.map { it.toDomain() }
                NetworkResult.Success(personnelList)
            } else {
                NetworkResult.Error("Aucun résultat")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Erreur réseau")
        }
    }
}

// Extension pour convertir DTO -> Domain
private fun PersonnelDto.toDomain() = Personnel(
    id = this.id ?: 0L,
    ppr = this.ppr,
    cin = this.cin,
    nomFr = this.nomFr,
    nomAr = this.nomAr,
    prenomFr = this.prenomFr,
    prenomAr = this.prenomAr,
    email = this.email,
    telephone = this.telephone,
    dateNaissance = this.dateNaissance,
    lieuNaissance = this.lieuNaissance,
    sexe = this.sexe,
    adresse = this.adresse,
    photoUrl = this.photoUrl,
    typeEmploye = this.typeEmploye,
    dateRecrutementMinistere = this.dateRecrutementMinistere,
    dateRecrutementENSA = this.dateRecrutementENSA,
    gradeActuel = this.gradeActuel,
    echelleActuelle = this.echelleActuelle,
    echelonActuel = this.echelonActuel,
    soldeConges = this.soldeConges,
    estActif = this.estActif
)
