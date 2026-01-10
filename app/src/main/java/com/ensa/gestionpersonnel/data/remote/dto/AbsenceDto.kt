package com.ensa.gestionpersonnel.data.remote.dto

import com.ensa.gestionpersonnel.domain.model.Absence
import com.ensa.gestionpersonnel.domain.model.AbsenceType
import java.util.Date

data class AbsenceRequest(
    val personnelId: Long,
    val dateDebut: Date,
    val dateFin: Date,
    val type: String,
    val motif: String? = null,
    val justificatifUrl: String? = null
) {
    companion object {
        fun fromAbsence(absence: Absence): AbsenceRequest {
            return AbsenceRequest(
                personnelId = absence.personnelId,
                dateDebut = absence.dateDebut,
                dateFin = absence.dateFin,
                type = absence.type.name,
                motif = absence.motif,
                justificatifUrl = absence.justificatifUrl
            )
        }
    }
}

data class AbsenceResponse(
    val id: Long,
    val personnelId: Long,
    val personnelNom: String,
    val personnelPrenom: String,
    val personnelPpr: String,
    val dateDebut: Date,
    val dateFin: Date,
    val type: String,
    val motif: String? = null,
    val justificatifUrl: String? = null,
    val estValideeParAdmin: Boolean,
    val createdAt: Date,
    val updatedAt: Date
) {
    fun toAbsence(): Absence {
        return Absence(
            id = id,
            personnelId = personnelId,
            personnelNom = personnelNom,
            personnelPrenom = personnelPrenom,
            personnelPpr = personnelPpr,
            dateDebut = dateDebut,
            dateFin = dateFin,
            type = enumValueOf<AbsenceType>(type),
            motif = motif,
            justificatifUrl = justificatifUrl,
            estValideeParAdmin = estValideeParAdmin,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}