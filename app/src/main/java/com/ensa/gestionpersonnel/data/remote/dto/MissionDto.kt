package com.ensa.gestionpersonnel.data.remote.dto

import com.ensa.gestionpersonnel.domain.model.Mission
import com.ensa.gestionpersonnel.domain.model.StatutMission
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * DTO pour les échanges avec l'API concernant les missions
 */
data class MissionDto(
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("destination")
    val destination: String,

    @SerializedName("objetMission")
    val objetMission: String,

    @SerializedName("dateDepart")
    val dateDepart: String,

    @SerializedName("dateRetour")
    val dateRetour: String,

    @SerializedName("statut")
    val statut: String,

    @SerializedName("rapportUrl")
    val rapportUrl: String? = null,

    @SerializedName("personnelId")
    val personnelId: Long,

    @SerializedName("personnelNom")
    val personnelNom: String? = null,

    @SerializedName("personnelPrenom")
    val personnelPrenom: String? = null
) {
    /**
     * Convertit le DTO en modèle de domaine
     */
    fun toDomain(): Mission {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return Mission(
            id = id,
            destination = destination,
            objetMission = objetMission,
            dateDepart = dateFormat.parse(dateDepart) ?: throw IllegalArgumentException("Invalid date format"),
            dateRetour = dateFormat.parse(dateRetour) ?: throw IllegalArgumentException("Invalid date format"),
            statut = when (statut.uppercase()) {
                "PLANIFIEE", "PLANIFIÉE" -> StatutMission.PLANIFIEE
                "EN_COURS" -> StatutMission.EN_COURS
                "TERMINEE", "TERMINÉE" -> StatutMission.TERMINEE
                "ANNULEE", "ANNULÉE" -> StatutMission.ANNULEE
                else -> StatutMission.PLANIFIEE
            },
            rapportUrl = rapportUrl,
            personnelId = personnelId,
            personnelNom = personnelNom,
            personnelPrenom = personnelPrenom
        )
    }

    companion object {
        /**
         * Crée un DTO à partir d'un modèle de domaine
         */
        fun fromDomain(mission: Mission): MissionDto {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            return MissionDto(
                id = mission.id,
                destination = mission.destination,
                objetMission = mission.objetMission,
                dateDepart = dateFormat.format(mission.dateDepart),
                dateRetour = dateFormat.format(mission.dateRetour),
                statut = mission.statut.name,
                rapportUrl = mission.rapportUrl,
                personnelId = mission.personnelId,
                personnelNom = mission.personnelNom,
                personnelPrenom = mission.personnelPrenom
            )
        }
    }
}