package com.ensa.gestionpersonnel.data.repository

import com.ensa.gestionpersonnel.data.local.PersonnelLocalStorage
import com.ensa.gestionpersonnel.data.remote.api.DashboardApi
import com.ensa.gestionpersonnel.data.remote.dto.DashboardStats
import com.ensa.gestionpersonnel.data.remote.dto.MonthlyData
import com.ensa.gestionpersonnel.utils.NetworkResult
import kotlinx.coroutines.delay
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val dashboardApi: DashboardApi
) {
    
    // Mode test : mettre à false pour utiliser le vrai backend
    private val TEST_MODE = true
    
    suspend fun getStats(): NetworkResult<DashboardStats> {
        if (TEST_MODE) {
            delay(500) // Simule un délai réseau
            val stats = PersonnelLocalStorage.getStats()
            
            val dashboardStats = DashboardStats(
                totalPersonnel = stats["totalPersonnel"] as? Int ?: 0,
                personnelActif = stats["personnelActif"] as? Int ?: 0,
                personnelInactif = stats["personnelInactif"] as? Int ?: 0,
                absencesEnCours = 0, // À implémenter plus tard
                missionsEnCours = 0, // À implémenter plus tard
                avancementsAnnee = 0, // À implémenter plus tard
                repartitionParType = stats["repartitionParType"] as? Map<String, Int> ?: emptyMap(),
                evolutionRecrutement = emptyList(), // À implémenter plus tard
                moyenneAnciennete = stats["moyenneAnciennete"] as? Int ?: 0,
                totalConges = stats["totalConges"] as? Int ?: 0
            )
            
            return NetworkResult.Success(dashboardStats)
        }
        
        return try {
            val response = dashboardApi.getStats()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Impossible de charger les statistiques")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Erreur réseau")
        }
    }
}

