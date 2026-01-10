package com.ensa.gestionpersonnel.domain.model

import java.util.Date

/**
 * Modèle de domaine pour une Mission
 */
data class Mission(
    val id: Long = 0,
    val destination: String,
    val objetMission: String,
    val dateDepart: Date,
    val dateRetour: Date,
    val statut: StatutMission = StatutMission.PLANIFIEE,
    val rapportUrl: String? = null,
    val personnelId: Long,
    val personnelNom: String? = null,
    val personnelPrenom: String? = null
) {
    /**
     * Vérifie si la mission est terminée
     */
    fun isCloturee(): Boolean = statut == StatutMission.TERMINEE

    /**
     * Calcule la durée de la mission en jours
     */
    fun getDureeJours(): Int {
        val diff = dateRetour.time - dateDepart.time
        return (diff / (1000 * 60 * 60 * 24)).toInt() + 1
    }

    /**
     * Clôture la mission
     */
    fun cloturerMission() {
        // Cette méthode sera appelée via le ViewModel/Repository
    }
}

/**
 * Énumération des statuts possibles d'une mission
 */
enum class StatutMission {
    PLANIFIEE,
    EN_COURS,
    TERMINEE,
    ANNULEE
}