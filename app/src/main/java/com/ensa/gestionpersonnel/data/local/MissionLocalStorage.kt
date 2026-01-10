package com.ensa.gestionpersonnel.data.local

import android.content.Context
import com.ensa.gestionpersonnel.domain.model.Mission
import com.ensa.gestionpersonnel.domain.model.StatutMission
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissionLocalStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("missions_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_MISSIONS = "missions_list"
        private const val KEY_MISSION_ID_COUNTER = "mission_id_counter"
    }

    /**
     * Récupère toutes les missions
     */
    fun getAllMissions(): List<Mission> {
        val json = prefs.getString(KEY_MISSIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<Mission>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Récupère une mission par ID
     */
    fun getMissionById(id: Long): Mission? {
        return getAllMissions().find { it.id == id }
    }

    /**
     * Récupère les missions d'un personnel
     */
    fun getMissionsByPersonnel(personnelId: Long): List<Mission> {
        return getAllMissions().filter { it.personnelId == personnelId }
    }

    /**
     * Récupère les missions par statut
     */
    fun getMissionsByStatut(statut: StatutMission): List<Mission> {
        return getAllMissions().filter { it.statut == statut }
    }

    /**
     * Sauvegarde une nouvelle mission
     */
    fun saveMission(mission: Mission): Mission {
        val missions = getAllMissions().toMutableList()

        val missionToSave = if (mission.id == 0L) {
            // Nouvelle mission - générer un ID
            val newId = getNextId()
            mission.copy(id = newId)
        } else {
            mission
        }

        missions.add(missionToSave)
        saveMissions(missions)
        return missionToSave
    }

    /**
     * Met à jour une mission existante
     */
    fun updateMission(mission: Mission): Mission? {
        val missions = getAllMissions().toMutableList()
        val index = missions.indexOfFirst { it.id == mission.id }

        if (index != -1) {
            missions[index] = mission
            saveMissions(missions)
            return mission
        }
        return null
    }

    /**
     * Clôture une mission (change son statut à TERMINEE)
     */
    fun cloturerMission(missionId: Long): Mission? {
        val mission = getMissionById(missionId) ?: return null
        val updatedMission = mission.copy(statut = StatutMission.TERMINEE)
        return updateMission(updatedMission)
    }

    /**
     * Supprime une mission
     */
    fun deleteMission(missionId: Long): Boolean {
        val missions = getAllMissions().toMutableList()
        val removed = missions.removeIf { it.id == missionId }

        if (removed) {
            saveMissions(missions)
        }
        return removed
    }

    /**
     * Sauvegarde toutes les missions
     */
    private fun saveMissions(missions: List<Mission>) {
        val json = gson.toJson(missions)
        prefs.edit().putString(KEY_MISSIONS, json).apply()
    }

    /**
     * Génère un nouvel ID pour une mission
     */
    private fun getNextId(): Long {
        val currentId = prefs.getLong(KEY_MISSION_ID_COUNTER, 1000L)
        val nextId = currentId + 1
        prefs.edit().putLong(KEY_MISSION_ID_COUNTER, nextId).apply()
        return nextId
    }

    /**
     * Efface toutes les missions (utile pour les tests)
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}